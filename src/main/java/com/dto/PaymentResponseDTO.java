package com.dto;

import com.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private Long id;
    private BigDecimal amount;
    private Currency currency;
    private String debtorIban;
    private String creditorIban;
    private LocalDateTime creationTime;
    private boolean isCancelled;
    private BigDecimal cancellationFee;
}
