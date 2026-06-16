package com.seniorpath.tx

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class AuditService(
    private val auditRepository: AuditRepository,
) {
    /**
     * Intended to run in its own transaction so this write rolls back independently when it
     * throws. The annotation is correct; the CALL PATH in [processPayment] is what defeats it.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordAuditAttempt(reference: String) {
        auditRepository.save(AuditEntity(reference = reference))
        throw AuditSinkUnavailableException("audit sink unavailable for reference=$reference")
    }
}