package com.seniorpath.outbox.repository

import com.seniorpath.outbox.entity.PayoutEntry
import org.springframework.data.jpa.repository.JpaRepository

interface PayoutLedgerRepository : JpaRepository<PayoutEntry, Long> {
    fun findByOrderId(orderId: String): List<PayoutEntry>
}
