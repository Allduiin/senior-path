# Knowledge Map — Coverage & Calibration

> Companion to `roadmap.md` and `progress-log.md`. Tracks per-question coverage across
> re-test cycles. Each re-test (every 2–4 weeks) appends a column.

## How to read this
- **Score 0–100**: 0 = no idea; 40 = correct instinct, no mechanism; 70 = mechanism +
  one tradeoff; 90+ = answered cold with tradeoffs ("senior" bar).
- A gap is **closed** when the score is ≥ 80 and held across two consecutive re-tests.
- **This file is the single owner of per-question scores and statuses** (see `CLAUDE.md` →
  one-owner-per-fact). Other docs point here; numbers only ever move here.
- **New cells** (blind spots discovered by `/assess` or in session) get the next sequential
  number — **Q13, Q14, …** — with pillar noted, and travel the same 8-stage flow as Q1–Q12.

> **Fresh start 2026-07-07.** The June pilot run is kept below as a **prior** (reference only).
> The next session runs a fresh cold baseline (Q1–Q12) that becomes the operative column.

## Per-question matrix
<!-- viz:questions -->

| Q | Topic | Pillar | Pilot 2026-06 (prior) | Baseline 2026-07 | Latest | Status |
|---|---|---|:--:|:--:|:--:|---|
| Q1 | Tx propagation / proxy self-invocation | Distributed/Spring | 40→80 | 50 | 50 | open |
| Q2 | Isolation levels & MVCC | Persistence | 35→55 | 55 | 55 | open |
| Q3 | N+1 / fetch strategies | Persistence | 55 | 45 | 45 | open |
| Q4 | Coroutine scopes & cancellation | Concurrency | 10 | 20 | 20 | open |
| Q5 | Virtual threads & pinning | Concurrency | 35 | 30 | 30 | open |
| Q6 | Transactional outbox | Distributed | 25 | 20 | 70 | open |
| Q7 | Idempotent consumption | Distributed | 30 | 5 | 82 | open |
| Q8 | Exactly-once / delivery semantics | Distributed | 0 | 0 | 84 | open |
| Q9 | Payment system design | System design | 25 | 35 | 35 | open |
| Q10 | Resilience patterns | Resilience | 40 | 10 | 10 | open |
| Q11 | Tail-latency diagnosis | Resilience/Obs | 0 | 40 | 40 | open |
| Q12 | equals/hashCode in collections | JVM/Lang | 40 | 65 | 65 | open |

Pilot baseline mean ≈ 28 (2026-06-14). **Operative baseline mean ≈ 31 (2026-07-28).**

