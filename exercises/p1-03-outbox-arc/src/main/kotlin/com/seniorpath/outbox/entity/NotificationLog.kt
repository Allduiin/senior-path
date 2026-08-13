package com.seniorpath.outbox.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "notification_log")
class NotificationLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val orderId: String,

    @Column(nullable = false)
    val amountMinor: Long,

    @Column(nullable = false)
    val notifiedAt: Instant = Instant.now(),
)
