# Idempotent consumption (keys, dedup stores, the failure window)

**Maps to:** Q7 (idempotent consumption) · **Phase 1** · Exercise: shared Q6+Q7+Q8 arc module,
stage B ([p1-03-outbox-arc](../../../exercises/p1-03-outbox-arc/SPEC.md))
[← back to index](../README.md)

## TL;DR
The producer side (→ Q6) is **at-least-once by construction**: the relay's publish and its
mark-published are two resources with independent commit points, so a crash between them
republishes. Reversing them converts *duplicate* into *lost* — strictly worse. The duplicate must
therefore be absorbed by the **consumer**, which is made **idempotent**: applying the same message
twice leaves the same system state as applying it once (`f(f(x)) = f(x)`). The mechanism is a
**dedup key assigned by the producer**, claimed via a **unique constraint** in the **same local
transaction as the side effect**. That composition is what "effectively-once" actually names
(→ Q8).

## Why the consumer is the only place left
```
relay:  publish ──►(crash)──► mark published      two commit points, unclosable
        ▲ crash here ⇒ next pass republishes  ⇒ AT-LEAST-ONCE
broker: deliver ──►(crash)──► ack                 two commit points, unclosable
        ▲ crash here ⇒ broker redelivers      ⇒ AT-LEAST-ONCE
```
Each hop is the Q6 dual-write shape one layer down. Delivery cannot be strengthened; **processing**
can be made insensitive to repetition. That's the whole stance.

## The idempotency key
| Candidate | Verdict | Mechanism |
|---|---|---|
| **Producer-assigned event id** (the outbox row `id`, carried in the payload) | ✅ canonical | Stable across every redelivery — the relay republishes *the same row*, so the same id |
| Business key (`orderId`) | ⚠️ only when one event per aggregate is legal | Correct for `PaymentCaptured`; wrong for `BalanceAdjusted`, where a legitimate second event is eaten |
| AMQP `deliveryTag` / Kafka offset | ❌ | Broker-assigned and **changes on redelivery** — dedups nothing |
| Payload hash | ⚠️ | Collapses two genuinely distinct events that happen to be identical |

**Rule:** the key is **assigned by the producer, at authoring time, and travels in the message.**
Anything the transport assigns is a delivery identifier, not an identity.

## The dedup store — two defects the naïve version has
Naïve: `SELECT key` → side effect → `INSERT key`. Both halves are broken.

### (a) Crash window — check-then-act across two commit points
| Order | Crash between | Result |
|---|---|---|
| record key → side effect | after recording | Key present, side effect never happened. Redelivery skipped as "done" → **silently lost, permanently** (worst outcome) |
| side effect → record key | after side effect | Redelivery re-applies → **duplicate** |
| **both in ONE local tx** | anywhere | Both durable or neither; redelivery retries cleanly ✅ |

**Therefore: the dedup table lives in the same database as the side effect**, and the claim +
side effect share a single commit point. Same fix as the outbox, applied at the consumer.

Corollary — **Redis/Memcached as a dedup store is a trap**: a second resource re-creates
dual-write with extra hops. In-memory `Set` is worse still (dies with the process, invisible to
other instances).

### (b) Race — TOCTOU between concurrent consumers
`SELECT` then `INSERT` is not atomic; two prefetched copies on two threads both see "absent".
The fix is not a lock — it's a constraint:

```sql
INSERT INTO processed_events (event_id) VALUES (:key) ON CONFLICT DO NOTHING
```
**The unique constraint *is* the check.** Rows-affected `0` ⇒ someone else owns the key ⇒ skip.
One atomic statement, no window, correct across instances and processes. Claim the key **first**
in the transaction: the constraint then serializes same-key processing at the DB level, and the
whole tx (claim + side effect) rolls back together on failure.

| Approach | Atomic? | Multi-instance safe? | Note |
|---|:--:|:--:|---|
| `SELECT` then `INSERT` | ❌ | ❌ | TOCTOU race |
| App lock / `synchronized` | ❌ | ❌ | Per-JVM only |
| `SELECT … FOR UPDATE` on key row | ✅ | ✅ | Needs the row to exist; heavier |
| **`INSERT … ON CONFLICT DO NOTHING`** | ✅ | ✅ | Canonical; check rows-affected |

