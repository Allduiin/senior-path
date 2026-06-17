# Isolation levels, MVCC & the lost update

**Maps to:** Q2 (isolation levels & MVCC; optimistic vs pessimistic locking) · **Phase 1** · Exercise: `p1-02-lost-update`
[← back to index](../README.md) · Related: [[spring-proxy-and-transactions]]

## TL;DR
An isolation level is **a list of anomalies it forbids**, not a feature. The ANSI standard
defines levels by *locking* phenomena, but real engines use **MVCC**, so the **same level name
behaves differently** across PostgreSQL and InnoDB. The **lost update** — two transactions read
`X`, both write a value derived from `X`, one write silently overwritten — is the anomaly ANSI
*forgot* and the one that bites payments. PostgreSQL's default **READ COMMITTED does not stop it**
for an application-level read-modify-write; you fix it at the **row** (optimistic version,
pessimistic `FOR UPDATE`, or a single atomic `UPDATE`), never by globally escalating isolation.

## The anomaly catalogue
ANSI named three; the real taxonomy (Berenson et al. 1995) is six.

| Anomaly | What happens | ANSI? |
|---|---|:--:|
| Dirty write | T2 overwrites T1's *uncommitted* write | no |
| Dirty read | T2 reads T1's *uncommitted* write | yes |
| Non-repeatable read | row read twice differs (committed change between) | yes |
| Phantom | range query re-run returns a different *set* (committed insert/delete) | yes |
| **Lost update** | both read `X`, both write `f(X)`; one overwritten | **no — the gap** |
| Read skew / **write skew** | multi-row reads/writes break an invariant that holds per-row | no |

ANSI levels by what they forbid:

| Level | Dirty read | Non-repeatable | Phantom | Lost update |
|---|:--:|:--:|:--:|:--:|
| READ UNCOMMITTED | ✓ allowed | ✓ | ✓ | ✓ |
| READ COMMITTED | ✗ | ✓ | ✓ | **✓ allowed** |
| REPEATABLE READ | ✗ | ✗ | ✓ | spec-ambiguous |
| SERIALIZABLE | ✗ | ✗ | ✗ | ✗ |

## Why READ COMMITTED loses the update
RC gives **a fresh snapshot per statement** of committed data — and *no* promise the row is
unchanged between your read and your write. An **application-level read-modify-write** is the trap:

```
T1 SELECT balance → 1000      ┐ both see committed 1000
T2 SELECT balance → 1000      ┘
T1 UPDATE balance = 999 commit
T2 UPDATE balance = 999 commit   ← writes value from a STALE read; T1's −1 lost
```

RC kept its promise (each read saw committed data); it never promised repeatability. **Last-writer-wins.**

The pivotal distinction — *how* you write decides everything, even at RC:

| Write shape | Lost update at RC? | Why |
|---|:--:|---|
| `SET balance = :appValue` (blind write) | **YES** | value derived from a stale snapshot |
| `SET balance = balance - :amt` (atomic delta) | **NO** | row lock + **re-read of `balance` at write time** |

## PG vs InnoDB REPEATABLE READ — same name, opposite failure mode
| | **PostgreSQL RR** = Snapshot Isolation | **InnoDB RR** = MVCC reads + next-key locks |
|---|---|---|
| Snapshot | **one per transaction** (first query) | one per transaction for plain `SELECT` |
| Writes | **first-updater-wins**: write-write conflict → abort `40001` | **current read** + next-key locks; **blocks** the loser |
| Lost update (blind RMW) | **prevented by abort** → app must **retry** | **still possible** (blocked write applies stale value) |
| Phantoms | gone (snapshot) | gone (gap locks) — stronger than ANSI RR |
| Write skew | **still allowed** (needs SERIALIZABLE / SSI) | allowed |

> **One-liner:** PG RR **aborts** the loser (retry `40001`); InnoDB RR **blocks** the loser
> (serialize). Neither makes a blind app-level RMW safe — that needs an atomic write, a row lock
> taken *before* the window, or a version check. PG SERIALIZABLE = **SSI** (predicate/SIRead
> locks) and is the only level that also stops **write skew**.

## The three fixes (p1-02)
| Fix | Mechanism | Lock held | Stops it because | Default when |
|---|---|:--:|---|---|
| **(a) Optimistic** `@Version` | `UPDATE … WHERE id=? AND version=?`; 0 rows → `OptimisticLockingFailureException` → **retry whole RMW** | none in window | version mismatch detects the concurrent write | **low contention**, long think-time |
| **(b) Pessimistic** `FOR UPDATE` | `SELECT … FOR UPDATE` (`PESSIMISTIC_WRITE`) locks the row **before** the window | whole window | writers can't read-to-modify until commit | **high contention**, hot row |
| **(c) Atomic** | one `UPDATE … SET balance = balance - :amt WHERE id=:id` | one statement | DB row lock + re-read at write time | pure delta; **highest throughput** |

Tradeoffs: **(a)** retry storms under contention, zero lock-hold; **(b)** no wasted work but
serializes throughput + **deadlock** risk (lock ordering) + lock-wait timeouts; **(c)** fastest,
but you lose the in-app value — any limit/fraud check must move **into the `WHERE`**
(`… AND balance >= :amt`) or be re-validated under the lock.

## Isolation ≠ invariant
Locking makes the **counter** correct; it does **not** enforce a **business rule**. A no-overdraft
rule (`balance ≥ 0`) must be checked **inside** the locked/atomic path (`… WHERE id=:id AND
balance >= :amt`, 0 rows → reject). Check it *before* the lock and you've rebuilt the race for the
invariant.

## Cold-recall checklist
- [ ] Which anomaly did ANSI miss, and why does RC allow it for an app-level RMW?
- [ ] Why does `SET x = x - :n` survive RC but `SET x = :appValue` does not?
- [ ] PG RR vs InnoDB RR on a write-write conflict: **abort** vs **block** — and what the app must do in each.
- [ ] Which level stops **write skew**, and what mechanism (SSI)?
- [ ] Optimistic vs pessimistic vs atomic: contention profile, retry storms, deadlock risk, right default.
- [ ] Why isolation alone can't enforce `balance ≥ 0`.
