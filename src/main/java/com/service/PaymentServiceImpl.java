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

        LocalDateTime creationTime = LocalDateTime.now();

        Payment payment = PaymentUtils.determineAndCreatePaymentEntity(request);

        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setDebtorIban(request.getDebtorIban());
        payment.setCreditorIban(request.getCreditorIban());
        payment.setCreationTime(creationTime);
        payment.setCancelled(false);

        payment = paymentRepository.save(payment);
        log.info("Payment created with ID: {}, type: {}", payment.getId(), payment.getClass().getSimpleName());

        if (payment instanceof Type1Payment || payment instanceof Type2Payment) {
            PaymentNotificationDTO notificationResult = notificationService.notifyPaymentCreated(payment);
            log.info("Notification for payment ID: {} was {}",
                    payment.getId(), notificationResult.isNotified() ? "successful" : "unsuccessful");
        }

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

        if (now.isAfter(endOfCreationDay)) {
            throw new IllegalStateException("Payment cannot be canceled after the day of creation");
        }

        BigDecimal cancellationFee = PaymentUtils.calculateCancellationFee(payment);
        payment.setCancellationFee(cancellationFee);
        payment.setCancelled(true);

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

        BigDecimal cancellationFee = PaymentUtils.calculateCancellationFee(payment);
        PaymentFeeResponseDTO responseDTO = new PaymentFeeResponseDTO(payment.getId(), cancellationFee);

        log.info("Retrieved payment ID and cancellation fee for ID: {}", paymentId);

        return responseDTO;
    }
}