# Transactional outbox & the dual-write problem

**Maps to:** Q6 (transactional outbox) · **Phase 1** · Exercise: shared Q6+Q7+Q8 arc module,
stage A ✅ ([p1-03-outbox-arc](../../../exercises/p1-03-outbox-arc/SPEC.md))
[← back to index](../README.md)

## TL;DR
A business operation that must **commit state to the DB** and **publish an event to a broker**
writes to **two transactional resources with independent commit points**. No ordering of the two
writes is safe — every ordering leaves a failure window (lost event or ghost event). The outbox
pattern restores atomicity by **collapsing both writes into one local ACID transaction**: the
event is INSERTed as a row next to the state change; an asynchronous **relay** publishes it
afterwards. The price: the relay is **at-least-once**, so consumers must be idempotent (→ Q7),
and "effectively-once" is a composition, not a delivery guarantee (→ Q8).

## The dual-write problem
| Ordering | Failure window | Downstream symptom |
|---|---|---|
| **Commit DB → publish** | Crash / network failure between commit and publish | **Lost event.** State changed; downstream never learns. Silent divergence — the worst kind (no error anywhere). |
| **Publish → commit DB** | Tx aborts after publish (constraint violation, serialization failure, crash before commit) | **Ghost event.** Consumers act on state that never existed. |
| **Publish inside the `@Transactional` method** | Same as above — the broker ack is **not governed by the DB tx**; still two commit points | Both windows, hidden behind an illusion of atomicity. |

**Key invariant:** atomicity is a property of *one* transactional resource. The moment an
operation spans two, you either need an atomic commit protocol across both (2PC) — or you must
collapse the writes into one resource. Everything else is a race.

Non-fixes that look like fixes:
- **Publisher confirms / RabbitMQ tx** — make the *publish itself* reliable; they do nothing to
  tie it to the DB commit. The window is *between* the resources, not inside either one.
- **try/catch + compensating delete** — the crash case has no `catch` block running.
- **"Publish last, it rarely fails"** — rare × forever = certain. In payments, one lost
  `PaymentCaptured` event is an unreconciled ledger.

## Why not 2PC / XA
| Objection | Mechanism |
|---|---|
| Blocking protocol | Participants in the **in-doubt** window hold locks until the coordinator decides; a slow/dead coordinator freezes them |
| Coordinator SPOF | Coordinator log is a new critical resource to replicate/recover |
| Latency & throughput | Two round-trips per participant per commit, on the hot path |
| Heuristic outcomes | A participant timing out may unilaterally commit/abort → the "atomic" protocol yields mixed results anyway |
| Ecosystem reality | RabbitMQ and Kafka don't participate in XA in practice; most cloud/SaaS APIs never will |

Modern services therefore standardize on **local ACID tx + asynchronous propagation** — the
outbox is the canonical instance of that stance.

## The pattern
```
        ONE local ACID tx                        async, after commit
┌────────────────────────────────┐
│ INSERT payment  (state change) │        relay ──► SELECT unpublished
│ INSERT outbox   (the event)    │                  ──► publish to broker
└──────────────┬─────────────────┘                  ──► mark published / delete
               │
        single commit point — both rows durable, or neither
```
The event payload is **authored inside the transaction**, at the moment the state changes, with
full context — the outbox row *is* the explicit event contract.

### Outbox table shape
| Column | Type / note |
|---|---|
| `id` | monotonic PK (`bigserial`, or UUIDv7). Doubles as the **message id** consumers dedup on (→ Q7) |
| `aggregate_type` / `aggregate_id` | routing + per-aggregate ordering key |
| `event_type` | e.g. `PaymentAuthorized` |
| `payload` | `jsonb`, written at commit time — the contract, decoupled from internal schema |
| `created_at` | audit / lag metrics |
| `published_at` | `NULL` = pending; alternative: delete-after-publish (leaner, loses audit trail) |

Retention: rows are done once published — cron cleanup or delete-on-publish; unbounded growth is
an operational, not correctness, problem.

## The relay — polling publisher vs CDC (log tailing)
| | Polling publisher | CDC (Debezium / WAL tailing) |
|---|---|---|
| How | Periodic `SELECT … WHERE published_at IS NULL ORDER BY id LIMIT n` | Reads the replication log (WAL / binlog) |
| Latency | Poll interval (100 ms–seconds) | Near-real-time |
| DB load | Constant polling; index on the pending predicate | None beyond replication slot |
| Ordering | **Sequence order ≠ commit order** (pitfall below) | **Commit-ordered** by construction |
| Multi-instance | Needs `FOR UPDATE SKIP LOCKED` or leader election | Connector framework handles it |
| Infra cost | None — it's a loop in your service | Kafka Connect / Debezium to run and operate |
| When | Default; fine at most scales | High volume, latency-sensitive, or CDC already present |

**Polling mechanics done right:** `SELECT … FOR UPDATE SKIP LOCKED` (instances don't double-claim
or block each other) → publish with confirms → `UPDATE published_at`. A crash **between publish
and mark** re-publishes on restart — this is *the* duplicate window that makes the whole pipeline
**at-least-once by design**. You don't fight it; you make consumers idempotent (Q7).

**Ordering pitfall (sequence ≠ commit order):** `id` is allocated at INSERT time, but
transactions commit in any order — a long tx can commit id 103 *after* a relay pass already read
id 105. A relay that resumes "after max id seen" **permanently skips** 103. Mitigations:
re-scan *all* unpublished rows each pass (the `published_at IS NULL` predicate, not an id
cursor); or CDC, whose WAL order is commit order. Cross-aggregate total order is not worth
chasing — preserve **per-aggregate** order and let the rest interleave.

## What it buys / what it doesn't
| Guaranteed | Not guaranteed |
|---|---|
| No lost events (event durable iff state committed) | **Exactly-once delivery** — relay is at-least-once (→ Q7/Q8) |
| No ghost events (rollback removes the outbox row too) | Synchronous propagation — consumers see the event later (eventual consistency) |
| Explicit, versionable event contract | Cross-aggregate global ordering |
| Works with plain Postgres + any broker | Free storage — published rows need cleanup |

## Outbox vs CDC-on-domain-tables
Pointing Debezium **directly at business tables** also solves dual-write (WAL is written in the
same commit) — but consumers become coupled to your **internal schema**: every column rename is a
breaking event change, and intent (`PaymentAuthorized` vs "row updated") is lost. The outbox
keeps an intentional contract; **Debezium's outbox event router** combines both: CDC transport,
outbox contract. That's the "outbox vs CDC" tradeoff in one line: *transport mechanism vs
contract ownership — the outbox table is about the contract.*

## Alternatives (know why they're rejected)
- **2PC/XA** — above.
- **Listen-to-yourself:** publish first, consume your own event to update the DB. DB state
  becomes eventually consistent with itself; read-your-writes is gone; validation happens after
  publish (ghosts possible).
- **Event sourcing:** the event log *is* the state — dual-write dissolves by construction, but
  it's a whole-paradigm commitment (projections, replay, versioning), not a patch.

## Self-check (answer cold)
1. Name both dual-write failure windows and the downstream symptom of each.
2. Why does publishing inside `@Transactional` not fix it? Why don't publisher confirms?
3. Give three reasons 2PC/XA is rejected for DB+broker atomicity.
4. Why is the relay at-least-once — where exactly is the duplicate window?
5. Why can an id-cursor relay skip events forever? Two mitigations?
6. Polling relay vs CDC — two tradeoffs each; what does Debezium's outbox router combine?
