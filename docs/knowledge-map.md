# Knowledge Map — Coverage & Calibration

> Companion to `roadmap.md` and `progress-log.md`. Tracks per-question coverage across
> re-test cycles. Each re-test (every 2–4 weeks) appends a column.

## How to read this
- **Score 0–100**: 0 = no idea; 40 = correct instinct, no mechanism; 70 = mechanism +
  one tradeoff; 90+ = answered cold with tradeoffs ("senior" bar).
- A gap is **closed** when the score is ≥ 80 and held across two consecutive re-tests.

## Per-question matrix

| Q | Topic | Pillar | Baseline 2026-06-14 | Latest | Status |
|---|---|---|:--:|:--:|---|
| Q1 | Tx propagation / proxy self-invocation | Distributed/Spring | 40 | 40 | open — `p1-01` issued |
| Q2 | Isolation levels & MVCC | Persistence | 35 | 35 | open |
| Q3 | N+1 / fetch strategies | Persistence | 55 | 55 | open |
| Q4 | Coroutine scopes & cancellation | Concurrency | 10 | 10 | open |
| Q5 | Virtual threads & pinning | Concurrency | 35 | 35 | open (inverted intuition) |
| Q6 | Transactional outbox | Distributed | 25 | 25 | open |
| Q7 | Idempotent consumption | Distributed | 30 | 30 | open |
| Q8 | Exactly-once / delivery semantics | Distributed | 0 | 0 | open |
| Q9 | Payment system design | System design | 25 | 25 | open |
| Q10 | Resilience patterns | Resilience | 40 | 40 | open (bulkhead confused) |
| Q11 | Tail-latency diagnosis | Resilience/Obs | 0 | 0 | open |
| Q12 | equals/hashCode in collections | JVM/Lang | 40 | 40 | open |

Baseline mean ≈ 28.

## Re-test log
| Date | Trigger | Questions probed | Notes |
|---|---|---|---|
| 2026-06-14 | Baseline diagnostic | Q1–Q12 | Initial calibration. |
