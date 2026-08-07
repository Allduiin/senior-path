# p1-03 — The outbox arc, stage A: dual-write → transactional outbox

| | |
|---|---|
| **Phase** | 1 — Distributed systems & transactional correctness |
| **Targets diagnostic** | **Q6** now; the module is the shared **Q6+Q7+Q8 arc** — stage B (idempotent consumer, Q7) and stage C (delivery semantics, Q8) will extend it after their teach |
| **Start state** | RED — the crash test fails by design |
| **Done state** | GREEN — the event survives a crash between DB commit and publish |

## Objective
Reproduce the **dual-write problem** in a payment-capture flow and eliminate it with a
**transactional outbox + relay**: the event becomes durable in the *same* ACID transaction as the
state change, and an asynchronous relay publishes it to RabbitMQ. KB note:
[transactional-outbox.md](../../docs/knowledge-base/phase-1-distributed-tx/transactional-outbox.md).

## Scenario
`PaymentService.capture(orderId, amountMinor)` does what most codebases do:

1. **Commit point #1** — `TransactionTemplate` commits the `Payment` row to Postgres.
2. `CrashPoint.maybeCrash(AFTER_COMMIT_BEFORE_PUBLISH)` — a test hook modelling the process
   dying at the worst possible moment. **Keep this call in the post-commit path of `capture`.**
3. **Commit point #2** — `RabbitTemplate.convertAndSend` publishes `payment.captured`.

The test arms the crash point: the payment commits, the publish never happens, and the event is
**lost** — `crash between DB commit and publish must not lose the event` fails by design. A third
test (`failed duplicate capture …`) watches for **ghost events** and blocks the fake fix of
publishing inside the DB transaction.

## Tasks
1. **Diagnose (Analysis).** Before coding: name both dual-write failure windows and the symptom
   of each; explain why moving the publish *inside* the transaction is not a fix (which test
   catches it, and via which mechanism?); explain why publisher confirms wouldn't help either.
2. **Outbox write.** Design an outbox table as a JPA `@Entity` (schema is created by
   `ddl-auto: create-drop` in tests). Recommended columns are in the KB note (`id` monotonic PK,
   `event_type`, `payload` authored in-tx, `created_at`, `published_at` nullable). Change
   `capture` so the payment **and** the outbox row are written in **one** transaction, and the
   inline publish (commit point #2) is gone.
3. **Relay.** A `@Scheduled` poller (scheduling is already enabled on `Application`): select
   unpublished rows in insert order, publish each to `PaymentEvents.EXCHANGE` /
   `CAPTURED_ROUTING_KEY`, then mark published. Poll interval ≤ 1 s (the test waits 10 s).
   Decide consciously: what happens if the relay crashes *between* publish and mark-published?
4. **Document (Analysis).** Polling relay vs CDC tradeoffs; why your pipeline is now
   at-least-once and what that demands of consumers (the bridge to stage B / Q7); the
   sequence-order ≠ commit-order pitfall and why your relay query must re-scan the
   `published_at IS NULL` predicate rather than resume after a max-id cursor.

## Acceptance criteria
- `./gradlew :p1-03-outbox-arc:test` is GREEN (Docker running).
- `crash between DB commit and publish must not lose the event`: the armed crash still throws
  `SimulatedCrashException`, the payment row exists, **and** the event reaches the queue within
  10 s — published by your relay, not by the request thread.
- `failed duplicate capture must never publish a ghost event`: exactly **one** event for the
  orderId — the rolled-back duplicate leaves no trace on the broker.
- Correct for the right reason: single commit point for state+event, async publish. Not by
  catching the crash, publishing in-tx, or editing tests.

## Constraints
- Do not edit the tests.
- Keep `CrashPoint.maybeCrash(AFTER_COMMIT_BEFORE_PUBLISH)` in `capture`, after the DB
  transaction commits — the crash test asserts the crash actually fired.
- The relay must publish the payload **as authored at write time** (the outbox row is the
  contract), not re-derive it from the `payments` table.
- Testcontainers Postgres + RabbitMQ only — no manual infra.
- The plugin hook forbids code comments: put your reasoning in the Analysis section here, and
  tag any deliberate comment with `// allow: code-comment <reason>`.

## Stretch goals
1. **Multi-instance relay.** Make the polling query safe for two concurrent relay instances
   (`FOR UPDATE SKIP LOCKED` via a native query). In the Analysis, explain what SKIP LOCKED
   buys over plain `FOR UPDATE` here (throughput vs blocked pollers), and why duplicates are
   still possible (and acceptable) even with it.
2. **Prove the max-id-cursor bug.** Add a scratch test (or a reasoned walkthrough in the
   Analysis with timestamps) showing that a relay resuming "after the highest id seen" skips an
   outbox row whose transaction committed late — sequence order ≠ commit order.

## How to run
```
./gradlew :p1-03-outbox-arc:test
```
(Docker must be running for Testcontainers.)

---

## Analysis (you fill this in)
> _TODO: both dual-write windows + symptoms; why in-tx publish and publisher confirms are
> non-fixes; your outbox schema and why; the relay's duplicate window ⇒ at-least-once ⇒ what
> stage B must add; polling vs CDC; sequence-vs-commit-order pitfall and your query's defense._
