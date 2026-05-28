package com.library.management.dto.request;

import java.math.BigDecimal;

import com.library.management.enums.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FinePaymentRequest {

    @NotNull(message = "Fine ID is required")
    private Long fineId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String transactionId;
}
