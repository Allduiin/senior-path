# p1-03 — The outbox arc (Q6 → Q7 → Q8)

| | |
|---|---|
| **Phase** | 1 — Distributed systems & transactional correctness |
| **Targets diagnostic** | Shared **Q6+Q7+Q8 arc**. **Stage A — Q6** (transactional outbox) ✅ REVIEWED 2026-08-09 · **Stage B — Q7** (idempotent consumer) ← current · **Stage C — Q8** (delivery semantics) pending its teach |
| **Run** | `./gradlew :p1-03-outbox-arc:test` (Docker required) |

---

# Stage A — dual-write → transactional outbox (Q6) ✅

| | |
|---|---|
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

## Analysis — stage A (filled in 2026-08-09)
> _TODO: both dual-write windows + symptoms; why in-tx publish and publisher confirms are
> non-fixes; your outbox schema and why; the relay's duplicate window ⇒ at-least-once ⇒ what
> stage B must add; polling vs CDC; sequence-vs-commit-order pitfall and your query's defense._

### Dual write problems
- Lost event: when the first write operation is done and before or during the second write operation was thrown any problem, the second raw can be lost, so in our case with rabbit we will never notify rabbit about save payment
- Ghost event: Problem that will be in case if we haven't able to save event to db(in case of exception), but rabbit did send a message to queue that payment was done
### Why the obvious fixes are fake
Publish inside transaciton is handled by ghost test, mechanism is: Started transaction → published message → transaction problem → rolled back changes → a message is already published

Publisher confirms гарантируют получение брокером сообщения (подтверждение brokerа → publisher'у,
покрывают ногу «сервис → брокер»), но не решают dual-write: не спасают ни когда транзакция
откатилась после publish (ghost event), ни когда запись закоммитилась, а до publish дело не
дошло — процесс умер, publish не начался, и подтверждать нечего (lost event).

### My solution
The solution is to create an outbox table for the event and save events at the same transaction as payment, and after that asynchronously handle this saved event
and send them to the queue.
- Payload must be authored at writing time for the reason event entity should have all information about what and how should be sent
- Id is for correct ordering of events, so we will have no problems when newer events publish earlier than older one
- exchange, routingKey, and message are required to send event
- sentAt is a mark column to mark the event as sent and in the future ready to be deleted
- Two saves must be in the same transaction for a reason, so if something wrong with db writes, all is rolled back, and in case if payment save we sure that event also saves and will be handled in the future, so we will have no problem in case one is saved and seconds have not been published
## The relay's honest contract
- If relay dies after publish operation, mark-sent will not mark and at the next check it will send one more time, so we guarantee at least one sent message, but it is not guaranteed that it always will be only one, o consumers must be ready for this and ignore subsequent messages
## The tradeoffs you're carrying
- The main trade of is paying by more complex decision to have better passability we also have two problems with this case first is fail tolerance what do if one of scheduled events does not work correctly, 
So we have few ways 1: stop, 2: retries and after few retries mark this event unsent 3: skip for now and go throw next events to not block all
- Also, we have tradeoff with multithreading and performance do we require processing at strict order, or we cannot guarantee order but do it faster using multithreading and optimization; also, in case of multiple instances we should use limits and batches and locks to not have problems with when two different instances are simultaneously trying to publish event

---

# Stage B — the idempotent consumer (Q7)

| | |
|---|---|
| **Targets diagnostic** | **Q7** — idempotent consumption (keys, dedup store, the failure window) |
| **Start state** | RED — three tests in `IdempotentConsumerTest` fail by design |
| **Done state** | GREEN — one captured payment produces exactly one payout credit, no matter how many times its event is delivered |

## Objective
Stage A made the producer side **at-least-once**: your relay publishes, then marks sent, and a
crash between those two steps republishes the same event. Close the loop by building the
**consumer** that absorbs those duplicates — a dedup claim and the side effect committed in **one
local transaction**. KB note:
[idempotent-consumption.md](../../docs/knowledge-base/phase-1-distributed-tx/idempotent-consumption.md).

