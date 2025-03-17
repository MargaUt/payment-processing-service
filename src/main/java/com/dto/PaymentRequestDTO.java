package com.dto;

import com.model.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Currency currency;
    @NotNull
    private String debtorIban;
    @NotNull
    private String creditorIban;
    private String details;
    private String creditorBic;
}