## Re-test log
<!-- viz:retest-log -->
| Date | Trigger | Questions probed | Notes |
|---|---|---|---|
| 2026-06-14 | *(pilot)* Baseline diagnostic | Q1–Q12 | Initial calibration; superseded by fresh start. |
| 2026-06-16 | *(pilot)* Q1 teach + p1-01 solved | Q1 | 40→70; proxy-types + propagation taught. |
| 2026-06-16 | *(pilot)* Cold re-test Q1 + re-ask | Q1 | 70→80 provisional; gaps: `rollbackOnly` naming, proxy build vs interceptor timing. |
| 2026-06-17 | *(pilot)* Q2 re-taught + p1-02 solved + cold quiz | Q2 | 35→55; gaps: atomic current-read-under-lock, SSI for write skew, tradeoff axes. |
| — | **Fresh start 2026-07-07** — next entry is the new baseline | Q1–Q12 | Pilot rows above are priors, not operative scores. |
| 2026-08-09 | p1-03 stage A review + cold quiz (`/learn-theme` stage 5) | Q6 | 20→70. Cold: dual-write windows, single-tx atomicity, at-least-once + consumer consequence, publish-then-mark, PSP-call-as-third-commit-point (design corner). Sub-gaps: confirms cover only the running publish leg (missed the never-started-publish remainder); sequence≠commit-order / max-id-cursor skip (named retry half only). |
| 2026-08-16 | p1-03 stage C solved + review + cold quiz (`/learn-theme` stage 5) | Q8 | 35→84. Score built from code + Analysis + a cold 5-question quiz (run after the user challenged a number first taken from guided work — same stage-5-gate correction as Q7). Cold quiz result: Two Generals as the last-messenger argument ✓; **the producer/broker vs broker/consumer redelivery boundary answered cold and correctly** — the sub-gap that recurred through all three arc stages, now closed (app/outbox logic republishes on the producer leg; the broker redelivers the unacked message on the consumer leg); MANUAL ack ✓ and unprompted extended it to mid-method placement; Kafka EOS scope ✓ including that it never touches the Postgres effect. One residual: named the publisher-confirm **direction** backwards (said publisher→broker; the confirm travels broker→publisher, mirror of consumer→broker ack) — the same boundary, one precision slip. Built the fix unaided: dropped the seeded `AcknowledgeMode.NONE` factory for AUTO (ack after the method returns ⇒ effect→ack ⇒ at-least-once), then reused the stage-B composition to make the notification effectively-once. Nailed the shared-store trap without a hint — scoped the dedup key `{consumer}:{eventId}` so the notify and ledger consumers of one event don't starve each other (and gave the ledger key a symmetric prefix). Narrowed both catches to `DuplicateKeyException` (verified the translator actually produces it) so an integrity failure from the effect is no longer swallowed as a duplicate. Analysis, second pass clean after three corrections: **producer↔broker confusion returned** (wrote "producer retries" / "MANUAL ack at producer" — it is the broker that redelivers and the consumer that acks; same confusion as stage A publisher-confirms), and **Kafka EOS overreach** (first claimed EOS removes the notification duplicate — it removes only the producer's transport-retry dup, not the relay's re-publish nor the Postgres-bound effect). Both corrected. Also self-corrected the crash-point placement (had `AFTER_CLAIM_BEFORE_NOTIFY` outside the tx first — the exact stage-B mistake — moved it between claim and effect). |
| 2026-08-13 | Q8 pre-teach cold probe (`/learn-theme` stage 0 refresh) | Q8 | 0→35. Derived unprompted that ack placement relative to the effect is the axis (effect→ack = at-least-once, ack→effect = at-most-once) — carried over from the stage-B work. Gaps: proposed producer-queries-consumer as an exactly-once scheme (the exact regress Two Generals forbids); impossibility unknown as a proven property; Kafka EOS mechanism unknown (guessed broker-side id dedup — right direction, one of two features). Post-teach follow-up showed the classic overreach ("Kafka guarantees the consumer one copy, no dedup needed") — corrected with the duplicate-source table; Kafka→Kafka transactional read-process-write named as the only no-dedup case. |
| 2026-08-11 | p1-03 stage B solved + review + cold quiz (`/learn-theme` stage 5) | Q7 | 5→82. Built unaided after teach: producer-assigned key (outbox row PK) carried in AMQP `message_id`; claim as a single `INSERT` (`Persistable.isNew=true` ⇒ `persist`, atomicity from the PK constraint) committed in ONE local transaction with the ledger append; duplicate detected via `DataIntegrityViolationException` caught outside the tx boundary. Cold quiz, 5 questions, 4 clean: per-consumer dedup scope (+ `consumerName:eventId` composite when shared); relative accumulation vs absolute assignment (`balance = balance + n` needs dedup, `status = 'CAPTURED'` is naturally idempotent) **and** the late-duplicate hazard that resurrects a superseded state; concurrent claim resolution + the interleaving that defeats `SELECT`-then-`INSERT`; PSP effect with intent-record-before-call and the unknown-state residue. Sub-gaps: consumer-side commit→ack window needed a re-ask (twice drifted into the relay's publish→mark-sent window) and redelivery-on-channel-loss was framed as retry-policy-dependent rather than protocol-guaranteed; moment the key is assigned (Analysis stated "at send time" three times before correction); exception classification breadth (`DataIntegrityViolationException` also swallows an integrity failure from the side effect). Practical traps hit: Spring Data `save()` ⇒ `merge` for assigned ids (silent select-then-update, the double-credit bug); no `ObjectMapper` bean without spring-web. |
| 2026-07-28 | **Fresh baseline** (`/assess`, baseline mode) | Q1–Q12 | Operative column set; mean ≈ 31. Q8 skipped→0, Q7/Q10 honest blanks. Persisting pilot gaps confirmed: `rollbackOnly`/`UnexpectedRollbackException` (Q1), atomic current-read + optimistic `@Version` + SSI mechanism (Q2), pinning (Q5). New sub-gaps: batch fetching absent + EAGER≠join + HHH000104 in-memory pagination (Q3); per-collection (not per-item) lazy trigger (Q3); orphaned-entry/re-put duplication + immutable-key rules (Q12); idempotency-key insert-before-PSP-call + guarded state transitions (Q9); GC-pause & pool-exhaustion tail causes + traces→metrics→logs order (Q11). Pilot Q10 score (40) was inflated; honest blank is the truer reading. |
