# Progress Log — Senior Java/Kotlin Path

> Source of truth for my progress. Claude Code updates and commits this every session.
> Related: `roadmap.md` (plan), `knowledge-map.md` (scores — single owner), `CLAUDE.md`
> (exercise index — single owner), `spaced-review.md` (retention).

## Meta
- **Start:** 2026-07-07 (**fresh start** — the 2026-06 pilot run is archived in git history)
- **Target:** Senior Java/Kotlin Backend Engineer
- **Horizon:** ~18 mo (front-loaded) — adjust to actual hours
- **Hours/week:** 6–10 (roadmap baseline)
- **Title / experience:** Mid-level backend, 3–5 yrs (payments/fintech; Sofia)
- **Current overall level:** ~L3 (pilot estimate) — **re-baseline pending**
- **Last assessment:** — (pilot baseline 2026-06-14, superseded) · **Next due: baseline
  diagnostic at next session** (Q1–Q12 cold, diagnostic-style)

## Level per pillar
Scale: L1 (novice) · L2 (surface) · L3 (confident mid) · L4 (strong mid) · L5 (senior) · L6 (staff)

All levels below are **pilot estimates** (2026-06-14 diagnostic) carried as priors; the fresh
baseline recalibrates them.

| Pillar | Prior (pilot) | Target | Notes |
|---|:--:|:--:|---|
| Distributed systems & tx correctness | L2–L3 | L5 | Top priority. Outbox/exactly-once/idempotency open |
| Concurrency (JVM + Kotlin) | L2 | L5 | Deepest gap. Pinning inverted, coroutines ~0 |
| JVM internals & performance | L2 | L4–L5 | Surface: GC/JIT/profiling (largely untested) |
| Persistence (Hibernate/JPA) | L3 | L5 | Practice yes; internals (batch fetch, L2, MVCC) open |
| System design & architecture | L2–L3 | L5 | Largest senior delta |
| Resilience & observability | L2 | L5 | Bulkhead confused with feature flag; tail-latency 0 |
| Domain (payments/fintech) | L3 | L5 | Strong by context; needs formalizing |
| Spring/Kotlin currency | L3 | L4 | Boot 3→4, Kotlin 2.4 |

## Phase tracker
| Phase | Theme | Status | Start | End | Project done | Re-test |
|---|---|:--:|:--:|:--:|:--:|:--:|
| 1 | Distributed & tx correctness | ready — opens after baseline | | | ☐ | |
| 2 | Concurrency (JVM + Kotlin) | not started | | | ☐ | |
| 3 | JVM internals / persistence depth | not started | | | ☐ | |
| 4 | System design & architecture | not started | | | ☐ | |
| 5 | Resilience & observability | not started | | | ☐ | |

## Diagnostic / re-test scores
**Owner: `knowledge-map.md`** (one owner per fact — see `CLAUDE.md`). This file never restates
the numbers. Status: **fresh baseline pending**; pilot results kept there as reference.

## Completed tasks / projects
| Date | What | Artifact / module |
|---|---|---|
| 2026-07-07 | **Lab overhauled + fresh start.** One-owner-per-fact doc rule; Analysis gate; multi-theme arcs & phase capstones defined; Q13+ convention; reading log added; skills updated; p1-01 & p1-02 reverted to RED scaffolds for re-issue; pilot run (2026-06-14→17) archived in git history | whole repo |

## Exercise tracker
**Owner: the Exercise index in `CLAUDE.md`** (status + run commands). Solve details are logged
per-date in Completed tasks; scores in `knowledge-map.md`.

## Open weak spots (priority top-down)
Carried from the pilot as **priors** — the fresh baseline confirms/reorders them. Gaps only;
scores live in `knowledge-map.md`.
1. Transactional outbox — pattern not known (Q6).
2. Exactly-once / effectively-once semantics (Q8).
3. Idempotent consumption: key + dedup store + failure window (Q7).
4. Coroutines: `coroutineScope` vs `supervisorScope`, cancellation (Q4).
5. Virtual threads: pinning, CPU-bound (Q5).
6. Isolation levels & MVCC (Q2) — pilot gaps: atomic current-read-under-row-lock, SSI for
   write skew, optimistic/pessimistic/atomic tradeoff axes.
7. Spring proxy self-invocation (Q1) — pilot gaps: REQUIRED-vs-REQUIRES_NEW + `rollbackOnly`;
   proxy = startup wiring vs per-call interceptor.
8. Bulkhead = resource isolation; circuit breaker auto-recovery (Q10).
9. Tail-latency diagnosis p99/p50 (Q11).
10. Batch fetching / `@EntityGraph`; equals/hashCode buckets (Q3, Q12).

## Next session focus
**Run the fresh baseline diagnostic** (Q1–Q12, cold, diagnostic-style — record in
`knowledge-map.md` as the 2026-07 baseline), recalibrate pillar levels, then open Phase 1 via
`/learn-theme` on the top gap. Candidate first arc: **Q6+Q7+Q8 as one exercise arc** (dual-write →
outbox → idempotent consumer — see `learning-flow.md` → Multi-theme arcs), or re-issue
p1-01/p1-02 (both reverted to RED) if Q1/Q2 baseline low. KB notes for Q1/Q2 exist from the
pilot — reuse/extend rather than rewrite.
