package com.kiranalink.service;

import com.kiranalink.entity.Customer;
import com.kiranalink.entity.CustomerAccount;
import com.kiranalink.entity.LedgerEntry;
import com.kiranalink.repository.CustomerAccountRepository;
import com.kiranalink.repository.CustomerRepository;
import com.kiranalink.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhataScoreService {

    private static final int SCORE_MIN = 300;
    private static final int SCORE_MAX = 900;
    private static final int SCORE_DEFAULT = 600;
    private static final int SCORE_MAX_CHANGE = 100;

    private final CustomerRepository customerRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final CustomerAccountRepository customerAccountRepository;

    public BigDecimal calculateLimit(int score) {
        if (score >= 800) return BigDecimal.valueOf(10000);
        if (score >= 700) return BigDecimal.valueOf(6000);
        if (score >= 600) return BigDecimal.valueOf(3000);
        if (score >= 550) return BigDecimal.valueOf(1500);
        if (score >= 500) return BigDecimal.valueOf(1000);
        return BigDecimal.ZERO;
    }

    private static class DebitRecord {
        BigDecimal amount;
        LocalDateTime date;
        LocalDateTime paidDate;

        DebitRecord(BigDecimal amount, LocalDateTime date) {
            this.amount = amount;
            this.date = date;
            this.paidDate = null;
        }
    }

    private static class CreditRecord {
        BigDecimal amount;
        LocalDateTime date;

        CreditRecord(BigDecimal amount, LocalDateTime date) {
            this.amount = amount;
            this.date = date;
        }
    }

    @Transactional
    public void recalculateGlobalKhataScore(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            return;
        }

        List<LedgerEntry> allEntries = ledgerEntryRepository.findByCustomerIdOrderByCreatedAtAsc(customerId);

        int oldScore = customer.getKhataScore() != null ? customer.getKhataScore() : SCORE_DEFAULT;

        if (allEntries.isEmpty()) {
            int clampedScore = clampDelta(oldScore, SCORE_DEFAULT);
            customer.setKhataScore(clampedScore);
            customer.setTrustScore(clampedScore);
            customer.setKhataLimit(calculateLimit(clampedScore));
            customer.setLastScoreUpdate(LocalDateTime.now());
            customerRepository.save(customer);
            return;
        }

        List<DebitRecord> debits = new ArrayList<>();
        List<CreditRecord> credits = new ArrayList<>();

        for (LedgerEntry entry : allEntries) {
            if ("debit".equalsIgnoreCase(entry.getType())) {
                debits.add(new DebitRecord(entry.getAmount(), entry.getCreatedAt()));
            } else if ("credit".equalsIgnoreCase(entry.getType())) {
                credits.add(new CreditRecord(entry.getAmount(), entry.getCreatedAt()));
            }
        }

        // FIFO Settlement logic
        int creditIdx = 0;
        BigDecimal currentCreditAmount = !credits.isEmpty() ? credits.get(0).amount : BigDecimal.ZERO;
        LocalDateTime currentCreditDate = !credits.isEmpty() ? credits.get(0).date : null;

        for (DebitRecord debit : debits) {
            BigDecimal remainingDebit = debit.amount;
            while (remainingDebit.compareTo(BigDecimal.ZERO) > 0 && creditIdx < credits.size()) {
                if (currentCreditAmount.compareTo(remainingDebit) >= 0) {
                    currentCreditAmount = currentCreditAmount.subtract(remainingDebit);
                    debit.paidDate = currentCreditDate;
                    remainingDebit = BigDecimal.ZERO;
                    if (currentCreditAmount.compareTo(BigDecimal.ZERO) == 0) {
                        creditIdx++;
                        if (creditIdx < credits.size()) {
                            currentCreditAmount = credits.get(creditIdx).amount;
                            currentCreditDate = credits.get(creditIdx).date;
                        }
                    }
                } else {
                    remainingDebit = remainingDebit.subtract(currentCreditAmount);
                    creditIdx++;
                    if (creditIdx < credits.size()) {
                        currentCreditAmount = credits.get(creditIdx).amount;
                        currentCreditDate = credits.get(creditIdx).date;
                    }
                }
            }
        }

        // 1. Payment Timeliness Score (PTS) - 40%
        double ptsSum = 0.0;
        for (DebitRecord debit : debits) {
            if (debit.paidDate != null) {
                long daysToPay = Duration.between(debit.date, debit.paidDate).toDays();
                if (daysToPay <= 7) ptsSum += 1.0;
                else if (daysToPay <= 15) ptsSum += 0.8;
                else if (daysToPay <= 30) ptsSum += 0.5;
                else ptsSum += 0.2;
            } else {
                ptsSum += 0.0; // unpaid
            }
        }
        double pts = !debits.isEmpty() ? (ptsSum / debits.size()) : 1.0;

        // 2. Consistency Score (CS) - 25%
        long latePayments = 0;
        for (DebitRecord debit : debits) {
            if (debit.paidDate != null) {
                long daysToPay = Duration.between(debit.date, debit.paidDate).toDays();
                if (daysToPay > 15) {
                    latePayments++;
                }
            }
        }
        double cs = !debits.isEmpty() ? (1.0 - ((double) latePayments / debits.size())) : 1.0;

        // 3. Outstanding Risk Score (ORS) - 20%
        List<CustomerAccount> accounts = customerAccountRepository.findByCustomerId(customerId);
        BigDecimal currentUnpaid = BigDecimal.ZERO;
        for (CustomerAccount acc : accounts) {
            if (acc.getBalance() != null && acc.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                currentUnpaid = currentUnpaid.add(acc.getBalance());
            }
        }

        BigDecimal maxHistorical = BigDecimal.ONE;
        BigDecimal runningBalance = BigDecimal.ZERO;
        for (LedgerEntry entry : allEntries) {
            if ("debit".equalsIgnoreCase(entry.getType())) {
                runningBalance = runningBalance.add(entry.getAmount());
            } else {
                runningBalance = runningBalance.subtract(entry.getAmount());
            }
            if (runningBalance.compareTo(maxHistorical) > 0) {
                maxHistorical = runningBalance;
            }
        }

        double orsRatio = currentUnpaid.doubleValue() / Math.max(1.0, maxHistorical.doubleValue());
        double ors = Math.max(0.0, Math.min(1.0, 1.0 - orsRatio));

        // 4. Recency Score (RS) - 15%
        double rs;
        if (!credits.isEmpty()) {
            CreditRecord lastPayment = credits.get(credits.size() - 1);
            long daysSinceLastPayment = Duration.between(lastPayment.date, LocalDateTime.now()).toDays();
            if (daysSinceLastPayment <= 15) rs = 1.0;
            else if (daysSinceLastPayment <= 30) rs = 0.7;
            else rs = 0.4;
        } else {
            rs = 0.0;
        }

        // Final weighted score S
        double s = (0.40 * pts) + (0.25 * cs) + (0.20 * ors) + (0.15 * rs);
        double rawScore = 300.0 + (s * 600.0);
        int boundedRawScore = (int) Math.round(Math.max(SCORE_MIN, Math.min(SCORE_MAX, rawScore)));

        // Smoothing delta
        int finalScore = clampDelta(oldScore, boundedRawScore);

        customer.setKhataScore(finalScore);
        customer.setTrustScore(finalScore);
        customer.setKhataLimit(calculateLimit(finalScore));
        customer.setLastScoreUpdate(LocalDateTime.now());
        customerRepository.save(customer);

        log.info("[KhataScore] Updated customer {}: PTS={}, CS={}, ORS={}, RS={} -> Score={}",
                customer.getPhoneNumber(), pts, cs, ors, rs, finalScore);
    }

    private int clampDelta(int oldScore, int targetScore) {
        int diff = targetScore - oldScore;
        int clampedDiff = Math.max(-SCORE_MAX_CHANGE, Math.min(SCORE_MAX_CHANGE, diff));
        return Math.max(SCORE_MIN, Math.min(SCORE_MAX, oldScore + clampedDiff));
    }
}
