package com.utils;

import com.dto.PaymentRequestDTO;
import com.model.Payment;
import com.model.Type1Payment;
import com.model.Type2Payment;
import com.model.Type3Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PaymentUtilsTest {

    @Test
    void validatePaymentRequest_ValidRequest() {
        PaymentRequestDTO validRequest = new PaymentRequestDTO();
        validRequest.setAmount(BigDecimal.TEN);
        validRequest.setCurrency("EUR");
        validRequest.setDebtorIban("DE1234567890");
        validRequest.setCreditorIban("GB0987654321");
        validRequest.setDetails("Payment details");

        assertDoesNotThrow(() -> PaymentUtils.validatePaymentRequest(validRequest));
    }

    @Test
    void validatePaymentRequest_InvalidAmount() {
        PaymentRequestDTO invalidRequest = new PaymentRequestDTO();
        invalidRequest.setAmount(BigDecimal.ZERO);
        invalidRequest.setCurrency("EUR");
        invalidRequest.setDebtorIban("DE1234567890");
        invalidRequest.setCreditorIban("GB0987654321");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PaymentUtils.validatePaymentRequest(invalidRequest));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void validatePaymentRequest_InvalidCurrency() {
        PaymentRequestDTO invalidRequest = new PaymentRequestDTO();
        invalidRequest.setAmount(BigDecimal.TEN);
        invalidRequest.setCurrency("GBP");
        invalidRequest.setDebtorIban("DE1234567890");
        invalidRequest.setCreditorIban("GB0987654321");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PaymentUtils.validatePaymentRequest(invalidRequest));
        assertEquals("Currency must be EUR or USD", exception.getMessage());
    }

    @Test
    void validatePaymentRequest_MissingDebtorIban() {
        PaymentRequestDTO invalidRequest = new PaymentRequestDTO();
        invalidRequest.setAmount(BigDecimal.TEN);
        invalidRequest.setCurrency("EUR");
        invalidRequest.setDebtorIban(null);
        invalidRequest.setCreditorIban("GB0987654321");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PaymentUtils.validatePaymentRequest(invalidRequest));
        assertEquals("Debtor IBAN is required", exception.getMessage());
    }

    @Test
    void validatePaymentRequest_MissingCreditorBicForType1() {
        PaymentRequestDTO invalidRequest = new PaymentRequestDTO();
        invalidRequest.setAmount(BigDecimal.TEN);
        invalidRequest.setCurrency("EUR");
        invalidRequest.setDebtorIban("DE1234567890");
        invalidRequest.setCreditorIban("GB0987654321");
        invalidRequest.setDetails("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PaymentUtils.validatePaymentRequest(invalidRequest));
        assertEquals("Details are required for TYPE1 (EUR) payments", exception.getMessage());
    }

    @Test
    void determineAndCreatePaymentEntity_Type1Payment() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setCurrency("EUR");
        request.setDetails("Payment details");

        Payment payment = PaymentUtils.determineAndCreatePaymentEntity(request);
        assertInstanceOf(Type1Payment.class, payment);
        assertEquals("Payment details", ((Type1Payment) payment).getDetails());
    }

    @Test
    void determineAndCreatePaymentEntity_Type2Payment() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setCurrency("USD");
        request.setDetails("Payment details");

        Payment payment = PaymentUtils.determineAndCreatePaymentEntity(request);
        assertInstanceOf(Type2Payment.class, payment);
        assertEquals("Payment details", ((Type2Payment) payment).getDetails());
    }

    @Test
    void determineAndCreatePaymentEntity_Type3Payment() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setCurrency("USD");
        request.setCreditorBic("BIC123");

        Payment payment = PaymentUtils.determineAndCreatePaymentEntity(request);
        assertInstanceOf(Type3Payment.class, payment);
        assertEquals("BIC123", ((Type3Payment) payment).getCreditorBic());
    }

    @Test
    void calculateCancellationFee_Type1Payment() {
        Type1Payment payment = new Type1Payment();
        payment.setCreationTime(LocalDateTime.now().minusHours(5));
        payment.setCurrency("EUR");

        BigDecimal fee = PaymentUtils.calculateCancellationFee(payment);
        assertEquals(new BigDecimal("0.25").setScale(2, RoundingMode.HALF_UP), fee);
    }

    @Test
    void calculateCancellationFee_Type2Payment() {
        Type2Payment payment = new Type2Payment();
        payment.setCreationTime(LocalDateTime.now().minusHours(3));
        payment.setCurrency("USD");

        BigDecimal fee = PaymentUtils.calculateCancellationFee(payment);
        assertEquals(new BigDecimal("0.28").setScale(2, RoundingMode.HALF_UP), fee);
    }

    @Test
    void calculateCancellationFee_Type3Payment() {
        Type3Payment payment = new Type3Payment();
        payment.setCreationTime(LocalDateTime.now().minusHours(10));
        payment.setCurrency("EUR");

        BigDecimal fee = PaymentUtils.calculateCancellationFee(payment);
        assertEquals(new BigDecimal("1.50").setScale(2, RoundingMode.HALF_UP), fee);
    }

    @Test
    void convertUsdToEur() {
        BigDecimal usdAmount = new BigDecimal("10.00");
        BigDecimal eurAmount = PaymentUtils.convertUsdToEur(usdAmount);
        assertEquals(new BigDecimal("9.20").setScale(2, RoundingMode.HALF_UP), eurAmount);
    }
}