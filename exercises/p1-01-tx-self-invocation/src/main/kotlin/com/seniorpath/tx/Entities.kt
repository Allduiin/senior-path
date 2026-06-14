package com.seniorpath.tx

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * A committed unit of business work. In the scenario this row represents the
 * payment we successfully recorded — it MUST survive even when the audit write fails.
 */
@Entity
@Table(name = "payments")
class PaymentEntity(
    @Column(nullable = false, unique = true)
    val reference: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)

/**
 * A best-effort audit record written by a method that is intended to run in its
 * OWN transaction (REQUIRES_NEW) and to throw, so that this row rolls back
 * independently of the surrounding payment transaction.
 */
@Entity
@Table(name = "audit_entries")
class AuditEntity(
    @Column(nullable = false)
    val reference: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)
