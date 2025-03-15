package com.repository;

import com.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p.id FROM Payment p WHERE p.isCancelled = false AND p.amount = :amount")
    List<Long> findNonCancelledPaymentIdsByAmount(@Param("amount") BigDecimal amount);

    @Query("SELECT p.id FROM Payment p WHERE p.isCancelled = false")
    List<Long> findAllNonCancelledPaymentIds();

}
