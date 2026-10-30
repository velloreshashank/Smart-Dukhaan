package com.kiranalink.dto.bill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VerifyOtpRequest {
    @NotBlank(message = "Customer phone number is required")
    private String customerPhoneNumber;

    @NotBlank(message = "OTP is required")
    private String otp;

    @NotNull(message = "Bill data is required")
    private BillCreateRequest billData;

    public VerifyOtpRequest() {}

    public String getCustomerPhoneNumber() { return customerPhoneNumber; }
    public void setCustomerPhoneNumber(String customerPhoneNumber) { this.customerPhoneNumber = customerPhoneNumber; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public BillCreateRequest getBillData() { return billData; }
    public void setBillData(BillCreateRequest billData) { this.billData = billData; }
}
