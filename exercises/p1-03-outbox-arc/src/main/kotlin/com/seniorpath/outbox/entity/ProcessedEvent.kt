package com.seniorpath.outbox.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.domain.Persistable

@Entity
@Table(name = "processed_events")
class ProcessedEvent (
    @Id
    var eventId: String
): Persistable<String> {
    override fun getId(): String = eventId
    override fun isNew(): Boolean = true
}