## Natural idempotency — when no dedup table is needed
| Technique | Shape | Idempotent because |
|---|---|---|
| **Guarded state transition** | `UPDATE payment SET status='CAPTURED' WHERE id=:id AND status='AUTHORIZED'` | Second application matches 0 rows; **0 rows is success, not failure** |
| **Upsert to an absolute value** | `INSERT … ON CONFLICT (id) DO UPDATE SET status='CAPTURED'` | Assigns, doesn't accumulate |
| **Optimistic version** | `UPDATE … WHERE version = :expected` | Event carries the version it applies to |
| **Relative mutation** | `UPDATE balance SET amount = amount - 100` | ❌ **never** — the archetypal double-debit |

**Absolute assignment is idempotent; relative accumulation never is.** Money movement is exactly
the relative case, which is why the dedup table survives as the general answer in payments.

## Side effects you cannot put in your transaction
A PSP charge, an email, an SMS: not rollback-able, not committable with your rows.

| Move | Mechanism | Residual risk |
|---|---|---|
| 1. **Push idempotency to the callee** | Send *your* key as the PSP's `Idempotency-Key` header — their dedup becomes yours | Their retention window; their semantics |
| 2. **Record intent before the call** (→ Q9) | `INSERT` the attempt row with the key and **commit before** calling out | Leaves a durable **unknown** state — recoverable by webhook/poll reconciliation |
| 3. Accept + reconcile | Only where the effect is cheap or benign | Duplicate visible downstream |

Honest limit: with a foreign side effect the window never closes — it narrows to a state you
**know you don't know**. Recording intent first is what converts an invisible charge into a
reconcilable one.

## Ack ordering
| Order | Semantics | Use |
|---|---|---|
| ack → then process | **At-most-once** — crash after ack loses the message | Only for disposable telemetry |
| **process + commit → then ack** | **At-least-once** — crash before ack redelivers | Default; the redelivery is absorbed by dedup |

The ack is itself a second resource (crash between commit and ack ⇒ redelivery). That gap is not
closed — it's *the reason the dedup table exists*.

## Scope & retention
- **Idempotency is per consumer, not global.** Two services consuming the same event each need
  their own dedup; one must never mark an event processed on another's behalf. Key on
  `(consumer, event_id)`, or give each service its own table.
- **Retention:** `processed_events` grows without bound. TTL must **exceed the maximum plausible
  redelivery window** (broker retention + DLQ replay + manual reprocessing). Purge older than
  that; purging too eagerly reopens the hole for a late redelivery.
- **Poison messages:** a message that always fails redelivers forever. Bound attempts and route
  to a **DLQ** — the dedup key must not be claimed-and-committed for a message that never
  succeeded (that would be the "lost" row of the crash-window table).

## What it buys / what it doesn't
| Guaranteed | Not guaranteed |
|---|---|
| Duplicate deliveries produce one state change | Exactly-once **delivery** (impossible → Q8) |
| Safe relay/broker retries, safe DLQ replay | Ordering — dedup says nothing about sequence |
| Crash-safe: claim + effect atomic | Idempotency of **foreign** side effects (needs the callee's key) |
| Works with plain Postgres + any broker | Free storage — dedup rows need a TTL purge |

## The composition (bridge to Q8)
> **effectively-once = at-least-once delivery + idempotent processing**

Not a transport guarantee — an end-to-end property assembled from an unreliable transport and a
disciplined consumer. Q8 covers why the delivery half can never be strengthened (Two Generals)
and what the systems advertising "exactly-once" actually implement.

## Self-check (answer cold)
1. Why is the consumer, not the relay or the broker, the place where duplicates get absorbed?
2. Which two orderings of "record key" and "do side effect" fail, and what is each symptom? Which
   is worse and why?
3. Why must the dedup table be in the same DB as the side effect? What breaks with Redis?
4. Why is `SELECT`-then-`INSERT` wrong even inside one transaction, and what replaces it?
5. Give two side effects that are naturally idempotent and one that never is.
6. You must call a PSP from the consumer. Name the two mechanisms and what residual state remains.
7. Ack before or after commit — name both semantics and which gap justifies the dedup table.
8. What sets the minimum TTL on the dedup table?
