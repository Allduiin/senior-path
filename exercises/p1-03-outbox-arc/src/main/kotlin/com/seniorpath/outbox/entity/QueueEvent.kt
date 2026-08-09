package com.seniorpath.outbox.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "queue_events")
class QueueEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val exchange: String,
    val routingKey: String,
    val message: String,
    var sentAt: LocalDateTime? = null,
    var createdAt: LocalDateTime? = LocalDateTime.now()
)