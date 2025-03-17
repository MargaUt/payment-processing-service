package com.service;

import com.dto.PaymentFeeResponseDTO;
import com.dto.PaymentRequestDTO;
import com.dto.PaymentResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    /**
     * Creates a new payment.
     *
     * @param request the payment request data
     * @return the payment response data
     */
    PaymentResponseDTO createPayment(PaymentRequestDTO request);

    /**
     * Cancels an existing payment.
     *
     * @param paymentId the ID of the payment to be canceled
     * @return the updated payment response data
     */
    PaymentResponseDTO cancelPayment(Long paymentId);

    /**
     * Retrieves all non-canceled payment IDs by a specific amount.
     *
     * @param amount the amount of the payments to search for
     * @return a list of payment IDs that have not been canceled
     */
    List<Long> getNonCancelledPaymentIdsByAmount(BigDecimal amount);

    /**
     * Retrieves all non-canceled payment IDs.
     *
     * @return a list of all non-canceled payment IDs
     */
    List<Long> getAllNonCancelledPaymentIds();

    /**
     * Retrieves the payment details and cancellation fee for a specific payment.
     *
     * @param paymentId the ID of the payment to retrieve
     * @return the payment fee response data
     */
    PaymentFeeResponseDTO getPaymentById(Long paymentId);

}
