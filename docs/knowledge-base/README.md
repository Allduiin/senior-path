# Knowledge Base — theory index

The durable theory notes for the senior-path lab. Each entry below links to a focused
note. Notes are written/extended by the mentor as topics are taught, and are meant for
**spaced re-reading** — terse, mechanism-first, table-heavy.

> How this relates to the other docs:
> - `roadmap.md` = the plan · `progress-log.md` = my live status · `knowledge-map.md` = scores.
> - **This folder = the theory itself**, mapped to the diagnostic questions (Q1–Q12).

Status legend: ✅ written · ✍️ partial · ⬜ not yet written.

## Phase 1 — Distributed systems & transactional correctness
| Theme | Maps to | Status | Note |
|---|:--:|:--:|---|
| Spring proxy AOP & `@Transactional` propagation | Q1 | ✅ | [phase-1-distributed-tx/spring-proxy-and-transactions.md](phase-1-distributed-tx/spring-proxy-and-transactions.md) |
| Isolation levels & MVCC | Q2 | ✅ | [phase-1-distributed-tx/isolation-levels-and-mvcc.md](phase-1-distributed-tx/isolation-levels-and-mvcc.md) |
| Transactional outbox & the dual-write problem | Q6 | ✅ | [phase-1-distributed-tx/transactional-outbox.md](phase-1-distributed-tx/transactional-outbox.md) |
| Idempotent consumption (keys, dedup, failure window) | Q7 | ⬜ | _pending_ |
| Delivery semantics & effectively-once | Q8 | ⬜ | _pending_ |

## Phase 2 — Concurrency (JVM + Kotlin)
| Theme | Maps to | Status | Note |
|---|:--:|:--:|---|
| Coroutine scopes & cancellation | Q4 | ⬜ | _pending_ |
| Virtual threads & pinning | Q5 | ⬜ | _pending_ |
| Java Memory Model (happens-before, volatile) | — | ⬜ | _pending_ |

## Phase 3 — JVM internals / persistence depth
| Theme | Maps to | Status | Note |
|---|:--:|:--:|---|
| N+1, fetch strategies, batch fetching, `@EntityGraph` | Q3 | ⬜ | _pending_ |
| Hibernate internals (flush, dirty checking, L1/L2) | Q3 | ⬜ | _pending_ |
| `equals`/`hashCode` with hash-based collections | Q12 | ⬜ | _pending_ |
| GC (G1 vs ZGC), JIT, profiling / flame graphs | — | ⬜ | _pending_ |

## Phase 4 — System design & architecture
| Theme | Maps to | Status | Note |
|---|:--:|:--:|---|
| Payment authorization service design | Q9 | ⬜ | _pending_ |
| Service boundaries, API/contract & REST versioning | — | ⬜ | _pending_ |

## Phase 5 — Resilience, observability & ops
| Theme | Maps to | Status | Note |
|---|:--:|:--:|---|
| Resilience4j: circuit breaker, bulkhead, retry, timeout | Q10 | ⬜ | _pending_ |
| Tail-latency diagnosis (p99 spike / flat p50) | Q11 | ⬜ | _pending_ |
| Observability: metrics/logs/traces, OpenTelemetry | — | ⬜ | _pending_ |
