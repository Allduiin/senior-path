package com.seniorpath.isolation

import org.springframework.data.jpa.repository.JpaRepository

/**
 * Plain CRUD repository.
 *
 * If you take the PESSIMISTIC approach, you will add a locking finder here, e.g.:
 *
 *     @Lock(LockModeType.PESSIMISTIC_WRITE)
 *     @Query("select w from Wallet w where w.id = :id")
 *     fun findByIdForUpdate(id: Long): Wallet?
 *
 * which emits `SELECT ... FOR UPDATE` and serialises concurrent writers on the row.
 *
 * If you take the single-statement ATOMIC approach, you will add a modifying query, e.g.:
 *
 *     @Modifying
 *     @Query("update Wallet w set w.balance = w.balance - :amount where w.id = :id")
 *     fun decrement(id: Long, amount: Long): Int
 *
 * letting the database apply the delta under its own row lock (no read-modify-write window).
 */
interface WalletRepository : JpaRepository<Wallet, Long>
