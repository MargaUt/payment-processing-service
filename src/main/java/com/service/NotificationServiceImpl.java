package com.service;

import com.dto.PaymentNotificationDTO;
import com.model.Payment;
import com.model.Type1Payment;
import com.model.Type2Payment;
import com.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;

    private static final String TYPE1_NOTIFICATION_URL = "https://api.notification-service.com/payments/type1/";
    private static final String TYPE2_NOTIFICATION_URL = "https://api.notification-service.com/payments/type2/";

    public NotificationServiceImpl(PaymentRepository paymentRepository) {
        this.restTemplate = new RestTemplate();
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentNotificationDTO notifyPaymentCreated(Payment payment) {
        PaymentNotificationDTO notificationDTO = new PaymentNotificationDTO();
        notificationDTO.setPaymentId(payment.getId());
        notificationDTO.setPaymentType(payment.getClass().getSimpleName());
        notificationDTO.setNotificationTime(LocalDateTime.now());

        if (!(payment instanceof Type1Payment) && !(payment instanceof Type2Payment)) {
            log.info("Skipping notification for payment ID: {}, type: {}",
                    payment.getId(), payment.getClass().getSimpleName());
            notificationDTO.setNotified(false);
            return notificationDTO;
        }

        String notificationUrl = determineNotificationUrl(payment);
        boolean success = false;
        Integer statusCode = null;

        try {
            log.info("Sending notification for payment ID: {} to URL: {}",
                    payment.getId(), notificationUrl);

            String fullUrl = notificationUrl + payment.getId();

            ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

            statusCode = response.getStatusCode().value();

            success = response.getStatusCode().is2xxSuccessful();

            log.info("Notification for payment ID: {} was {}, status code: {}",
                    payment.getId(), success ? "successful" : "unsuccessful", statusCode);
        } catch (Exception e) {
            log.error("Failed to send notification for payment ID: {}", payment.getId(), e);
        }

        notificationDTO.setNotified(success);
        notificationDTO.setStatusCode(statusCode);

        saveNotificationResult(payment, success);

        return notificationDTO;
    }

    private String determineNotificationUrl(Payment payment) {
        if (payment instanceof Type1Payment) {
            return TYPE1_NOTIFICATION_URL;
        } else if (payment instanceof Type2Payment) {
            return TYPE2_NOTIFICATION_URL;
        } else {
            throw new IllegalArgumentException("Unsupported payment type for notification: " +
                    payment.getClass().getSimpleName());
        }
    }

    @Transactional
    private void saveNotificationResult(Payment payment, boolean success) {

        payment.setNotified(success);
        payment.setNotificationTime(LocalDateTime.now());

        paymentRepository.save(payment);
        log.info("Notification result saved for payment ID: {}, success: {}", payment.getId(), success);
    }
}