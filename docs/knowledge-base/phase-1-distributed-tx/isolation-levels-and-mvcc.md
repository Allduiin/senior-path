# Isolation levels, MVCC & the lost update

**Maps to:** Q2 (isolation levels & MVCC; optimistic vs pessimistic locking) · **Phase 1** · Exercise: `p1-02` (lost-update, to be re-scaffolded)
[← back to index](../README.md) · Related: [[spring-proxy-and-transactions]]

## TL;DR
An isolation level is **the list of anomalies it forbids**, not a strength dial. ANSI defines
levels by *locking* phenomena, but real engines use **MVCC**, so the **same level name behaves
differently** across PostgreSQL and InnoDB. The **lost update** — two txns read `X`, both write
`f(X)`, one write silently overwritten — is the anomaly ANSI *forgot*, and the one that bites
payments. PostgreSQL's default **READ COMMITTED does not stop it** for an application-level
read-modify-write. You fix it at the **row** (optimistic `@Version`, pessimistic `FOR UPDATE`,
or a single atomic `UPDATE`), **never** by globally escalating isolation.

## The anomaly catalogue
ANSI named three preventable phenomena; the real taxonomy (Berenson et al. 1995, *"A Critique of
ANSI SQL Isolation Levels"*) is six.

| Anomaly | What happens | In ANSI? |
|---|---|:--:|
| Dirty write | T2 overwrites T1's *uncommitted* write | no |
| Dirty read | T2 reads T1's *uncommitted* write | yes |
| Non-repeatable read | a row read twice differs (committed change between) | yes |
| Phantom | a range query re-run returns a different *set* (committed insert/delete) | yes |
| **Lost update** | both read `X`, both write `f(X)`; one overwritten | **no — the gap** |
| Read skew / **write skew** | multi-row reads/writes break an invariant that holds per-row | no |

ANSI levels by what they forbid:

| Level | Dirty read | Non-repeatable | Phantom | Lost update (app RMW) |
|---|:--:|:--:|:--:|:--:|
| READ UNCOMMITTED | allowed | allowed | allowed | allowed |
| READ COMMITTED | ✗ | allowed | allowed | **allowed** |
| REPEATABLE READ | ✗ | ✗ | allowed | spec-ambiguous |
| SERIALIZABLE | ✗ | ✗ | ✗ | ✗ |

## Why READ COMMITTED loses the update
RC makes exactly one promise: **each statement sees a fresh snapshot of committed data**. It does
*not* promise the row is unchanged between your read and your write. An **application-level
read-modify-write** is the trap:

```
T1 SELECT balance → 1000      ┐ both see committed 1000
T2 SELECT balance → 1000      ┘
T1 UPDATE balance = 999 commit
T2 UPDATE balance = 999 commit   ← value derived from a STALE read; T1's −1 lost
```

RC kept its promise (each read saw committed data); it never promised repeatability.
**Last-writer-wins.**

The pivotal distinction — *how* you write decides everything, even at unchanged RC:

| Write shape | Lost update at RC? | Why |
|---|:--:|---|
| `SET balance = :appValue` (blind write) | **YES** | value derived from a stale snapshot; DB just stores it |
| `SET balance = balance - :amt` (atomic delta) | **NO** | row lock + **re-read of `balance` at write time** (current read, not the snapshot) |

A write in MVCC reads the **latest committed row version under a row lock** — "snapshot for reads,
**current read** for the write." That is why the atomic delta is safe at plain RC.

## PG vs InnoDB REPEATABLE READ — same name, opposite failure mode
| | **PostgreSQL RR** = Snapshot Isolation | **InnoDB RR** = MVCC reads + next-key locks |
|---|---|---|
| Snapshot | **one per transaction** (taken at first query) | one per-txn snapshot for plain `SELECT` |
| Write-write conflict | **first-updater-wins**: loser **aborts** `40001` | **current read** + next-key locks; loser **blocks** then proceeds |
| Lost update (blind RMW) | **prevented by abort** → app must **retry** | **still possible** (blocked write applies stale value) |
| Phantoms | gone (snapshot) | gone (gap/next-key locks) — *stronger* than ANSI RR |
| Write skew | **still allowed** (needs SERIALIZABLE / SSI) | allowed |

> **One-liner:** PG RR **aborts** the loser (retry `40001`); InnoDB RR **blocks** the loser
> (serialize). Neither makes a blind app-level RMW safe — that needs an atomic write, a row lock
> taken *before* the window, or a version check. PG `SERIALIZABLE` = **SSI** (predicate/SIRead
> locks) and is the only level that also stops **write skew**.

## The three fixes (p1-02)
| Fix | Mechanism | Lock in window | Stops it because | Default when |
|---|---|:--:|---|---|
| **(a) Optimistic** `@Version` | `UPDATE … WHERE id=? AND version=?`; 0 rows → `OptimisticLockingFailureException` → **retry whole RMW** | none | version mismatch detects the concurrent write | **low contention**, long think-time |
| **(b) Pessimistic** `FOR UPDATE` | `SELECT … FOR UPDATE` (`PESSIMISTIC_WRITE`) locks the row **before** the window | whole window | writers can't read-to-modify until commit | **high contention**, hot row |
| **(c) Atomic** | one `UPDATE … SET balance = balance - :amt WHERE id=:id` | one statement | DB row lock + re-read at write time | pure delta; **highest throughput** |

Tradeoffs: **(a)** retry storms under contention, zero lock-hold (needs bounded attempts +
jittered backoff); **(b)** no wasted work but serializes throughput + **deadlock** risk (consistent
lock ordering) + lock-wait timeouts; **(c)** fastest, but you lose the in-app value — any
limit/fraud check must move **into the `WHERE`** (`… AND balance >= :amt`) or be re-validated
under the lock.

## Isolation ≠ invariant
Locking makes the **counter** correct; it does **not** enforce a **business rule**. A no-overdraft
rule (`balance ≥ 0`) must be checked **inside** the locked/atomic path (`… WHERE id=:id AND
balance >= :amt`, 0 rows → reject). Check it *before* the lock and you've rebuilt the race — for
the invariant this time.

## Cold-recall checklist
- [ ] Which anomaly did ANSI miss, and why does RC allow it for an app-level RMW?
- [ ] Why does `SET x = x - :n` survive RC but `SET x = :appValue` does not? (current read vs snapshot)
- [ ] PG RR vs InnoDB RR on a write-write conflict: **abort** vs **block** — and what the app must do in each.
- [ ] Which level stops **write skew**, and by what mechanism (SSI)?
- [ ] Optimistic vs pessimistic vs atomic: contention profile, retry storms, deadlock risk, right default.
- [ ] Why isolation alone can't enforce `balance ≥ 0`.
