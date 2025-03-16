package com.service;

import com.dto.PaymentFeeResponseDTO;
import com.dto.PaymentNotificationDTO;
import com.dto.PaymentRequestDTO;
import com.dto.PaymentResponseDTO;
import com.model.Payment;
import com.model.Type1Payment;
import com.model.Type2Payment;
import com.model.Type3Payment;
import com.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    public PaymentServiceImpl(ModelMapper modelMapper,
                              PaymentRepository paymentRepository,
                              NotificationService notificationService) {
        this.modelMapper = modelMapper;
        this.paymentRepository =  paymentRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        validatePaymentRequest(request);

        LocalDateTime creationTime = LocalDateTime.now();

        Payment payment = determineAndCreatePaymentEntity(request);

        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setDebtorIban(request.getDebtorIban());
        payment.setCreditorIban(request.getCreditorIban());
        payment.setCreationTime(creationTime);
        payment.setCancelled(false);

        payment = paymentRepository.save(payment);
        log.info("Payment created with ID: {}, type: {}", payment.getId(), payment.getClass().getSimpleName());

        // Send notification for TYPE1 and TYPE2 payments
        if (payment instanceof Type1Payment || payment instanceof Type2Payment) {
            PaymentNotificationDTO notificationResult = notificationService.notifyPaymentCreated(payment);
            log.info("Notification for payment ID: {} was {}",
                    payment.getId(), notificationResult.isNotified() ? "successful" : "unsuccessful");
        }

        // Fetch the updated payment with notification info
        payment = paymentRepository.findById(payment.getId()).orElse(payment);

        return modelMapper.map(payment, PaymentResponseDTO.class);
    }

    @Override
    @Transactional
    public PaymentResponseDTO cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfCreationDay = payment.getCreationTime().toLocalDate().atTime(23, 59, 59);

        // Check if payment can be canceled (must be same day before midnight)
        if (now.isAfter(endOfCreationDay)) {
            throw new IllegalStateException("Payment cannot be canceled after the day of creation");
        }

        BigDecimal cancellationFee = calculateCancellationFee(payment);
        payment.setCancellationFee(cancellationFee);
        payment.setCancelled(true);
        // TODO: add this to the response DTO?
        //payment.setCancellationTime(now);

        // Save payment with cancellation details
        payment = paymentRepository.save(payment);
        log.info("Payment cancelled with ID: {}, cancellation fee: {} EUR", payment.getId(), cancellationFee);

        return modelMapper.map(payment, PaymentResponseDTO.class);
    }

    @Override
    public List<Long> getNonCancelledPaymentIdsByAmount(BigDecimal amount) {
        return paymentRepository.findNonCancelledPaymentIdsByAmount(amount);
    }

    @Override
    public List<Long> getAllNonCancelledPaymentIds() {
        return paymentRepository.findAllNonCancelledPaymentIds();
    }

    @Override
    public PaymentFeeResponseDTO getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

        BigDecimal cancellationFee = calculateCancellationFee(payment);
        PaymentFeeResponseDTO responseDTO = new PaymentFeeResponseDTO(payment.getId(), cancellationFee);

        log.info("Retrieved payment ID and cancellation fee for ID: {}", paymentId);

        return responseDTO;
    }

    //TODO: think about moving to another class
    private BigDecimal calculateCancellationFee(Payment payment) {

        // Calculate hours since creation (full hours only)
        long hours = payment.getCreationTime().until(LocalDateTime.now(), ChronoUnit.HOURS);

        // Determine coefficient based on payment type
        double coefficient;
        if (payment instanceof Type1Payment) {
            coefficient = 0.05;
        } else if (payment instanceof Type2Payment) {
            coefficient = 0.1;
        } else if (payment instanceof Type3Payment) {
            coefficient = 0.15;
        } else {
            throw new IllegalStateException("Unknown payment type");
        }

        BigDecimal fee = BigDecimal.valueOf(hours * coefficient);

        if ("USD".equals(payment.getCurrency())) {
            fee = convertUsdToEur(fee);
        }

        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal convertUsdToEur(BigDecimal usdAmount) {

        // Fixed rate: 1 USD = 0.92 EUR
        BigDecimal exchangeRate = BigDecimal.valueOf(0.92);

        return usdAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }

    private void validatePaymentRequest(PaymentRequestDTO request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (!"EUR".equals(request.getCurrency()) && !"USD".equals(request.getCurrency())) {
            throw new IllegalArgumentException("Currency must be EUR or USD");
        }

        if (request.getDebtorIban() == null || request.getDebtorIban().isBlank()) {
            throw new IllegalArgumentException("Debtor IBAN is required");
        }

        if (request.getCreditorIban() == null || request.getCreditorIban().isBlank()) {
            throw new IllegalArgumentException("Creditor IBAN is required");
        }

        // TYPE1 validations (EUR with details)
        if ("EUR".equals(request.getCurrency()) &&
                (request.getCreditorBic() == null || request.getCreditorBic().isBlank())) {
            if (request.getDetails() == null || request.getDetails().isBlank()) {
                throw new IllegalArgumentException("Details are required for TYPE1 (EUR) payments");
            }
        }

    }

    /**
     * Determines and creates the appropriate payment type entity based on request data.
     *
     * @param request The payment request data
     * @return The appropriate payment entity (Type1Payment, Type2Payment, or Type3Payment)
     */
    private Payment determineAndCreatePaymentEntity(PaymentRequestDTO request) {
        // First check if this is a TYPE3 payment (has BIC code)
        if (request.getCreditorBic() != null && !request.getCreditorBic().isBlank()) {
            // TYPE3 payment (both EUR and USD are supported)
            Type3Payment payment = new Type3Payment();
            payment.setCreditorBic(request.getCreditorBic());
            return payment;
        }

        // If not TYPE3, determine based on currency
        switch (request.getCurrency()) {
            case "EUR":
                // TYPE1 - EUR with mandatory details
                Type1Payment type1Payment = new Type1Payment();
                type1Payment.setDetails(request.getDetails());
                return type1Payment;

            case "USD":
                // TYPE2 - USD with optional details
                Type2Payment type2Payment = new Type2Payment();
                if (request.getDetails() != null && !request.getDetails().isBlank()) {
                    type2Payment.setDetails(request.getDetails());
                }
                return type2Payment;

            default:
                throw new IllegalArgumentException("Unsupported currency: " + request.getCurrency());
        }
    }
}