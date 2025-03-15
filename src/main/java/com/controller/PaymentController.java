package com.controller;

import com.dto.PaymentFeeResponseDTO;
import com.dto.PaymentRequestDTO;
import com.dto.PaymentResponseDTO;
import com.exeption.ErrorResponse;
import com.exeption.InvalidPaymentDataException;
import com.service.CountryResolverService;
import com.service.PaymentService;
import com.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CountryResolverService countryResolverService;


    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO request) {
        log.info("Creating payment: {}", request);
        try {
            PaymentResponseDTO response = paymentService.createPayment(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException | InvalidPaymentDataException e) {
            log.error("Validation error when creating payment: {}", e.getMessage());
            throw new InvalidPaymentDataException(e.getMessage());
        }
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponseDTO> cancelPayment(
            @PathVariable Long paymentId) {

        PaymentResponseDTO response = paymentService.cancelPayment(paymentId);

        //TODO: print validation error, in case when cancellation was done a day after
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
        try {
            PaymentFeeResponseDTO responseDTO = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching payment with ID {}: {}", paymentId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(InvalidPaymentDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentDataException(InvalidPaymentDataException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}