package com.seniorpath.outbox.repository

import com.seniorpath.outbox.entity.QueueEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface QueueEventRepository: JpaRepository<QueueEvent, Long> {
    @Query("select e from QueueEvent e where e.sentAt is null order by e.createdAt")
    fun findUnsent(): List<QueueEvent>
}