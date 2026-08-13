package com.seniorpath.outbox.repository

import com.seniorpath.outbox.entity.NotificationLog
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationLogRepository : JpaRepository<NotificationLog, Long> {
    fun findByOrderId(orderId: String): List<NotificationLog>
}
