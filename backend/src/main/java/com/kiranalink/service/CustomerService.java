package com.kiranalink.service;

import com.kiranalink.dto.customer.CustomerRequest;
import com.kiranalink.entity.Customer;
import com.kiranalink.entity.CustomerAccount;
import com.kiranalink.entity.User;
import com.kiranalink.exception.BadRequestException;
import com.kiranalink.exception.ResourceNotFoundException;
import com.kiranalink.repository.CustomerAccountRepository;
import com.kiranalink.repository.CustomerRepository;
import com.kiranalink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final UserRepository userRepository;
    private final KhataScoreService khataScoreService;

    public List<Customer> getCustomers(Long shopkeeperId, String search) {
        List<Customer> customers;
        if (search != null && !search.isBlank()) {
            customers = customerRepository.searchByNameOrPhone(search);
        } else {
            List<CustomerAccount> accounts = customerAccountRepository.findByShopkeeperId(shopkeeperId);
            customers = new ArrayList<>();
            for (CustomerAccount acc : accounts) {
                if (acc.getCustomer() != null) {
                    acc.getCustomer().setKhataBalance(acc.getBalance());
                    customers.add(acc.getCustomer());
                }
            }
            if (customers.isEmpty()) {
                customers = customerRepository.findAll();
            }
        }

        for (Customer c : customers) {
            Optional<CustomerAccount> acc = customerAccountRepository.findByCustomerIdAndShopkeeperId(c.getId(), shopkeeperId);
            acc.ifPresent(account -> c.setKhataBalance(account.getBalance()));
        }

        return customers;
    }

    public Customer getCustomerById(Long customerId, Long shopkeeperId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        customerAccountRepository.findByCustomerIdAndShopkeeperId(customerId, shopkeeperId)
                .ifPresent(acc -> customer.setKhataBalance(acc.getBalance()));

        return customer;
    }

    public Customer getCustomerByPhone(String phone, Long shopkeeperId) {
        Customer customer = customerRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with phone: " + phone));

        customerAccountRepository.findByCustomerIdAndShopkeeperId(customer.getId(), shopkeeperId)
                .ifPresent(acc -> customer.setKhataBalance(acc.getBalance()));

        return customer;
    }

    @Transactional
    public Customer createCustomer(Long shopkeeperId, CustomerRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new BadRequestException("Phone number is required");
        }

        User shopkeeper = userRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found with id: " + shopkeeperId));

        Optional<Customer> existing = customerRepository.findByPhoneNumber(request.getPhoneNumber());
        Customer customer;
        if (existing.isPresent()) {
            customer = existing.get();
        } else {
            customer = new Customer();
            customer.setName(request.getName() != null ? request.getName() : "Customer " + request.getPhoneNumber().substring(Math.max(0, request.getPhoneNumber().length() - 4)));
            customer.setPhoneNumber(request.getPhoneNumber());
            customer.setEmail(request.getEmail());
            customer.setKhataScore(600);
            customer.setTrustScore(600);
            customer.setKhataLimit(BigDecimal.valueOf(3000));
            customer.setKhataBalance(BigDecimal.ZERO);
            customer.setLastScoreUpdate(LocalDateTime.now());
            customer = customerRepository.save(customer);
        }

        Optional<CustomerAccount> account = customerAccountRepository.findByCustomerIdAndShopkeeperId(customer.getId(), shopkeeperId);
        if (account.isEmpty()) {
            CustomerAccount newAcc = new CustomerAccount(customer, shopkeeper, BigDecimal.ZERO);
            customerAccountRepository.save(newAcc);
        }

        return customer;
    }

    @Transactional
    public Customer updateCustomer(Long customerId, Long shopkeeperId, CustomerRequest request) {
        Customer customer = getCustomerById(customerId, shopkeeperId);

        if (request.getName() != null) customer.setName(request.getName());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) customer.setPhoneNumber(request.getPhoneNumber());

        customer = customerRepository.save(customer);
        return customer;
    }

    @Transactional
    public void deleteCustomer(Long customerId, Long shopkeeperId) {
        Customer customer = getCustomerById(customerId, shopkeeperId);
        customerAccountRepository.deleteByCustomerIdAndShopkeeperId(customerId, shopkeeperId);
        customerRepository.delete(customer);
    }

    @Transactional
    public Customer recalculateKhata(Long customerId, Long shopkeeperId) {
        khataScoreService.recalculateGlobalKhataScore(customerId);
        return getCustomerById(customerId, shopkeeperId);
    }
}
