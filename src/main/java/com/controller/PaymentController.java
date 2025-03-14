package com.controller;

import com.dto.PaymentRequestDTO;
import com.dto.PaymentResponseDTO;
import com.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @RequestBody PaymentRequestDTO request) {

        // Process payment immediately
        PaymentResponseDTO response = paymentService.createPayment(request);

        //TODO: print validation error, that client be aware of mandatory field
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponseDTO> cancelPayment(
            @PathVariable Long paymentId) {

        PaymentResponseDTO response = paymentService.cancelPayment(paymentId);

        //TODO: print validation error, in case when cancellation was done a day after
        return ResponseEntity.ok(response);
    }
}