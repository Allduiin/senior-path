package com.seniorpath.isolation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * A money wallet. [balance] is held in MINOR UNITS (cents) as a Long — never a floating-point
 * type — per payments convention.
 *
 * NOTE ON CONCURRENCY CONTROL (targets diagnostic Q2)
 * ---------------------------------------------------
 * This entity intentionally has NO optimistic-lock version column. The shipped withdraw path
 * is a naive read-modify-write, so under PostgreSQL's default READ COMMITTED isolation,
 * concurrent withdrawals silently LOSE UPDATES (last-writer-wins on the row).
 *
 * If you choose the OPTIMISTIC fix, add a version field here:
 *
 *     @Version
 *     @Column(nullable = false)
 *     val version: Long = 0
 *
 * and let Hibernate enforce `WHERE id = ? AND version = ?` on UPDATE (0 rows ⇒
 * OptimisticLockingFailureException ⇒ you retry the whole read-modify-write). The PESSIMISTIC
 * and single-statement ATOMIC fixes do NOT need a version column — see SPEC.md.
 */
@Entity
@Table(name = "wallets")
class Wallet(
    @Column(nullable = false)
    var balance: Long,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)
