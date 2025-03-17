package com.controller;

import com.dto.PaymentFeeResponseDTO;
import com.dto.PaymentRequestDTO;
import com.dto.PaymentResponseDTO;
import com.service.CountryResolverService;
import com.service.PaymentService;
import com.utils.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final CountryResolverService countryResolverService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO request) {
        log.info("Creating payment: {}", request);
        PaymentResponseDTO response = paymentService.createPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponseDTO> cancelPayment(@PathVariable Long paymentId) {
        PaymentResponseDTO response = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Long>> getNonCancelledPayments(@RequestParam(required = false) BigDecimal amount,
                                                              HttpServletRequest request) {
        List<Long> paymentIds;

        String clientIp = ClientIpResolver.getClientIpAddress(request);
        countryResolverService.logClientCountry(clientIp);

        if (amount != null) {
            paymentIds = paymentService.getNonCancelledPaymentIdsByAmount(amount);
        } else {
            paymentIds = paymentService.getAllNonCancelledPaymentIds();
        }

        return ResponseEntity.ok(paymentIds);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentFeeResponseDTO> getPaymentById(@PathVariable Long paymentId) {
        log.info("Fetching payment details for ID: {}", paymentId);
        PaymentFeeResponseDTO responseDTO = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(responseDTO);
    }
}