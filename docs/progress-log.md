# Progress Log — Senior Java/Kotlin Path

> Source of truth for my progress. Claude Code updates and commits this every session.
> Related: `roadmap.md` (plan), `knowledge-map.md` (scores — single owner), `CLAUDE.md`
> (exercise index — single owner), `spaced-review.md` (retention).

## Meta
<!-- viz:meta -->
- **Start:** 2026-07-28 (**fresh start** — day of the operative baseline; lab overhaul was
  2026-07-07, the 2026-06 pilot run is archived in git history)
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

<!-- viz:pillars -->
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
<!-- viz:phases -->
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
<!-- viz:tasks -->
| Date | What | Artifact / module |
|---|---|---|
| 2026-07-07 | **Lab overhauled + fresh start.** One-owner-per-fact doc rule; Analysis gate; multi-theme arcs & phase capstones defined; Q13+ convention; reading log added; skills updated; p1-01 & p1-02 reverted to RED scaffolds for re-issue; pilot run (2026-06-14→17) archived in git history | whole repo |
| 2026-07-28 | **Fresh baseline diagnostic** (Q1–Q12 cold via `/assess`). Mean ≈ 31; pillar levels recalibrated; Phase 1 opened. `/assess` calibration rules validated (sketch → v1, single-cell cap added) | docs + `/assess` skill |
| 2026-08-07 | **Q6 taught + KB note** (`/learn-theme`, arc start): dual-write failure windows, 2PC rejection, outbox mechanism, polling-vs-CDC relay, sequence≠commit-order pitfall, at-least-once consequence. Entered spaced review (due 2026-08-14). Stages 1–2+6 done | [knowledge-base/phase-1-distributed-tx/transactional-outbox.md](knowledge-base/phase-1-distributed-tx/transactional-outbox.md) |
| 2026-08-07 | **p1-03-outbox-arc scaffolded** (`/next-exercise`, arc stage A = Q6): payment capture with seeded dual-write (`TransactionTemplate` commit → `CrashPoint` → inline publish); crash test RED on assertion (`failures=1, errors=0`), happy-path + ghost-guard tests GREEN; Postgres + RabbitMQ Testcontainers. Stages B (Q7) / C (Q8) will extend the module | `exercises/p1-03-outbox-arc/` |
| 2026-08-10 | **Q7 taught + KB note** (`/learn-theme Q7`, arc stage B): why the consumer is the only place duplicates can be absorbed; producer-assigned key vs deliveryTag/business key; dedup store in the same DB; the check-then-act crash window (record-first ⇒ silent loss, effect-first ⇒ duplicate); `INSERT … ON CONFLICT DO NOTHING` as atomic claim; natural idempotency (guarded transition / upsert) vs relative accumulation; foreign side effects (PSP idempotency key, record-intent-before-call); ack-after-commit; per-consumer scope + retention bound. Entered spaced review (due 2026-08-17). Stages 1–2+6 done | [knowledge-base/phase-1-distributed-tx/idempotent-consumption.md](knowledge-base/phase-1-distributed-tx/idempotent-consumption.md) |
| 2026-08-10 | **p1-03 arc stage B scaffolded** (`/next-exercise`, Q7): second queue `payment-captured-ledger` bound to the same routing key (stage-A tests undisturbed); append-only `payout_ledger` as the accumulative side effect (no unique-constraint shortcut); `PayoutLedgerConsumer` skeleton; `CrashPoint` extended with payload-discriminated arming + `AFTER_PUBLISH_BEFORE_MARK` (placed in the relay) and `AFTER_CLAIM_BEFORE_EFFECT` (user places it). 3 tests: single credit, republish ⇒ no double-credit, mid-handle crash ⇒ no lost credit. Verified RED on assertions (3 failures, 0 errors) with stage A still green. Also fixed stage A, which did not compile as committed (dead `rabbit: rabbitTemplate` param) | `exercises/p1-03-outbox-arc/` |
| 2026-08-09 | **p1-03 stage A solved & REVIEWED** (Q6 20→70, see knowledge-map): user built same-tx outbox write + `@Scheduled` relay; review caught findAll-republish bug, ordering, interval — all fixed by user; Analysis written (RU/EN mix, accepted); design-corner debt paid (PSP call = third irreversible commit point); cold quiz 4 questions. User independently proposed and stress-tested 3 "simpler" alternatives (in-memory retry, fallback-row-on-error, publish-inside-tx) — each broken on process-death, strong learning signal | `exercises/p1-03-outbox-arc/`, SPEC Analysis |

## Exercise tracker
**Owner: the Exercise index in `CLAUDE.md`** (status + run commands). Solve details are logged
per-date in Completed tasks; scores in `knowledge-map.md`.

## Open weak spots (priority top-down)
<!-- viz:weak-spots -->
Reordered from the **2026-07-28 baseline**. Gaps only; scores live in `knowledge-map.md`.
1. **Q6+Q7+Q8 cluster (the Phase 1 arc):** Q6 core closed at stage-A review 2026-08-09;
   residual sub-gaps: publisher-confirms remaining window (confirms cover only the running
   publish leg), sequence≠commit-order max-id-cursor skip. Q7 taught 2026-08-10 — score still
   at baseline until stage B is solved and reviewed; the gap to close in code is the **atomic
   claim + side effect in one local transaction** (and detecting "already claimed" without
   `SELECT`-then-`INSERT`). Q8 delivery semantics — ack ordering, Two Generals, effectively-once
   composition (untaught).
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
**Q7 is at stage 4 — solve p1-03 arc stage B** (`./gradlew :p1-03-outbox-arc:test`, Docker
required): idempotency key + same-DB dedup table + atomic claim and side effect in ONE local
transaction; place `AFTER_CLAIM_BEFORE_EFFECT` between claim and credit; fill the stage-B
Analysis (ANALYSIS GATE). Resume with `/learn-theme Q7` on GREEN → stage 5 review + Q7 score.
RED verified 2026-08-10: stage B `failures=3, errors=0` (all `AssertionError`), stage A still
`failures=0, errors=0`.
Q6 retention due **2026-08-14** (`/repeat-knowledge`) — probe the two residual sub-gaps
(confirms cover only the running publish leg; max-id-cursor skip). Q7 retention due 2026-08-17.
Deep re-assessment due **2026-08-18** (`/assess`). p1-01/p1-02 stay RED in the queue.
Session pacing note (2026-08-09, still active): short teach blocks, RU allowed for write-ups,
land terms on code he already wrote.