## Scenario
A second queue, `payment-captured-ledger`, is bound to the same exchange and routing key as the
stage-A queue (both queues get their own copy of every `payment.captured` event — the stage-A
tests keep polling their queue undisturbed).

Its consumer's job: **credit the merchant's payout ledger** with the captured amount by appending
a `PayoutEntry` row. The ledger is **append-only and accumulative** — a real payout ledger holds
many entries per order (capture, refund, fee), so `orderId` is *not* unique there and a unique
constraint is not available to you as a shortcut. Two deliveries of one event ⇒ two rows ⇒ the
merchant is paid twice. Dedup has to happen **before** the append.

Provided for you: `PayoutEntry` (entity), `PayoutLedgerRepository`, the queue + binding in
`RabbitConfig`, and a `PayoutLedgerConsumer` skeleton. Everything about **identity and dedup** is
yours to design.

Two crash points model the two failure modes:

| Label | Where | Models |
|---|---|---|
| `AFTER_PUBLISH_BEFORE_MARK` | already placed in your relay, between publish and mark-sent | The relay dying mid-pass ⇒ the same event is **published twice** |
| `AFTER_CLAIM_BEFORE_EFFECT` | **you place it** in the consumer, between claiming the key and crediting the ledger | The consumer dying mid-handle ⇒ does your claim survive a rollback it shouldn't? |

## Tasks
1. **Design the key (Analysis first).** The consumer needs an identifier that is **stable across
   redeliveries** and assigned by the producer. Decide what it is and how it travels — a field in
   the payload authored in the capture transaction, or an AMQP message property the relay sets
   from the outbox row. Write down why the AMQP `deliveryTag` and the `orderId` are *not*
   automatically the right answer here.
2. **Dedup store.** Add a table (JPA `@Entity`, schema via `ddl-auto: create-drop`) that records
   which keys this consumer has processed. It must live in **this** database. Claiming a key must
   be a **single atomic statement**, not `SELECT`-then-`INSERT` — decide what enforces that and
   how you detect "someone already owns this key".
3. **The consumer.** Turn `PayoutLedgerConsumer.onPaymentCaptured` into a listener on
   `PaymentEvents.LEDGER_QUEUE`. Claim the key and append the `PayoutEntry` **in one local
   transaction**; on a duplicate, skip the credit and let the message be acknowledged. Parse the
   payload with Jackson (`jackson-databind` is on the classpath).
4. **Place the crash point.** `crashPoint.maybeCrash(CrashPoint.AFTER_CLAIM_BEFORE_EFFECT, payload)`
   goes **after the claim, before the credit**. Decide consciously what must happen to the claim
   when the exception unwinds.
5. **Document (Analysis).** Your key and why; what your claim statement is and what makes it
   atomic; the two orderings of claim/side-effect that fail and the symptom of each; where the ack
   sits relative to your commit and which semantics that gives; what bounds the retention of the
   dedup table; what changes when the side effect is a PSP call instead of a local row.

## Acceptance criteria
- `./gradlew :p1-03-outbox-arc:test` is GREEN — **both** test classes (stage A must not regress).
- `captured payment is credited to the payout ledger exactly once` — one entry, right amount.
- `a republished event must not double-credit the ledger` — the relay publishes the same event
  twice; exactly one entry survives.
- `a crash between claiming the key and crediting must not lose the credit` — the first attempt
  dies mid-handle; the redelivery must still produce exactly one entry. **Zero entries means your
  claim outlived a rolled-back side effect** — the silent-loss failure.
- Correct for the right reason: an atomic claim + the side effect in one transaction. Not by
  catching the crash, disabling the listener, or editing tests.

## Constraints
- Do not edit the tests.
- Keep `crashPoint.maybeCrash(CrashPoint.AFTER_PUBLISH_BEFORE_MARK, it.message)` in the relay
  between publish and mark-sent.
- Do not put a unique constraint on `payout_ledger.orderId` and do not change `PayoutEntry` —
  the ledger is append-only by design; that shortcut would dedup one specific side effect instead
  of teaching the general mechanism.
