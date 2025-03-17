package com.utils;

import com.dto.PaymentRequestDTO;
import com.model.Payment;
import com.model.Type1Payment;
import com.model.Type2Payment;
import com.model.Type3Payment;
import com.model.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class PaymentUtils {

    /**
     * Validates payment request data
     * @param request The payment request to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validatePaymentRequest(PaymentRequestDTO request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Currency currency;
        try {
            currency = request.getCurrency();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Currency must be EUR or USD");
        }

        if (request.getDebtorIban() == null || request.getDebtorIban().isBlank()) {
            throw new IllegalArgumentException("Debtor IBAN is required");
        }

        if (request.getCreditorIban() == null || request.getCreditorIban().isBlank()) {
            throw new IllegalArgumentException("Creditor IBAN is required");
        }

        if (currency == Currency.EUR) {
            if (request.getCreditorBic() == null || request.getCreditorBic().isBlank()) {
                if (request.getDetails() == null || request.getDetails().isBlank()) {
                    throw new IllegalArgumentException("Details are required for TYPE1 (EUR) payments");
                }
            }
        }
    }

    /**
     * Determines and creates the appropriate payment type entity based on request data.
     *
     * @param request The payment request data
     * @return The appropriate payment entity (Type1Payment, Type2Payment, or Type3Payment)
     */
    public static Payment determineAndCreatePaymentEntity(PaymentRequestDTO request) {
        if (request.getCreditorBic() != null && !request.getCreditorBic().isBlank()) {
            Type3Payment payment = new Type3Payment();
            payment.setCreditorBic(request.getCreditorBic());
            return payment;
        }

        Currency currency = request.getCurrency();

        switch (currency) {
            case EUR:
                Type1Payment type1Payment = new Type1Payment();
                type1Payment.setDetails(request.getDetails());
                return type1Payment;

            case USD:
                Type2Payment type2Payment = new Type2Payment();
                if (request.getDetails() != null && !request.getDetails().isBlank()) {
                    type2Payment.setDetails(request.getDetails());
                }
                return type2Payment;

            default:
                throw new IllegalArgumentException("Unsupported currency: " + request.getCurrency());
        }
    }

    /**
     * Calculates the cancellation fee for a payment
     * @param payment The payment to calculate cancellation fee for
     * @return The calculated cancellation fee
     */
    public static BigDecimal calculateCancellationFee(Payment payment) {
        long hours = payment.getCreationTime().until(LocalDateTime.now(), ChronoUnit.HOURS);

        double coefficient;
        if (payment instanceof Type1Payment) {
            coefficient = 0.05;
        } else if (payment instanceof Type2Payment) {
            coefficient = 0.1;
        } else if (payment instanceof Type3Payment) {
            coefficient = 0.15;
        } else {
            throw new IllegalStateException("Unknown payment type");
        }

        BigDecimal fee = BigDecimal.valueOf(hours * coefficient);

        if (payment.getCurrency() == Currency.USD) {
            fee = convertUsdToEur(fee);
        }

        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Converts USD amount to EUR using a fixed exchange rate
     * @param usdAmount Amount in USD
     * @return Equivalent amount in EUR
     */
    public static BigDecimal convertUsdToEur(BigDecimal usdAmount) {
        // Fixed rate: 1 USD = 0.92 EUR
        BigDecimal exchangeRate = BigDecimal.valueOf(0.92);
        return usdAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }
}