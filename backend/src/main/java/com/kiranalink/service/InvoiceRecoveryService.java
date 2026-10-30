package com.kiranalink.service;

import com.kiranalink.entity.Customer;
import com.kiranalink.entity.CustomerAccount;
import com.kiranalink.entity.Invoice;
import com.kiranalink.entity.InvoiceReminderHistory;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.CustomerAccountRepository;
import com.kiranalink.repository.CustomerRepository;
import com.kiranalink.repository.InvoiceReminderHistoryRepository;
import com.kiranalink.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceRecoveryService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceReminderHistoryRepository invoiceReminderHistoryRepository;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getInvoiceId() == null) {
            invoice.setInvoiceId("KHATA-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000));
        }
        if (invoice.getStatus() == null) {
            invoice.setStatus("unpaid");
        }
        if (invoice.getReminderLevel() == null) {
            invoice.setReminderLevel(0);
        }
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Map<String, Object> importKhata(Long shopkeeperId) {
        List<CustomerAccount> overdueAccounts = customerAccountRepository.findByShopkeeperId(shopkeeperId).stream()
                .filter(a -> a.getBalance() != null && a.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        int importedCount = 0;
        int reminderCount = 0;
        List<String> results = new ArrayList<>();

        for (CustomerAccount acc : overdueAccounts) {
            Customer customer = customerRepository.findById(acc.getCustomerId()).orElse(null);
            if (customer == null || customer.getPhoneNumber() == null) continue;

            String cleanPhone = customer.getPhoneNumber().replaceAll("[^0-9]", "");
            String last10 = cleanPhone.length() >= 10 ? cleanPhone.substring(cleanPhone.length() - 10) : cleanPhone;

            Optional<Invoice> existing = invoiceRepository.findByClientPhoneEndingWith(last10).stream()
                    .filter(i -> List.of("unpaid", "overdue", "promised", "disputed").contains(i.getStatus()))
                    .findFirst();

            Invoice invoice = existing.orElse(null);
            if (invoice == null) {
                invoice = new Invoice();
                invoice.setInvoiceId("KHATA-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000));
                invoice.setClientName(customer.getName() != null ? customer.getName() : "Customer");
                invoice.setClientEmail((customer.getName() != null ? customer.getName() : "customer").replaceAll("\\s+", "").toLowerCase() + "@example.com");
                invoice.setClientPhone(customer.getPhoneNumber());
                invoice.setAmount(acc.getBalance());
                invoice.setDueDate(LocalDateTime.now().minusMinutes(5));
                invoice.setStatus("overdue");
                invoice.setReminderLevel(0);
                invoice = invoiceRepository.save(invoice);
                importedCount++;
            }

            String waMessage = "Hello " + invoice.getClientName() + "! Friendly reminder from SDukaan: pending balance is ₹" + invoice.getAmount() + ".";
            InvoiceReminderHistory rLog = new InvoiceReminderHistory(invoice, "whatsapp", waMessage, "sent");
            invoiceReminderHistoryRepository.save(rLog);

            invoice.setLastContactedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);

            reminderCount++;
            results.add("Reminder logged for " + invoice.getClientName() + " (₹" + invoice.getAmount() + ")");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("importedCount", importedCount);
        response.put("reminderCount", reminderCount);
        response.put("results", results);
        return response;
    }

    @Transactional
    public Invoice triggerReminder(Long invoiceId, String channel) {
        Invoice invoice = getInvoiceById(invoiceId);

        String ch = (channel != null && !channel.isBlank()) ? channel.toLowerCase() : "whatsapp";
        String msg = "Payment reminder for ₹" + invoice.getAmount() + " from SDukaan.";

        InvoiceReminderHistory logEntry = new InvoiceReminderHistory(invoice, ch, msg, "sent");
        invoiceReminderHistoryRepository.save(logEntry);

        invoice.setReminderLevel((invoice.getReminderLevel() != null ? invoice.getReminderLevel() : 0) + 1);
        invoice.setLastContactedAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice updateStatus(Long invoiceId, String status, LocalDateTime promisedDate, String reason) {
        Invoice invoice = getInvoiceById(invoiceId);
        invoice.setStatus(status.toLowerCase());
        if (promisedDate != null) {
            invoice.setPromisedDate(promisedDate);
        }
        return invoiceRepository.save(invoice);
    }
}
