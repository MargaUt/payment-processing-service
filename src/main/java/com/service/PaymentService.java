package com.service;

import com.dto.PaymentRequestDTO;
import com.dto.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO createPayment(PaymentRequestDTO request);
    PaymentResponseDTO cancelPayment(Long paymentId);
}
