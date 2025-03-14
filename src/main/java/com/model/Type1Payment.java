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
@DiscriminatorValue("TYPE1")
public class Type1Payment extends Payment {

    @NotNull
    @NotBlank(message = "Details are required for TYPE1 payments")
    private String details;
}
