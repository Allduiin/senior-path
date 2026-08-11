package com.seniorpath.outbox.repository

import com.seniorpath.outbox.entity.ProcessedEvent
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedEventRepository: JpaRepository<ProcessedEvent, String>