- No in-memory `Set`, no Redis, no broker-side dedup — the store is a table in this database.
- Testcontainers Postgres + RabbitMQ only.
- The plugin hook forbids code comments: reasoning goes in the Analysis below; tag any deliberate
  comment with `// allow: code-comment <reason>`.

## Stretch goals
1. **Poison message + DLQ.** Bound redelivery attempts and route a permanently-failing message to
   a dead-letter queue. In the Analysis: why the dedup key must **not** stay claimed for a message
   that never succeeded, and how that interacts with your one-transaction rule.
2. **Concurrent delivery.** Raise listener concurrency and reason (in the Analysis, or with a
   scratch test) about two threads handling the same key at the same instant: which of them wins,
   what the loser observes, and why a `SELECT`-then-`INSERT` claim would let both through.

## How to run
```
./gradlew :p1-03-outbox-arc:test
```
(Docker must be running for Testcontainers.)

## Analysis — stage B (you fill this in)
> _TODO: your idempotency key and why (and why not `deliveryTag` / `orderId`); your claim
> statement and what makes it atomic; both failing orderings of claim vs side effect + symptoms;
> ack placement and the resulting semantics; dedup-table retention bound; what changes when the
> side effect is a PSP call._

### The key
- I took key as id of rabbit outbox table message event so it is unique for 
every event that msut be sent by rabbit, orderId identifies an order not event, for one order we can have many events and it is okay with buisness logic
- It is same for every duplication of sending so we can identidy the duplicate and if it wouldn't be we 
would have two identical charges at ledger, it is because we set it as unique key in dedup table and setting it at adding event to outbox table
- The dedup key is set to message_id of rabbit message and we took it at consumer side
- Not a delivery tag because it will be unique for every message even with same payload and event id, so we will have two unique keys for same event and will be troubles with duplicates

### The claim
- We claiming the key by atomic db operation so we can be sure that the key is not claimed by other thread, and done once
- The atomarity is achived by unique constraint on primary key of db
- If key is not empty write and fluh operation with persitance always new will return us exception in case we already have key
- Select-then-insert is not atomic operation because between them could be another insert of another thread or instance so we will have two identical charges at ledger

### Claim vs side effect
- Effect -> mark: money charged but not sent to cosumer, effect: duplication of charges
- mark of self transaciton -> effect: mark exists and have no charges, duplication will cause 0 charges
- mark and effect at same tranasction: only all on no one, so only 1 charge will be made

### The ack
- We are using rabbit mq so we are using ack from rabbit mq
- We have semantic of at-least-once delivery, so consumer must be able to handle duplicate messages
- In invert order at-most-once data can be not sent and lost 
- Duplicate can be fixed by deduplication of messages, but if we have no any messages we cannot fix at all
- ack sends the container in AcknowledgeMode.AUTO mode after the listener method returns normally, that is,
after the tx.execute commit. Nothing happens in the window between the commit and the ack, and a failure 
in this window results in a redelivery. The first bullet ("using RabbitMQ, therefore using RabbitMQ's ack")
doesn't convey anything—such lines only add confusion to Analysis.

### Retention of the dedup table
- Time to live of dedup table enteties must be limited by time that can be needed for sending second duplicated message to consumer
- If we delete it earlier, we will be able to have a case when the same message was duplicated and charged twice
- This part is not done for now as it is test practice project that not require this thing with adding new process and schduling
- We can add this part in the future if we will need it
- In our case we can have relay that can sent message circulary and endlessly if we will have problems with handling it and will not correctly work with it

### What changes when the side effect is a PSP call instead of a local row
- If we use PSP db tranaction is not shared on it so it cannot be rolled back in case of problems, and success commit to key table do not guarantee
that it was 100% successfully delivered to psp
- I think there must be a process that gurantees has been delivered to psp and option to rollback it in case of problems for example by state model CREATED -> PENDING -> COMPLETED
- With writes that are stuck must be checked status on psp side and after that update acordinaly, if there no event resend it, blind resend can lead to duplication on psp ide if it is not ready for it
- Psp needs idenpotent key to be able to handle duplicate messages
- Guarantees of message delivery and rollback are hard to achieve with external psp