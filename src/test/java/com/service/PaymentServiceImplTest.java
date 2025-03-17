package com.service;


import com.dto.PaymentFeeResponseDTO;
import com.dto.PaymentNotificationDTO;
import com.dto.PaymentRequestDTO;
import com.dto.PaymentResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.model.Currency;
import com.model.Payment;
import com.model.Type1Payment;
import com.repository.PaymentRepository;
import com.utils.PaymentUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequestDTO paymentRequestDTO;
    private Payment payment;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        paymentRequestDTO = new PaymentRequestDTO();
        paymentRequestDTO.setAmount(new BigDecimal("100.00"));
        paymentRequestDTO.setCurrency(Currency.EUR);
        paymentRequestDTO.setDebtorIban("DE123456789");
        paymentRequestDTO.setCreditorIban("DE987654321");
        paymentRequestDTO.setDetails("Valid payment details");

        payment = new Type1Payment();
        payment.setId(1L);
        payment.setAmount(paymentRequestDTO.getAmount());
        payment.setCurrency(paymentRequestDTO.getCurrency());
        payment.setDebtorIban(paymentRequestDTO.getDebtorIban());
        payment.setCreditorIban(paymentRequestDTO.getCreditorIban());
        payment.setCreationTime(LocalDateTime.now());
        payment.setCancelled(false);
    }

    @Test
    void createPayment_Success() throws IOException {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(modelMapper.map(any(Payment.class), eq(PaymentResponseDTO.class)))
                .thenReturn(loadJson("src/test/resources/responses/createPayment_success.json", PaymentResponseDTO.class));

        PaymentNotificationDTO notificationDTO = mock(PaymentNotificationDTO.class);
        when(notificationDTO.isNotified()).thenReturn(true);

        when(notificationService.notifyPaymentCreated(any(Payment.class)))
                .thenReturn(notificationDTO);

        PaymentResponseDTO response = paymentService.createPayment(paymentRequestDTO);

        assertNotNull(response, "Response should not be null");
        verify(paymentRepository, times(1)).save(any(Payment.class)); // Verify save method was called
        verify(notificationService, times(1)).notifyPaymentCreated(any(Payment.class)); // Verify notify method was called
    }

    @Test
    void cancelPayment_Success() throws IOException {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(modelMapper.map(any(Payment.class), eq(PaymentResponseDTO.class)))
                .thenReturn(loadJson("src/test/resources/responses/cancelPayment_success.json", PaymentResponseDTO.class));

        PaymentResponseDTO response = paymentService.cancelPayment(1L);

        assertNotNull(response);
        assertTrue(payment.isCancelled());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void cancelPayment_ThrowsException_WhenPaymentNotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.cancelPayment(1L));

        assertEquals("Payment not found with ID: 1", exception.getMessage());
    }

    @Test
    void getPaymentById_Success() {
        try (MockedStatic<PaymentUtils> mockedUtils = mockStatic(PaymentUtils.class)) {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            mockedUtils.when(() -> PaymentUtils.calculateCancellationFee(payment)).thenReturn(BigDecimal.TEN);

            PaymentFeeResponseDTO response = paymentService.getPaymentById(1L);

            assertNotNull(response);
            assertEquals(BigDecimal.TEN, response.getCancellationFee());
        }
    }

    private <T> T loadJson(String path, Class<T> type) throws IOException {
        return objectMapper.readValue(new File(path), type);
    }
}