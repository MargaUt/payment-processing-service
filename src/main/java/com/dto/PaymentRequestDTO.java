package com.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    @NotNull
    private Double amount;
    @NotNull
    private String currency;
    @NotNull
    private String debtorIban;
    @NotNull
    private String creditorIban;
    private String details;
    private String creditorBic;

}
