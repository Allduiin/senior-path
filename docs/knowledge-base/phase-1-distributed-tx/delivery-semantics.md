# Delivery semantics & effectively-once

**Maps to:** Q8 (exactly-once / delivery semantics) · **Phase 1** · Exercise: shared Q6+Q7+Q8 arc
module, stage C ([p1-03-outbox-arc](../../../exercises/p1-03-outbox-arc/SPEC.md))
[← back to index](../README.md)

## TL;DR
There are **two** delivery semantics, not three, and which one you get is decided by a single
choice: **where the acknowledgement sits relative to the effect**. Exactly-once *delivery* is not
a missing feature — it is **provably impossible** over an unreliable channel (Two Generals).
Exactly-once *effect* is achievable, because the effect lives in storage you control. The
composition `at-least-once delivery + idempotent processing` is what the industry calls
**effectively-once**, and it is the only honest form. Systems advertising "exactly-once" (Kafka
EOS) implement a transaction over **their own** storage; the guarantee stops at their boundary.

## 1. The axis — ack placement

| Order | What sits in the window | Semantics | Failure mode |
|---|---|---|---|
| **ack → effect** | message already deleted, work not done | **at-most-once** | silent loss, unrecoverable |
| **effect → ack** | work done, broker not yet told | **at-least-once** | duplicate, absorbable by dedup |

There is no third row. Any protocol claiming one is either doing the composition below or lying.

**Asymmetry that decides the choice:** a duplicate is repairable by the receiver; a loss is
repairable by nobody. In payments this is not a preference — it is the only defensible option.

### Getting each one in Spring AMQP
| Want | Configuration | Mechanism |
|---|---|---|
| at-least-once (default) | `acknowledge-mode: auto` | Container acks **after the listener method returns normally** — i.e. after the transaction commits |
| at-most-once | `acknowledge-mode: none` | Broker auto-acks on dispatch; nothing is ever redelivered |
| at-most-once (manual) | `acknowledge-mode: manual` + `channel.basicAck(...)` as the **first** statement | Same window, opened by hand |

`AUTO` is not the broker's auto-ack. `NONE` is.

## 2. Why exactly-once delivery is impossible — Two Generals (1975)

Two generals on opposite hills; the enemy in the valley between. Attacking together wins,
attacking alone loses. The only channel is messengers through the valley, each of whom may be
captured. Goal: agree on a common attack time.

**Proof by contradiction, on the last message.** Suppose a protocol guarantees agreement. Take
the **shortest** message sequence for which both attack. Its final message may be lost, and the
sender cannot know whether it arrived.

| Case | Consequence |
|---|---|
| Receiver attacks anyway | The final message was unnecessary ⇒ the sequence was not the shortest. Contradiction |
| Receiver does not attack | The protocol fails to guarantee agreement. Contradiction |

So no such protocol exists. **No finite number of acknowledgements produces common knowledge over
a lossy channel.** Each extra round moves the uncertainty to the newest message; it never removes
it.

