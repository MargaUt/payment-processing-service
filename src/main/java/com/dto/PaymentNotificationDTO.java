package com.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotificationDTO {
    private Long paymentId;
    private String paymentType;
    private boolean notified;
    private LocalDateTime notificationTime;
    private Integer statusCode;
}
