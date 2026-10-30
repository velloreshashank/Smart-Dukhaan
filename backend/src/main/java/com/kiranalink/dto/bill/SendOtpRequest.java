package com.kiranalink.dto.bill;

import jakarta.validation.constraints.NotBlank;

public class SendOtpRequest {
    @NotBlank(message = "Customer phone number is required")
    private String customerPhoneNumber;

    public SendOtpRequest() {}

    public String getCustomerPhoneNumber() { return customerPhoneNumber; }
    public void setCustomerPhoneNumber(String customerPhoneNumber) { this.customerPhoneNumber = customerPhoneNumber; }
}
