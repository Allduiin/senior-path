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
- **Current overall level:** ~L2–L3 (fresh baseline 2026-07-28, Q1–Q12 mean ≈ 31)
- **Last assessment:** 2026-07-28 (fresh baseline, `/assess`) · **Next due: 2026-08-18**

## Level per pillar
Scale: L1 (novice) · L2 (surface) · L3 (confident mid) · L4 (strong mid) · L5 (senior) · L6 (staff)

Levels recalibrated from the **2026-07-28 fresh baseline** (scores in `knowledge-map.md`).
Mapping: pillar cell-mean < 40 → L2 · 40–59 → L3 · 60–79 → L4 · 80–89 → L5 · 90+ → L6;
single-cell pillars capped at L3 (low confidence) until a second cell lands.

| Pillar | Baseline 2026-07-28 | Target | Notes |
|---|:--:|:--:|---|
| Distributed systems & tx correctness | L2 (mean ≈ 19) | L5 | Top priority. Q1=50 only partial cell; Q6/Q7/Q8 ≈ blank — outbox/idempotency/delivery vocabulary absent |
| Concurrency (JVM + Kotlin) | L2 (mean 25) | L5 | Deepest gap confirmed. Coroutines instinct-only; pinning unknown |
| JVM internals & performance | L3 (single cell, low conf.) | L4–L5 | Q12=65 (best answer); GC/JIT/profiling still unprobed |
| Persistence (Hibernate/JPA) | L3 (mean 50) | L5 | Q2=55, Q3=45; batch fetching + SSI + atomic-update gaps |
| System design & architecture | L2 (Q9=35) | L5 | Instincts right, mechanisms missing (idempotency key, state machine enforcement) |
| Resilience & observability | L2 (mean 25) | L5 | Q10 honest blank (pilot 40 was inflated); Q11 up 0→40 |
| Domain (payments/fintech) | L3 (prior, untested) | L5 | Carried; formalizing pending |
| Spring/Kotlin currency | L3 (prior, untested) | L4 | Carried; Q1 partial evidence only |

## Phase tracker
| Phase | Theme | Status | Start | End | Project done | Re-test |
|---|---|:--:|:--:|:--:|:--:|:--:|
| 1 | Distributed & tx correctness | **in progress** | 2026-07-28 | | ☐ | |
| 2 | Concurrency (JVM + Kotlin) | not started | | | ☐ | |
| 3 | JVM internals / persistence depth | not started | | | ☐ | |
| 4 | System design & architecture | not started | | | ☐ | |
| 5 | Resilience & observability | not started | | | ☐ | |

## Diagnostic / re-test scores
**Owner: `knowledge-map.md`** (one owner per fact — see `CLAUDE.md`). This file never restates
the numbers. Status: **operative baseline recorded 2026-07-28**; pilot kept there as prior.

## Completed tasks / projects
| Date | What | Artifact / module |
|---|---|---|
| 2026-07-07 | **Lab overhauled + fresh start.** One-owner-per-fact doc rule; Analysis gate; multi-theme arcs & phase capstones defined; Q13+ convention; reading log added; skills updated; p1-01 & p1-02 reverted to RED scaffolds for re-issue; pilot run (2026-06-14→17) archived in git history | whole repo |
| 2026-07-28 | **Fresh baseline diagnostic** (Q1–Q12 cold via `/assess`). Mean ≈ 31; pillar levels recalibrated; Phase 1 opened. `/assess` calibration rules validated (sketch → v1, single-cell cap added) | docs + `/assess` skill |

## Exercise tracker
**Owner: the Exercise index in `CLAUDE.md`** (status + run commands). Solve details are logged
per-date in Completed tasks; scores in `knowledge-map.md`.

## Open weak spots (priority top-down)
Reordered from the **2026-07-28 baseline**. Gaps only; scores live in `knowledge-map.md`.
1. **Q6+Q7+Q8 cluster (the Phase 1 arc):** dual-write problem & outbox pattern unknown (Q6);
   idempotent consumption — key, same-DB dedup table, atomic check+side-effect (Q7); delivery
   semantics vocabulary — ack ordering, Two Generals, effectively-once composition (Q8).
2. Resilience patterns (Q10): circuit-breaker state machine + half-open recovery; bulkhead as
   resource isolation; retry hazards in payments.
3. Coroutines (Q4): structured concurrency (parent Job, sibling cancellation),
   `supervisorScope` + per-child handling, cooperative cancellation & `CancellationException`.
4. Virtual threads (Q5): carrier/mount/unmount mechanism; pinning (`synchronized`, JNI) +
   carrier-pool starvation; `ReentrantLock` fix, JEP 491.
5. Payment design mechanisms (Q9): idempotency-key insert-*before*-PSP-call; explicit
   PENDING-unknown state + webhook/poll reconciliation pair; guarded state transitions.
6. Tail latency (Q11): GC-pause & pool-exhaustion cause families; retry-masked timeouts;
   traces→metrics→logs localization order.
7. N+1 (Q3): batch fetching (`@BatchSize`) absent; EAGER ≠ join for JPQL; HHH000104
   in-memory pagination + two-query workaround; lazy trigger is per-collection.
8. Spring tx (Q1): `rollbackOnly`/`UnexpectedRollbackException`; `TransactionTemplate` as
   third fix; checked exceptions don't roll back by default.
9. Isolation/MVCC (Q2): atomic `UPDATE ... SET x = x - n` as fix #0; optimistic `@Version`
   missing from repertoire; SSI = dependency-cycle detection, not table locking.
10. equals/hashCode (Q12): orphaned-entry + re-put duplication mechanics; immutable-key
    design rules for review.

## Next session focus
**Start the Q6+Q7+Q8 arc via `/learn-theme` (begin with Q6 — transactional outbox).** The
cluster scored 20/5/0 and is the top weak spot; run teach → KB note per theme, then the shared
arc exercise (dual-write → outbox → idempotent consumer, per `learning-flow.md` → Multi-theme
arcs). p1-01/p1-02 stay RED in the queue: Q1 (50) and Q2 (55) need their re-teach + re-solve
after the arc is underway — KB notes exist from the pilot, extend rather than rewrite. Add the
~10-min design corner (place the outbox in a PSP integration — feeds Q9).
