package com.exeption;

public class InvalidPaymentDataException extends RuntimeException {
    public InvalidPaymentDataException(String message) {
        super(message);
    }
}