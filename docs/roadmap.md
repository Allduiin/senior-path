# Roadmap to Senior Java/Kotlin Backend Engineer

**Calibrated from diagnostic (12 questions, 14 June 2026).**
Start: competent mid-level (~L3), strong production hands-on, payments/fintech context.
Assumption: ~6–10 hrs/week, ~18-month horizon, front-loaded by leverage. Adjust to actual hours.

Related files: `progress-log.md` (live status), `knowledge-map.md` (coverage + calibration).

---

## Operating principles
1. **Theory on top of intuition.** The diagnostic showed correct instincts without the
   formal mechanism. The plan attaches names, mechanisms, and tradeoffs to things I
   half-know — the fast path.
2. **Close the distributed / transactions cluster first.** It's the biggest gap and the
   most valuable for payments; closing it lifts six diagnostic answers at once.
3. **Learn by building.** Every phase has a project that physically reconstructs a missed
   diagnostic question.
4. **Measure continuously.** Re-assess every **2–4 weeks** via `knowledge-map.md`: confirm
   closed gaps, probe untested areas to find new blind spots, recalibrate levels across all
   tests.

---

## Phase 1 — Distributed systems & transactional correctness (Months 1–4)
**Why first:** highest leverage, payments-critical, fixes the largest cluster of gaps.

**Topics**
- Idempotency: keys, dedup stores, unique-constraint-as-dedup, the side-effect/record-key failure window (Q7).
- Delivery semantics: at-least-once vs at-most-once; why exactly-once *delivery* is impossible; effectively-once = at-least-once + idempotent processing (Q8).
- Transactional outbox: the dual-write problem, outbox table in the same tx, the relay; outbox vs CDC (Q6).
- Saga pattern (orchestration vs choreography); why 2PC/distributed transactions are avoided.
- Isolation & MVCC: the levels, the anomalies each prevents, Postgres snapshot isolation vs InnoDB REPEATABLE READ, optimistic vs pessimistic locking (Q2).
- Spring proxy AOP & self-invocation; propagation behaviors end-to-end (Q1).

**Resources**
- *Designing Data-Intensive Applications* (Kleppmann) — ch. 7–9, 11. The spine of this roadmap.
- microservices.io (Chris Richardson) — outbox, saga, idempotent consumer patterns.
- *High-Performance Java Persistence* (Vlad Mihalcea) — transactions, isolation, locking.

**Project**
A small payment-style service: idempotent `POST /payments`, an outbox table written in the
same DB transaction, a relay that publishes to RabbitMQ, a consumer with a dedup store.
Then deliberately send duplicate messages and kill the process between DB commit and
publish; observe the lost event; fix with outbox.

**Done when:** Q1, Q2, Q6, Q7, Q8 answered cold, with tradeoffs.

---

## Phase 2 — Concurrency: JVM + Kotlin (Months 4–7)
**Why:** the deepest gap; underpins everything else.

**Topics**
- Java Memory Model: happens-before, visibility, `volatile`, atomics, `synchronized`, contention cost.
- Virtual threads: what they help (blocking I/O), what they don't (CPU-bound), and **pinning** (`synchronized`/native calls defeat them; fix with `ReentrantLock`) (Q5).
- Structured concurrency & scoped values (Java 21/25).
- Kotlin coroutines: dispatchers, `coroutineScope` vs `supervisorScope`, cancellation, `Flow` & backpressure (Q4).

**Resources**
- *Java Concurrency in Practice* (Goetz) — canonical for the JMM; pre-Loom, so pair with virtual-thread JEPs (444, 453) and JDK docs.
- *Kotlin Coroutines: Deep Dive* (Marcin Moskała) + kotlinx.coroutines docs.

**Project**
Load generator with a fixed platform-thread pool vs virtual threads under blocking I/O.
Reproduce pinning by wrapping a blocking call in `synchronized`. Build a coroutine pipeline
that cancels cleanly end-to-end.

**Done when:** Q4, Q5 answered cold; can explain why VTs don't help CPU-bound work.

---

## Phase 3 — JVM internals, performance & persistence depth (Months 7–10)

**Topics**
- GC: G1 vs ZGC, pause behavior, choosing; reading GC logs.
- JIT, escape analysis, allocation; profiling (JFR, async-profiler), reading a flame graph.
- Hibernate internals: flush timing, dirty checking, L1 vs L2 cache and a correctness
  pitfall, fetch strategies incl. **batch fetching** (Q3).
- `equals`/`hashCode` contract with hash-based collections and mutable fields (Q12).

**Resources**
- *High-Performance Java Persistence* (Mihalcea) — the rest of it.
- *Optimizing Java* (Evans/Gough) or JFR/async-profiler docs.

**Project**
Profile the Phase-1 service under load. Find an N+1 and a GC/allocation issue, fix each,
and **measure** before/after.

**Done when:** Q3, Q12 answered cold; can read a flame graph.

---

## Phase 4 — System design & architecture (Months 10–14)
**Why:** the single largest senior delta. Started informally in Phase 1; now deliberate.

**Topics**
- Service boundaries, API/contract design, REST versioning.
- Event-driven vs request/response; consistency models per use case.
- Capacity estimation (back-of-envelope), failure-mode-first design.
- ADRs: decisions with tradeoffs and consequences.

**Resources**
- DDIA (revisit). *System Design Interview* vol. 1–2 (Alex Xu). microservices.io. Real ADRs at work.

**Project**
Full design doc for the payment authorization service (the Q9 prompt): idempotent API,
payment state machine, PSP-timeout reconciliation (poll + webhook), consistency model,
retry strategy. Review against a senior design checklist.

**Done when:** Q9 runs as a 20-minute design-review walkthrough covering all four sub-parts.

---

## Phase 5 — Resilience, observability & production ops (Months 14–18)

**Topics**
- Resilience4j: retry, timeout, circuit breaker (auto-recovery state machine), **bulkhead
  as resource isolation** (not a feature flag) (Q10).
- Safe retries in payments: idempotency + jittered backoff + budgets.
- Observability: metrics/logs/traces, OpenTelemetry, Micrometer.
- Tail-latency diagnosis: the p99-spike/flat-p50 signature and its causes (Q11).
- SLOs, error budgets, incident response, blameless postmortems.

**Resources**
- *Release It!* (Michael Nygard). Google SRE Book (free online). OpenTelemetry docs.

**Project**
Instrument the service with OpenTelemetry, build a dashboard, run a game-day injecting a
slow dependency, diagnose the p99 spike. Add a correctly-configured bulkhead + circuit breaker.

**Done when:** Q10, Q11 answered cold; diagnosed a real tail-latency event.

---

## Continuous (all phases)
- Do a real Spring Boot 3 → 4 (Framework 7, Jackson 3) / Java 25 migration of one module.
- Track Kotlin 2.4 features (context parameters now stable).
- One engineering blog post or paper per week in weak domains.

---

## A note on what "senior" actually means
Hard skills are necessary but not sufficient. Senior is also **scope of ownership** (you
drive designs, not just implement them), **judgment under ambiguity**, **mentoring**, and
**influencing decisions**. This plan optimizes hard skills — pair it with owning a hard
problem end-to-end at work, ideally in the distributed/payments space where this roadmap
makes you strongest.
