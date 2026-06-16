# Progress Log — Senior Java/Kotlin Path

> Source of truth for my progress. Claude Code updates and commits this every session.
> Related: `roadmap.md` (plan), `knowledge-map.md` (coverage + calibration + re-tests).

## Meta
- **Start:** 2026-06-14
- **Target:** Senior Java/Kotlin Backend Engineer
- **Horizon:** ~18 mo (front-loaded) — adjust to actual hours
- **Hours/week:** 6–10 (roadmap baseline; ~18-mo horizon holds)
- **Title / experience:** Mid-level backend, 3–5 yrs (payments/fintech; Sofia)
- **Current overall level:** L3 (competent mid) — **confidence: LOW** (only baseline taken)
- **Last assessment:** 2026-06-14 (baseline) · **Next due:** 2026-06-28

## Level per pillar
Scale: L1 (novice) · L2 (surface) · L3 (confident mid) · L4 (strong mid) · L5 (senior) · L6 (staff)

| Pillar | Current | Target | Confidence | Notes |
|---|:--:|:--:|:--:|---|
| Distributed systems & tx correctness | L2–L3 | L5 | low | Top priority. Outbox/exactly-once/idempotency open |
| Concurrency (JVM + Kotlin) | L2 | L5 | low | Deepest gap. Pinning inverted, coroutines 0 |
| JVM internals & performance | L2 | L4–L5 | low | Surface: GC/JIT/profiling (largely untested) |
| Persistence (Hibernate/JPA) | L3 | L5 | low | Practice yes; internals (batch fetch, L2, MVCC) open |
| System design & architecture | L2–L3 | L5 | low | Largest senior delta |
| Resilience & observability | L2 | L5 | low | Bulkhead confused with feature flag; tail-latency 0 |
| Domain (payments/fintech) | L3 | L5 | low | Strong by context; needs formalizing |
| Spring/Kotlin currency | L3 | L4 | low | Boot 3→4, Kotlin 2.4 |

## Phase tracker
| Phase | Theme | Status | Start | End | Project done | Re-test |
|---|---|:--:|:--:|:--:|:--:|:--:|
| 1 | Distributed & tx correctness | in progress | 2026-06-14 | | ☐ | |
| 2 | Concurrency (JVM + Kotlin) | not started | | | ☐ | |
| 3 | JVM internals / persistence depth | not started | | | ☐ | |
| 4 | System design & architecture | not started | | | ☐ | |
| 5 | Resilience & observability | not started | | | ☐ | |

## Diagnostic / re-test summary
Full per-cell detail lives in `knowledge-map.md`. Baseline (2026-06-14), scores 0–100:

| Q | Topic | Score |
|---|---|:--:|
| Q1 | Tx propagation / proxy self-invocation | 40 → 80 |
| Q2 | Isolation levels & MVCC | 35 |
| Q3 | N+1 / fetch strategies | 55 |
| Q4 | Coroutine scopes & cancellation | 10 |
| Q5 | Virtual threads & pinning | 35 |
| Q6 | Transactional outbox | 25 |
| Q7 | Idempotent consumption | 30 |
| Q8 | Exactly-once / delivery semantics | 0 |
| Q9 | Payment system design | 25 |
| Q10 | Resilience patterns | 40 |
| Q11 | Tail-latency diagnosis | 0 |
| Q12 | equals/hashCode in collections | 40 |

Baseline mean ≈ 28.

## Completed tasks / projects
| Date | What | Artifact / module |
|---|---|---|
| 2026-06-14 | Lab bootstrapped; Phase 1 opened | repo scaffold, `CLAUDE.md`, exercise index |
| 2026-06-14 | Exercise `p1-01` created (RED by design) — targets Q1 | `exercises/p1-01-tx-self-invocation/` |
| 2026-06-16 | Solved `p1-01` (Q1) via separate-bean; test GREEN | `exercises/p1-01-tx-self-invocation/` |
| 2026-06-16 | Knowledge base started (hub + Q1 proxy/tx note) | `docs/knowledge-base/` |

## Exercise tracker
| Exercise | Phase | Targets | Status | Result |
|---|:--:|:--:|---|---|
| p1-01-tx-self-invocation | 1 | Q1 | REVIEWED | solved via separate-bean; `payments=1, audits=0`. Stretch 1 verified (REQUIRED → `UnexpectedRollbackException`). Cold re-test passed → Q1 80 (provisionally closed) |

## Open weak spots (priority top-down)
1. Transactional outbox — pattern not known (Q6).
2. Exactly-once / effectively-once semantics (Q8).
3. Idempotent consumption: key + dedup store + failure window (Q7).
4. Coroutines: `coroutineScope` vs `supervisorScope`, cancellation (Q4).
5. Virtual threads: pinning, CPU-bound (Q5).
6. Spring proxy self-invocation (Q1). — **p1-01 GREEN (40→70); cold re-test of propagation + proxy-types owed before "closed"; `UnexpectedRollbackException` stretch not yet done.**
7. InnoDB default = REPEATABLE READ; MVCC vs locking (Q2).
8. Bulkhead = resource isolation; circuit breaker auto-recovery (Q10).
9. Tail-latency diagnosis p99/p50 (Q11).
10. Batch fetching / `@EntityGraph`; equals/hashCode buckets (Q3, Q12).

## Next session focus
Q1 provisionally closed (80) — re-confirm briefly at the 2026-06-28 cycle (REQUIRED-vs-
REQUIRES_NEW + name `rollbackOnly`; proxy = startup wiring vs per-call interceptor).
Main work: begin **Q2 — isolation levels & MVCC** (next Phase-1 cell) — scaffold `p1-02`
(Postgres snapshot isolation vs InnoDB REPEATABLE READ; optimistic vs pessimistic locking).
