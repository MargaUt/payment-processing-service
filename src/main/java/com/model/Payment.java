package com.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
@Getter
@Setter
public abstract class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    //It avoids floating-point precision issues found in double and float.
    private BigDecimal amount;

    @NotNull
    @Pattern(regexp = "EUR|USD", message = "Currency must be EUR or USD")
    private String currency;

    @NotNull
    @NotBlank(message = "Debtor IBAN is required")
    private String debtorIban;

    @NotNull
    @NotBlank(message = "Creditor IBAN is required")
    private String creditorIban;

    // Timestamp when the payment was created
    private LocalDateTime creationTime;

    // The cancellation fee calculated in EUR
    private BigDecimal cancellationFee;

    // Flag to indicate if the payment is canceled
    private boolean isCancelled;

    private boolean notified;
    private LocalDateTime notificationTime;
}