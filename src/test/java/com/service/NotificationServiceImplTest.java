package com.service;

import com.dto.PaymentNotificationDTO;
import com.model.Payment;
import com.model.Type1Payment;
import com.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

class NotificationServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNotifyPaymentCreated_SkipsNotificationForOtherPayments() {
        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(3L);

        PaymentNotificationDTO notificationDTO = notificationService.notifyPaymentCreated(payment);

        assertEquals(3L, notificationDTO.getPaymentId());
        assertFalse(notificationDTO.isNotified(), "Notification should be skipped for this payment type");
        assertNull(notificationDTO.getStatusCode(), "Status code should be null for skipped notifications");

        verify(paymentRepository, never()).save(any(Payment.class));  // Ensure save is NOT called
    }

    @Test
    void testNotifyPaymentCreated_Failure_Type1Payment() {
        Type1Payment payment = mock(Type1Payment.class);
        when(payment.getId()).thenReturn(1L);

        ResponseEntity<String> responseEntity = ResponseEntity.status(500).body("Notification failed");
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);

        PaymentNotificationDTO notificationDTO = notificationService.notifyPaymentCreated(payment);

        assertEquals(1L, notificationDTO.getPaymentId());
        assertFalse(notificationDTO.isNotified(), "Notification should be marked as false due to failure");

        verify(paymentRepository, times(1)).save(payment);  // Ensure save is called
    }
}