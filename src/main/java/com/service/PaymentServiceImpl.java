package com.service;

import com.dto.PaymentFeeResponseDTO;
import com.dto.PaymentNotificationDTO;
import com.dto.PaymentRequestDTO;
import com.dto.PaymentResponseDTO;
import com.model.Payment;
import com.model.Type1Payment;
import com.model.Type2Payment;
import com.repository.PaymentRepository;
import com.utils.PaymentUtils;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    private static final String PAYMENT_CREATED_LOG_MSG = "Payment created with ID: {}, type: {}";
    private static final String PAYMENT_NOTIFICATION_LOG_MSG = "Notification for payment ID: {} was {}";
    private static final String PAYMENT_NOT_FOUND_MSG = "Payment not found with ID: ";
    private static final String PAYMENT_CANCELLATION_TIME_ERROR_MSG = "Payment cannot be canceled after the day of creation";
    private static final String CONCURRENT_UPDATE_ERROR_MSG = "Concurrent update detected for payment ID: ";

    public PaymentServiceImpl(ModelMapper modelMapper,
                              PaymentRepository paymentRepository,
                              NotificationService notificationService) {
        this.modelMapper = modelMapper;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        PaymentUtils.validatePaymentRequest(request);
        Payment payment = createNewPaymentEntity(request);

        payment = paymentRepository.save(payment);
        log.info(PAYMENT_CREATED_LOG_MSG, payment.getId(), payment.getClass().getSimpleName());

        sendPaymentCreatedNotification(payment);

        payment = paymentRepository.findById(payment.getId()).orElse(payment);
        return mapToResponseDTO(payment);
    }

    @Override
    @Transactional
    public PaymentResponseDTO cancelPayment(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        validateCancellationTime(payment);

        BigDecimal cancellationFee = calculateCancellationFee(payment);
        updatePaymentCancellation(payment, cancellationFee);

        payment = savePaymentWithLock(payment);
        return mapToResponseDTO(payment);
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
        Payment payment = findPaymentById(paymentId);
        BigDecimal cancellationFee = calculateCancellationFee(payment);

        PaymentFeeResponseDTO responseDTO = new PaymentFeeResponseDTO(payment.getId(), cancellationFee);
        log.info("Retrieved payment ID and cancellation fee for ID: {}", paymentId);

        return responseDTO;
    }

    private Payment createNewPaymentEntity(PaymentRequestDTO request) {
        LocalDateTime creationTime = LocalDateTime.now();
        Payment payment = PaymentUtils.determineAndCreatePaymentEntity(request);

        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setDebtorIban(request.getDebtorIban());
        payment.setCreditorIban(request.getCreditorIban());
        payment.setCreationTime(creationTime);
        payment.setCancelled(false);

        return payment;
    }

    private void sendPaymentCreatedNotification(Payment payment) {
        if (payment instanceof Type1Payment || payment instanceof Type2Payment) {
            PaymentNotificationDTO notificationResult = notificationService.notifyPaymentCreated(payment);
            log.info(PAYMENT_NOTIFICATION_LOG_MSG, payment.getId(), notificationResult.isNotified() ? "successful" : "unsuccessful");
        }
    }

    private Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(PAYMENT_NOT_FOUND_MSG + paymentId));
    }

    private void validateCancellationTime(Payment payment) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfCreationDay = payment.getCreationTime().toLocalDate().atTime(23, 59, 59);

        if (now.isAfter(endOfCreationDay)) {
            throw new IllegalStateException(PAYMENT_CANCELLATION_TIME_ERROR_MSG);
        }
    }

    private BigDecimal calculateCancellationFee(Payment payment) {
        return PaymentUtils.calculateCancellationFee(payment);
    }

    private void updatePaymentCancellation(Payment payment, BigDecimal cancellationFee) {
        payment.setCancellationFee(cancellationFee);
        payment.setCancelled(true);
    }

    private Payment savePaymentWithLock(Payment payment) {
        try {
            return paymentRepository.save(payment);
        } catch (OptimisticLockException e) {
            throw new IllegalStateException(CONCURRENT_UPDATE_ERROR_MSG + payment.getId(), e);
        }
    }

    private PaymentResponseDTO mapToResponseDTO(Payment payment) {
        return modelMapper.map(payment, PaymentResponseDTO.class);
    }
}