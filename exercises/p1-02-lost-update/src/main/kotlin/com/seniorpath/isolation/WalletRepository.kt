package com.seniorpath.isolation

import org.springframework.data.jpa.repository.JpaRepository

/**
 * Plain CRUD repository.
 *
 * If you take the PESSIMISTIC fix, add a locking finder, e.g.:
 *
 *     @Lock(LockModeType.PESSIMISTIC_WRITE)
 *     @Query("select w from Wallet w where w.id = :id")
 *     fun findByIdForUpdate(id: Long): Wallet?
 *
 * which emits `SELECT ... FOR UPDATE` and serialises concurrent writers on the row BEFORE the
 * read-modify-write window opens.
 *
 * If you take the single-statement ATOMIC fix, add a modifying query, e.g.:
 *
 *     @Modifying
 *     @Query("update Wallet w set w.balance = w.balance - :amount where w.id = :id")
 *     fun decrement(id: Long, amount: Long): Int
 *
 * letting the database apply the delta under its own row lock (no application-level read window).
 */
interface WalletRepository : JpaRepository<Wallet, Long>
