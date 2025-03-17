package com.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.DiscriminatorColumn;
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

    private LocalDateTime creationTime;

    private BigDecimal cancellationFee;

    private boolean isCancelled;

    private boolean notified;
    private LocalDateTime notificationTime;
}