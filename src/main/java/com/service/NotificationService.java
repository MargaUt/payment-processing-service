package com.service;

import com.dto.PaymentNotificationDTO;
import com.model.Payment;

public interface NotificationService {

    /**
     * Notifies the creation of a payment.
     *
     * @param payment the payment object containing the payment details
     * @return a PaymentNotificationDTO containing the notification result
     */
    PaymentNotificationDTO notifyPaymentCreated(Payment payment);
}
