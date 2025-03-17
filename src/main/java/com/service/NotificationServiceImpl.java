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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PaymentRepository paymentRepository;

    private static final String TYPE1_NOTIFICATION_URL = "https://api.notification-service.com/payments/type1/";
    private static final String TYPE2_NOTIFICATION_URL = "https://api.notification-service.com/payments/type2/";

    private static final String SKIPPING_NOTIFICATION_MESSAGE = "Skipping notification for payment ID: {}, type: {}";
    private static final String SENDING_NOTIFICATION_MESSAGE = "Sending notification for payment ID: {} to URL: {}";
    private static final String NOTIFICATION_ERROR_MESSAGE = "Failed to send notification for payment ID: {}";
    private static final String NOTIFICATION_RESULT_SAVED_MESSAGE = "Notification result saved for payment ID: {}, success: {}";

    public NotificationServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentNotificationDTO notifyPaymentCreated(Payment payment) {
        PaymentNotificationDTO notificationDTO = buildInitialNotificationDTO(payment);

        if (!isPaymentTypeSupported(payment)) {
            logSkippingNotification(payment);
            notificationDTO.setNotified(false);
            return notificationDTO;
        }

        String notificationUrl = determineNotificationUrl(payment);
        ResponseEntity<String> response = sendNotification(payment, notificationUrl);

        boolean success = response != null && response.getStatusCode().is2xxSuccessful();
        notificationDTO.setNotified(success);
        notificationDTO.setStatusCode(response != null ? response.getStatusCode().value() : null);

        saveNotificationResult(payment, success);

        return notificationDTO;
    }

    private PaymentNotificationDTO buildInitialNotificationDTO(Payment payment) {
        PaymentNotificationDTO notificationDTO = new PaymentNotificationDTO();
        notificationDTO.setPaymentId(payment.getId());
        notificationDTO.setPaymentType(payment.getClass().getSimpleName());
        notificationDTO.setNotificationTime(LocalDateTime.now());
        return notificationDTO;
    }

    private boolean isPaymentTypeSupported(Payment payment) {
        return payment instanceof Type1Payment || payment instanceof Type2Payment;
    }

    private void logSkippingNotification(Payment payment) {
        log.info(SKIPPING_NOTIFICATION_MESSAGE, payment.getId(), payment.getClass().getSimpleName());
    }

    private String determineNotificationUrl(Payment payment) {
        if (payment instanceof Type1Payment) {
            return TYPE1_NOTIFICATION_URL;
        } else if (payment instanceof Type2Payment) {
            return TYPE2_NOTIFICATION_URL;
        }
        throw new IllegalArgumentException("Unsupported payment type for notification: " +
                payment.getClass().getSimpleName());
    }

    private ResponseEntity<String> sendNotification(Payment payment, String notificationUrl) {
        try {
            String fullUrl = notificationUrl + payment.getId();
            log.info(SENDING_NOTIFICATION_MESSAGE, payment.getId(), fullUrl);
            ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to send notification for payment ID: {}", payment.getId());
                return null;
            }
            return response;
        } catch (ResourceAccessException e) {
            log.error("ResourceAccessException: Could not connect to the notification service. " +
                    "This is expected if the service is unavailable (e.g., fake URL for requirements). " +
                    "Payment ID: {}", payment.getId(), e);
            return null;
        } catch (Exception e) {
            log.error(NOTIFICATION_ERROR_MESSAGE, payment.getId(), e);
            return null;
        }
    }

    @Transactional
    private void saveNotificationResult(Payment payment, boolean success) {
        payment.setNotified(success);
        payment.setNotificationTime(LocalDateTime.now());
        paymentRepository.save(payment);
        log.info(NOTIFICATION_RESULT_SAVED_MESSAGE, payment.getId(), success);
    }
}