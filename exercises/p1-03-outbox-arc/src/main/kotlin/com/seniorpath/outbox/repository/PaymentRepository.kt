package com.seniorpath.outbox.repository

import com.seniorpath.outbox.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun existsByOrderId(orderId: String): Boolean
}