**Corollary for the "just ask the consumer" idea.** A producer that queries the consumer ("did you
process event X?") has replaced *"did my message arrive?"* with *"did the answer about my message
arrive?"*. Same channel, same lossiness, one hop further out.

**Second, independent window.** Even over a perfect network, the *process* can die between
applying the effect and sending the acknowledgement. The gap relocates inside one machine, where
there is no network to blame.

### The reframe that resolves everything
> Exactly-once **delivery** is impossible — delivery is a property of the wire.
> Exactly-once **effect** is achievable — the effect is a property of your state.

You do not control the wire. You do control the database.

## 3. effectively-once = at-least-once + idempotent processing

Not a third delivery semantic — a **composition** of a weak transport guarantee with a property of
the handler.

| Ingredient | Where it lives | Note |
|---|---|---|
| at-least-once transport | relay publishes before mark-sent; ack after commit | → [transactional-outbox](transactional-outbox.md) |
| producer-assigned stable key | outbox row PK, carried in the message | → [idempotent-consumption](idempotent-consumption.md) |
| atomic claim | single `INSERT`, uniqueness enforced by a constraint | not `SELECT`-then-`INSERT` |
| claim + effect inseparable | one local transaction | shared fate is the whole point |

Remove any row and the property collapses. Q6, Q7 and Q8 are three descriptions of one machine.

## 4. Why confirms + ack do not add up to a guarantee

The pipeline is four hops; each has its own window and its own remedy.

| Hop | Closed by | Window that remains |
|---|---|---|
| app → own DB | ACID transaction | — |
| DB → broker | publisher confirms | the publish that **never started** (process died before it) |
| broker → consumer | consumer ack | effect applied, ack not sent |
| consumer → its DB | ACID transaction | — |

Publisher confirms answer *"did the broker receive the bytes I sent?"* — they say nothing about
bytes never sent. Acks answer *"was it processed?"* — nothing about what happens after. The two
remaining gaps are closed by **state in a database** (outbox on the left, dedup on the right), not
by more acknowledgements. Acknowledgements are about *arrival*; the requirement is *applied
exactly once*. Different questions.

## 5. Kafka EOS — what is actually guaranteed

Two independent features, routinely conflated.

| Feature | Enabled by | Mechanism | Scope |
|---|---|---|---|
| **Idempotent producer** | `enable.idempotence=true` | Broker tracks `producer-id` + per-partition monotonic `sequence number`; a retransmit with a seen number is dropped | One producer **session**, one partition |
| **Transactions** | `transactional.id`, `beginTransaction` / `sendOffsetsToTransaction` / `commitTransaction` | Atomic write across partitions **including the consumer's offset commit**; readers use `isolation.level=read_committed` | Records and offsets **inside Kafka** |

### Which duplicate sources it actually removes
| Source | Removed? |
|---|---|
| Producer network retry (broker stored it, the ack was lost) | **Yes** — the idempotent producer's job |
| The application publishing the same logical event twice (relay crash between publish and mark-sent) | **No** — a fresh `send()` with a fresh sequence number; the broker sees two unrelated records |
| Consumer crash after processing, before the offset commit | **No**, unless the whole read-process-write is transactional |
| Consumer-group rebalance mid-batch | **No**, same reason |

**Session caveat:** without `transactional.id`, a restarted producer receives a **new**
`producer-id`, so sequence-number dedup cannot recognise anything from the previous session. It
protects against retries within a session, not against process restarts.

### The one honest exactly-once case
**Kafka → Kafka.** Transactional read-process-write with the offset committed inside the same
transaction as the output records, consumed downstream with `read_committed`. This genuinely
closes the third row above; it is what Kafka Streams' `processing.guarantee=exactly_once_v2` runs
on.

**Its condition is absolute: every effect must live inside Kafka.** The moment processing writes
to Postgres, calls a PSP, or sends an email, the broker's transaction covers none of it and the
consumer needs its own dedup.

> Kafka EOS is exactly-once **processing within Kafka**, not exactly-once **delivery**. It does not
> contradict Two Generals: it is not a wire guarantee, it is a transaction over one system's
> storage — the same trick as claim-plus-effect in one local transaction, at cluster scale.

**The field error to avoid:** *"we're on Kafka with EOS, we don't need idempotent consumers."*
Wrong whenever the service writes to its own database — i.e. almost always.

## 6. Vocabulary that survives review
| Term | Precise meaning |
|---|---|
| at-most-once | ack precedes the effect; loss possible, duplication impossible |
| at-least-once | effect precedes the ack; duplication possible, loss impossible |
| exactly-once delivery | does not exist over a lossy channel — Two Generals |
| effectively-once | at-least-once transport + idempotent effect; an end-to-end property, not a transport one |
| exactly-once processing | atomic state change **within one system's storage** (Kafka EOS, or your one-transaction claim+effect) |

## Self-check (answer cold)
1. Which single decision in the code determines the delivery semantics you get? Name the exact
   configuration change that turns your consumer into at-most-once.
2. State the Two Generals argument well enough to prove it — not describe it.
3. Why does adding a "did you receive it?" round trip fail to produce exactly-once?
4. Name the window that exists even on a perfect network.
5. Why is exactly-once *effect* achievable when exactly-once *delivery* is not?
6. Write out the effectively-once composition and the four ingredients your own pipeline supplies.
7. Which leg do publisher confirms close, and which two gaps do they leave? What closes those?
8. Kafka EOS: name the two features, what each removes, and the duplicate sources neither removes.
9. Under exactly what condition is "no dedup needed" true on Kafka?
10. Why does a restarted producer without `transactional.id` lose its dedup protection?
