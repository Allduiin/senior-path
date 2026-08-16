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
| 2026-08-16 | **p1-03 arc stage C solved & REVIEWED** (Q8 35→78, see knowledge-map) — **arc Q6+Q7+Q8 complete.** User fixed the seeded at-most-once consumer: `NONE`→`AUTO` (ack after method return ⇒ effect→ack ⇒ at-least-once), then reapplied the stage-B claim+effect-in-one-tx composition for effectively-once. Scoped the shared dedup key `{consumer}:{eventId}` unprompted (the stage-B quiz answer became code) and narrowed both consumers' catch to `DuplicateKeyException`. All 9 tests green. Analysis clean on the second pass after two corrections (producer↔broker redelivery/ack confusion; Kafka EOS scope). Whole payments-outbox pipeline now demonstrates dual-write→outbox→relay→at-least-once→idempotent consumer→effectively-once end to end | `exercises/p1-03-outbox-arc/`, SPEC Analysis |
| 2026-08-13 | **p1-03 arc stage C scaffolded** (`/next-exercise`, Q8): third queue `payment-captured-notify` on the same routing key; `MerchantNotificationConsumer` ships **working-but-wrong** — seeded `fireAndForgetFactory` (`AcknowledgeMode.NONE`, ack-before-effect) + bare `save` (no dedup); `AFTER_CLAIM_BEFORE_NOTIFY` crash label. Hidden trap: the dedup store is now shared by two consumers of one event — the stage-B quiz answer (scoped key) must become code. 3 tests: happy green, mid-handle crash ⇒ lost notification RED, republish ⇒ double notification RED. Verified `failures=2, errors=0` (both `AssertionError`), stages A+B still `failures=0` | `exercises/p1-03-outbox-arc/` |
| 2026-08-13 | **Q8 taught + KB note** (`/learn-theme Q8`, arc stage C prep): pre-teach probe 0→35 (ack-placement axis already derived from stage-B work). Taught: the two-semantics table + Spring AMQP `acknowledge-mode` mapping; Two Generals as a shortest-sequence proof (and why producer-queries-consumer is the same regress); the perfect-network process-death window; delivery-vs-effect reframe; effectively-once as the four-ingredient composition the user's own pipeline supplies; the four-hop confirms/ack gap table; Kafka EOS dissected (idempotent producer vs transactions, duplicate-source table, session caveat, Kafka→Kafka as the sole no-dedup case). Entered spaced review (due 2026-08-20). Stages 1–2+6 done | [knowledge-base/phase-1-distributed-tx/delivery-semantics.md](knowledge-base/phase-1-distributed-tx/delivery-semantics.md) |
| 2026-08-11 | **p1-03 stage B solved & REVIEWED** (Q7 5→82, see knowledge-map): user built the idempotent consumer — key = outbox row PK carried in AMQP `message_id` (relay sets it via `MessagePostProcessor`), claim = single `INSERT` with atomicity from the PK constraint, claim + ledger append in ONE `TransactionTemplate` transaction, duplicate caught as `DataIntegrityViolationException` outside the tx boundary. All 6 tests green with the crash point correctly placed inside the transaction. Debug path was itself the lesson: hand-rolled `ObjectMapper` vs contract drift (payload built by string template, parsed into the JPA entity), Spring Data `save()`⇒`merge` on assigned ids silently defeating the claim, `ConditionalRejectingErrorHandler` fatal-exception classification. Analysis took three passes; mentor error corrected mid-session (claimed a Boot `ObjectMapper` bean exists — it does not without spring-web). Cold quiz run after the user challenged a score set from guided work — the stage-5 gate requires a cold answer, and the challenge was correct | `exercises/p1-03-outbox-arc/`, SPEC Analysis |
| 2026-08-09 | **p1-03 stage A solved & REVIEWED** (Q6 20→70, see knowledge-map): user built same-tx outbox write + `@Scheduled` relay; review caught findAll-republish bug, ordering, interval — all fixed by user; Analysis written (RU/EN mix, accepted); design-corner debt paid (PSP call = third irreversible commit point); cold quiz 4 questions. User independently proposed and stress-tested 3 "simpler" alternatives (in-memory retry, fallback-row-on-error, publish-inside-tx) — each broken on process-death, strong learning signal | `exercises/p1-03-outbox-arc/`, SPEC Analysis |

## Exercise tracker
**Owner: the Exercise index in `CLAUDE.md`** (status + run commands). Solve details are logged
per-date in Completed tasks; scores in `knowledge-map.md`.

## Open weak spots (priority top-down)
<!-- viz:weak-spots -->
Reordered from the **2026-07-28 baseline**. Gaps only; scores live in `knowledge-map.md`.
1. **Q6+Q7+Q8 cluster (the Phase 1 arc) — arc complete 2026-08-16, all three cells 70–82,
   none yet held across two re-tests.** Q6 70, Q7 82, Q8 78. One persistent cross-cutting
   sub-gap survives all three stages and is the thing to probe on every future retention pass:
   **the producer↔broker / consumer↔broker boundary.** Recurs as: publisher-confirms conflated
   with the whole publish leg (Q6); the consumer-side commit→ack window answered with the relay's
   publish→mark-sent window (Q7, ≥3 times); "producer retries" / "MANUAL ack at producer" in the
   Q8 write-up. Root cause looks like one blurred mental model of *who talks to the broker on
   which leg*, not three separate slips. Other residuals: redelivery read as retry-policy rather
   than protocol-guaranteed requeue of unacked deliveries (Q7); Kafka EOS scope overreach,
   corrected (Q8); sequence≠commit-order max-id-cursor skip (Q6).
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
**Phase-1 arc Q6+Q7+Q8 is complete** (p1-03 all three stages REVIEWED). Retention now carries
these three; no new arc exercise needed. Immediate queue, by date:
- **Q6 retention overdue 2026-08-14** and **Q7 due 2026-08-17** (`/repeat-knowledge`) — one
  combined pass makes sense. Probe the cross-cutting **producer↔broker / consumer↔broker**
  sub-gap (weak-spot #1) hardest: who redelivers, who acks, on which leg. Also Q6 max-id-cursor
  skip. Q8 retention due 2026-08-20.
- **Deep re-assessment due 2026-08-18** (`/assess`) — first `/assess` since the fresh baseline;
  recompute the Distributed pillar now that Q6/Q7/Q8 are scored (70/82/78), and probe the
  broker-boundary gap with a fresh question rather than a repeat.
- **What's next in Phase 1:** roadmap topics still untouched here — **saga (orchestration vs
  choreography)** and the **Q1/Q2 re-teach** (Spring proxy propagation 50, isolation/MVCC 55),
  both still at baseline. p1-01 and p1-02 stay RED in the queue and target exactly these.
Deferred code follow-ups on p1-03 (fold into a retention pass, not a new exercise): dedup-table
retention (`createdAt` + cleanup, bounded below by the relay's unbounded republication) is still
unimplemented in both consumers.
Session pacing note (2026-08-09, still active): short teach blocks, RU allowed for write-ups,
land terms on code he already wrote.
