package com.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Version;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.DecimalMin;
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
    @Column(updatable = false)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private Currency currency;

    @NotNull
    @Pattern(regexp = "^[A-Z]{2}[A-Z0-9]{2,30}$", message = "Invalid Debtor IBAN format")
    @Column(updatable = false)
    private String debtorIban;

    @NotNull
    @Pattern(regexp = "^[A-Z]{2}[A-Z0-9]{2,30}$", message = "Invalid Creditor IBAN format")
    @Column(updatable = false)
    private String creditorIban;

    @Column(updatable = false)
    private LocalDateTime creationTime;

    private BigDecimal cancellationFee;

    private boolean isCancelled;

    private boolean notified;
    private LocalDateTime notificationTime;

    @Version
    private Long version;

}