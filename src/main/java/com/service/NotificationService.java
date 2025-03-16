package com.service;

import com.dto.PaymentNotificationDTO;
import com.model.Payment;

public interface NotificationService {

    PaymentNotificationDTO notifyPaymentCreated(Payment payment);
}
