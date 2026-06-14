package com.seniorpath.tx

import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<PaymentEntity, Long>

interface AuditRepository : JpaRepository<AuditEntity, Long>
