package com.controller;

import com.dto.PaymentFeeResponseDTO;
import com.dto.PaymentRequestDTO;
import com.dto.PaymentResponseDTO;
import com.service.CountryResolverService;
import com.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentService paymentService;

    @Mock
    private CountryResolverService countryResolverService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private PaymentRequestDTO paymentRequest;

    @Mock
    private PaymentResponseDTO paymentResponse;

    @Mock
    private PaymentFeeResponseDTO paymentFeeResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
    }

    @Test
    void testCreatePayment() {
        when(paymentService.createPayment(paymentRequest)).thenReturn(paymentResponse);
        ResponseEntity<PaymentResponseDTO> response = paymentController.createPayment(paymentRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertNotNull(response.getBody());
        verify(paymentService).createPayment(paymentRequest);
    }

    @Test
    void testCancelPayment() {
        Long paymentId = 1L;
        when(paymentService.cancelPayment(paymentId)).thenReturn(paymentResponse);
        ResponseEntity<PaymentResponseDTO> response = paymentController.cancelPayment(paymentId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        verify(paymentService).cancelPayment(paymentId);
    }

    @Test
    void testGetNonCancelledPayments_WithAmount() {
        BigDecimal amount = new BigDecimal("100.00");
        List<Long> paymentIds = Arrays.asList(1L, 2L, 3L);
        when(paymentService.getNonCancelledPaymentIdsByAmount(amount)).thenReturn(paymentIds);
        ResponseEntity<List<Long>> response = paymentController.getNonCancelledPayments(amount, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        verify(paymentService).getNonCancelledPaymentIdsByAmount(amount);
    }

    @Test
    void testGetNonCancelledPayments_WithoutAmount() {
        List<Long> paymentIds = Arrays.asList(1L, 2L, 3L);
        when(paymentService.getAllNonCancelledPaymentIds()).thenReturn(paymentIds);
        ResponseEntity<List<Long>> response = paymentController.getNonCancelledPayments(null, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        verify(paymentService).getAllNonCancelledPaymentIds();
    }

    @Test
    void testGetPaymentById() {
        Long paymentId = 1L;
        when(paymentService.getPaymentById(paymentId)).thenReturn(paymentFeeResponseDTO);
        ResponseEntity<PaymentFeeResponseDTO> response = paymentController.getPaymentById(paymentId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        verify(paymentService).getPaymentById(paymentId);
    }

    @Test
    void testGetNonCancelledPayments_ClientIpLogging() {
        String clientIp = "192.168.1.1";
        when(request.getRemoteAddr()).thenReturn(clientIp);
        List<Long> paymentIds = Arrays.asList(1L, 2L, 3L);
        when(paymentService.getAllNonCancelledPaymentIds()).thenReturn(paymentIds);
        paymentController.getNonCancelledPayments(null, request);
        verify(countryResolverService).logClientCountry(clientIp);
    }
}