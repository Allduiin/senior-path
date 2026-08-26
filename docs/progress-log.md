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
| 2026-08-26 | **Spaced review after a 9-day gap** (`/repeat-knowledge`, RU) — all 4 ledger themes were due; session closed by the user after ~1 h on two. Q7 **pass 77** (q3, EF→2.04, reps 1, next 2026-09-02) — off the lapse: crash-window asymmetry reproduced cold on a code-review framing, TTL floor re-derived, and the PSP producer-sent `Idempotency-Key` named as the first-call prerequisite (the 08-17 design inversion, closed). Q8 **pass 70** (q3, at the floor, EF→2.36, reps 1, next 2026-09-02) — first recall since the teach; Kafka EOS answer was the session's best (`InitProducerId` PID recovery + epoch fencing, unprompted), but the perfect-network process-death window was missed. **Q6 carried** (still due). **Q2 parked** — user reports the theme is not yet learned and `p1-02` is untouched, so a retention quiz would measure the teach rather than recall. Deliberate grading note: retention grades were **not** written into the knowledge-map `Latest` column — that instrument is `/assess`-owned and is overdue; today's numbers are logged as evidence for it | `docs/spaced-review.md` review log |
| 2026-08-17 | **Q2 re-taught + KB note extended** (`/learn-theme Q2`, stage 1–2): targeted teach on the three baseline gaps — snapshot-read vs **current-read-under-row-lock** (landed on the user's own Q7 guarded-transition UPDATE), optimistic `@Version` as a row-level CAS with mandatory bounded retry of the whole RMW, **SSI as rw-antidependency cycle detection** (SIRead markers, dangerous structure, abort `40001`, false positives, write-skew example) — SSI section added to the KB note. Q2 entered spaced review (due 2026-08-24). Stage 3 already done (p1-02 RED since 2026-07-07); handed off at stage 4 | [knowledge-base/phase-1-distributed-tx/isolation-levels-and-mvcc.md](knowledge-base/phase-1-distributed-tx/isolation-levels-and-mvcc.md) |
| 2026-08-17 | **First spaced-review pass on the arc** (`/repeat-knowledge`, RU): Q6 **pass 82** (q4, reps 1, next 2026-08-24) — cursor-skip interleaving reconstructed flawlessly; slips: retries attributed to publisher confirms, re-scan mitigation not named. Q7 **lapse 68** (q2, EF→2.18, relearn 2026-08-19) — redelivery re-framed as retry-policy (recurring), crash-window asymmetry not reproduced, TTL bound answered as pipeline time not replay window. Grade challenged 65→68 (PSP dedup named cold, conditional framing). Post-quiz discussion productive: user correctly pinned the Idempotency-Key limit (delegated contractual guarantee, not physical — why reconciliation is always mandatory) | `docs/spaced-review.md` review log |
| 2026-07-07 | **Lab overhauled + fresh start.** One-owner-per-fact doc rule; Analysis gate; multi-theme arcs & phase capstones defined; Q13+ convention; reading log added; skills updated; p1-01 & p1-02 reverted to RED scaffolds for re-issue; pilot run (2026-06-14→17) archived in git history | whole repo |
| 2026-07-28 | **Fresh baseline diagnostic** (Q1–Q12 cold via `/assess`). Mean ≈ 31; pillar levels recalibrated; Phase 1 opened. `/assess` calibration rules validated (sketch → v1, single-cell cap added) | docs + `/assess` skill |
| 2026-08-07 | **Q6 taught + KB note** (`/learn-theme`, arc start): dual-write failure windows, 2PC rejection, outbox mechanism, polling-vs-CDC relay, sequence≠commit-order pitfall, at-least-once consequence. Entered spaced review (due 2026-08-14). Stages 1–2+6 done | [knowledge-base/phase-1-distributed-tx/transactional-outbox.md](knowledge-base/phase-1-distributed-tx/transactional-outbox.md) |
| 2026-08-07 | **p1-03-outbox-arc scaffolded** (`/next-exercise`, arc stage A = Q6): payment capture with seeded dual-write (`TransactionTemplate` commit → `CrashPoint` → inline publish); crash test RED on assertion (`failures=1, errors=0`), happy-path + ghost-guard tests GREEN; Postgres + RabbitMQ Testcontainers. Stages B (Q7) / C (Q8) will extend the module | `exercises/p1-03-outbox-arc/` |
| 2026-08-10 | **Q7 taught + KB note** (`/learn-theme Q7`, arc stage B): why the consumer is the only place duplicates can be absorbed; producer-assigned key vs deliveryTag/business key; dedup store in the same DB; the check-then-act crash window (record-first ⇒ silent loss, effect-first ⇒ duplicate); `INSERT … ON CONFLICT DO NOTHING` as atomic claim; natural idempotency (guarded transition / upsert) vs relative accumulation; foreign side effects (PSP idempotency key, record-intent-before-call); ack-after-commit; per-consumer scope + retention bound. Entered spaced review (due 2026-08-17). Stages 1–2+6 done | [knowledge-base/phase-1-distributed-tx/idempotent-consumption.md](knowledge-base/phase-1-distributed-tx/idempotent-consumption.md) |
| 2026-08-10 | **p1-03 arc stage B scaffolded** (`/next-exercise`, Q7): second queue `payment-captured-ledger` bound to the same routing key (stage-A tests undisturbed); append-only `payout_ledger` as the accumulative side effect (no unique-constraint shortcut); `PayoutLedgerConsumer` skeleton; `CrashPoint` extended with payload-discriminated arming + `AFTER_PUBLISH_BEFORE_MARK` (placed in the relay) and `AFTER_CLAIM_BEFORE_EFFECT` (user places it). 3 tests: single credit, republish ⇒ no double-credit, mid-handle crash ⇒ no lost credit. Verified RED on assertions (3 failures, 0 errors) with stage A still green. Also fixed stage A, which did not compile as committed (dead `rabbit: rabbitTemplate` param) | `exercises/p1-03-outbox-arc/` |
| 2026-08-16 | **p1-03 arc stage C solved & REVIEWED** (Q8 35→84, see knowledge-map) — **arc Q6+Q7+Q8 complete.** User fixed the seeded at-most-once consumer: `NONE`→`AUTO` (ack after method return ⇒ effect→ack ⇒ at-least-once), then reapplied the stage-B claim+effect-in-one-tx composition for effectively-once. Scoped the shared dedup key `{consumer}:{eventId}` unprompted (the stage-B quiz answer became code) and narrowed both consumers' catch to `DuplicateKeyException`. All 9 tests green. Analysis clean on the second pass after two corrections (producer↔broker redelivery/ack confusion; Kafka EOS scope). Whole payments-outbox pipeline now demonstrates dual-write→outbox→relay→at-least-once→idempotent consumer→effectively-once end to end | `exercises/p1-03-outbox-arc/`, SPEC Analysis |
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
1. **"Requeue" spoken as "retry" — the one recurring gap, now confirmed cross-theme (Q7 + Q8),
   third session running (2026-08-11, 08-17, 08-26).** Everything else about the broker boundary
   is held; this is a vocabulary-and-actor slip that keeps regenerating the wrong mental model.
   Two mechanisms must stay separate: **handler throws** ⇒ container `nack`/`reject`s ⇒ requeue or
   DLQ, governed by config (`defaultRequeueRejected`, retry interceptor) — *this* one is policy;
   **channel/connection dies** ⇒ the broker requeues **every unacked delivery** on that channel,
   unilaterally, AMQP-mandated, no backoff and no attempt counter — *this* one is a protocol
   invariant. On 2026-08-26 the redelivery was tied to "no success / no exception" when the premise
   was a dead connection, where the ack can never arrive however the method ends. Related, same
   family: under `AUTO` it is the **container** that acks after the listener returns — the broker
   does not "wait for processing". Probe by forcing a scenario where the handler *succeeds* and a
   redelivery still happens.
2. **Q8 residue (pass at the 70 floor, 2026-08-26).** (a) **The perfect-network window** — with
   loss ruled out, process death between applying the effect and sending the ack still leaves the
   gap; it relocates from the wire into one machine. Answered by assuming away process failure too
   (vacuous), then reintroducing network drops. The paired reframe also unstated: exactly-once
   *effect* is achievable because the effect lives in storage you control, delivery is a property
   of a wire you don't. (b) **Publisher-confirm direction still unanswered** — flagged as the lone
   residual slip on 08-16, skipped on 08-26; the confirm travels broker→publisher, mirror of the
   consumer→broker ack. (c) The confirm's producer-side blind spot named as the confirm-lost
   variant rather than the publish that **never started**. Strengths to not re-probe: Kafka EOS
   (answered beyond the note — `InitProducerId` PID recovery, epoch fencing), NONE-vs-AUTO mapping,
   the claim signal and the flush/rollback-only hazard of the exception-based variant.
   Q7 residue: dedup-TTL contributors named were the small ones (outbox retention, queue latency)
   — **DLQ replay and manual reprocessing** are what actually set the number, i.e. weeks not hours.
   Q6 untested since 08-17 (still due): confirm the two known slips closed.
3. Resilience patterns (Q10): circuit-breaker state machine + half-open recovery; bulkhead as
   resource isolation; retry hazards in payments.
4. Coroutines (Q4): structured concurrency (parent Job, sibling cancellation),
   `supervisorScope` + per-child handling, cooperative cancellation & `CancellationException`.
5. Virtual threads (Q5): carrier/mount/unmount mechanism; pinning (`synchronized`, JNI) +
   carrier-pool starvation; `ReentrantLock` fix, JEP 491.
6. Payment design mechanisms (Q9): idempotency-key insert-*before*-PSP-call; explicit
   PENDING-unknown state + webhook/poll reconciliation pair; guarded state transitions.
7. Tail latency (Q11): GC-pause & pool-exhaustion cause families; retry-masked timeouts;
   traces→metrics→logs localization order.
8. N+1 (Q3): batch fetching (`@BatchSize`) absent; EAGER ≠ join for JPQL; HHH000104
   in-memory pagination + two-query workaround; lazy trigger is per-collection.
9. Spring tx (Q1): `rollbackOnly`/`UnexpectedRollbackException`; `TransactionTemplate` as
   third fix; checked exceptions don't roll back by default.
10. Isolation/MVCC (Q2): atomic `UPDATE ... SET x = x - n` as fix #0; optimistic `@Version`
    missing from repertoire; SSI = dependency-cycle detection, not table locking.
11. equals/hashCode (Q12): orphaned-entry + re-put duplication mechanics; immutable-key
    design rules for review.

## Next session focus
**Phase-1 arc Q6+Q7+Q8 is complete** (p1-03 all three stages REVIEWED) and both arc themes now
carry a retention pass (Q7 77, Q8 70 — 2026-08-26). Queue, in priority order:
- **`/assess` is the overdue item — due 2026-08-18, now 8 days past.** First `/assess` since the
  fresh baseline. Recompute the Distributed pillar; **re-score the Q7 and Q8 cells** — the map
  still reads 82 / 84 from stage-5 review work, while cold retention reads 77 / 70. Those numbers
  were deliberately left alone on 08-26 (the map's `Latest` column is `/assess`-owned; mixing
  instruments is the drift the one-owner rule exists to prevent), so reconciling them is this
  assessment's job. Probe the weak-spot #1 boundary with a **fresh** scenario, not a repeat: force
  a case where the handler succeeds and a redelivery still happens.
- **Retention:** **Q6 is due now** (next_due 2026-08-24, carried unreviewed from 08-26) and leads
  the next `/repeat-knowledge`. Q7 + Q8 next due 2026-09-02.
- **Q2 is NOT learned — user said so explicitly on 2026-08-26**, and `p1-02` is untouched
  (`WalletService.withdraw` still the naive RMW with `TODO(task 2)`, SPEC Analysis empty). Its
  ledger row is **parked** accordingly. This is the real Phase-1 blocker: `/learn-theme Q2` sits at
  stage 4, which is the user's to do — solve `./gradlew :p1-02-lost-update:test` to GREEN (pick one
  of optimistic `@Version` + bounded retry / pessimistic `FOR UPDATE` / atomic `UPDATE … SET
  balance = balance - :amount`) and write the Analysis. Consider a short re-teach first, since the
  08-17 teach evidently didn't land. Then stage 5 review, and only then does Q2 enter retention.
- Remaining in Phase 1 after Q2: **Q1 re-teach** (p1-01 RED) and **saga** (untaught, no cell yet),
  then the phase capstone.
Pacing signal (2026-08-26): the user spent ~1 h on two themes and stopped, asking for shorter
answers and offering to take follow-up questions instead of writing everything out. Retention runs
should be capped at **2 themes / ~25 min** unless he asks for more, and questions should target one
mechanism each rather than inviting an essay. The 9-day gap is what put four themes in the queue
at once — shorter, more frequent sessions beat long catch-ups.
Deferred code follow-ups on p1-03 (fold into a retention pass, not a new exercise): dedup-table
retention (`createdAt` + cleanup, bounded below by the relay's unbounded republication) is still
unimplemented in both consumers.
Session pacing note (2026-08-09, still active): short teach blocks, RU allowed for write-ups,
land terms on code he already wrote.
