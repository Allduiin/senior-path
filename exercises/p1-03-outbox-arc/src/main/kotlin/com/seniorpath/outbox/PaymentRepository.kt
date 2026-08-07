package com.seniorpath.outbox

import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun existsByOrderId(orderId: String): Boolean
}
