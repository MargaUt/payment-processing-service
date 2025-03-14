package com.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("TYPE3")
public class Type3Payment extends Payment {

    @NotNull
    @NotBlank(message = "BIC code is required for TYPE3 payments")
    private String creditorBic;

}
