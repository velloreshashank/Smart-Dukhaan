package com.kiranalink.dto.bill;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayVerifyRequest {
    @JsonProperty("razorpay_order_id")
    private String razorpayOrderId;

    @JsonProperty("razorpay_payment_id")
    private String razorpayPaymentId;

    @JsonProperty("razorpay_signature")
    private String razorpaySignature;

    @NotNull(message = "Bill data is required")
    private BillCreateRequest billData;

    public String getRazorpay_order_id() { return razorpayOrderId; }
    public void setRazorpay_order_id(String id) { this.razorpayOrderId = id; }

    public String getRazorpay_payment_id() { return razorpayPaymentId; }
    public void setRazorpay_payment_id(String id) { this.razorpayPaymentId = id; }

    public String getRazorpay_signature() { return razorpaySignature; }
    public void setRazorpay_signature(String sig) { this.razorpaySignature = sig; }
}

