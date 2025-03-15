package com.service;

import com.dto.PaymentFeeResponseDTO;
import com.dto.PaymentRequestDTO;
import com.dto.PaymentResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    PaymentResponseDTO createPayment(PaymentRequestDTO request);
    PaymentResponseDTO cancelPayment(Long paymentId);

    List<Long> getNonCancelledPaymentIdsByAmount(BigDecimal amount);

    List<Long> getAllNonCancelledPaymentIds();

    PaymentFeeResponseDTO getPaymentById(Long paymentId);

}
