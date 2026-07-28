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

| Q | Topic | Pillar | Pilot 2026-06 (prior) | Baseline 2026-07 | Latest | Status |
|---|---|---|:--:|:--:|:--:|---|
| Q1 | Tx propagation / proxy self-invocation | Distributed/Spring | 40→80 | 50 | 50 | open |
| Q2 | Isolation levels & MVCC | Persistence | 35→55 | 55 | 55 | open |
| Q3 | N+1 / fetch strategies | Persistence | 55 | 45 | 45 | open |
| Q4 | Coroutine scopes & cancellation | Concurrency | 10 | 20 | 20 | open |
| Q5 | Virtual threads & pinning | Concurrency | 35 | 30 | 30 | open |
| Q6 | Transactional outbox | Distributed | 25 | 20 | 20 | open |
| Q7 | Idempotent consumption | Distributed | 30 | 5 | 5 | open |
| Q8 | Exactly-once / delivery semantics | Distributed | 0 | 0 | 0 | open |
| Q9 | Payment system design | System design | 25 | 35 | 35 | open |
| Q10 | Resilience patterns | Resilience | 40 | 10 | 10 | open |
| Q11 | Tail-latency diagnosis | Resilience/Obs | 0 | 40 | 40 | open |
| Q12 | equals/hashCode in collections | JVM/Lang | 40 | 65 | 65 | open |

Pilot baseline mean ≈ 28 (2026-06-14). **Operative baseline mean ≈ 31 (2026-07-28).**

## Re-test log
| Date | Trigger | Questions probed | Notes |
|---|---|---|---|
| 2026-06-14 | *(pilot)* Baseline diagnostic | Q1–Q12 | Initial calibration; superseded by fresh start. |
| 2026-06-16 | *(pilot)* Q1 teach + p1-01 solved | Q1 | 40→70; proxy-types + propagation taught. |
| 2026-06-16 | *(pilot)* Cold re-test Q1 + re-ask | Q1 | 70→80 provisional; gaps: `rollbackOnly` naming, proxy build vs interceptor timing. |
| 2026-06-17 | *(pilot)* Q2 re-taught + p1-02 solved + cold quiz | Q2 | 35→55; gaps: atomic current-read-under-lock, SSI for write skew, tradeoff axes. |
| — | **Fresh start 2026-07-07** — next entry is the new baseline | Q1–Q12 | Pilot rows above are priors, not operative scores. |
| 2026-07-28 | **Fresh baseline** (`/assess`, baseline mode) | Q1–Q12 | Operative column set; mean ≈ 31. Q8 skipped→0, Q7/Q10 honest blanks. Persisting pilot gaps confirmed: `rollbackOnly`/`UnexpectedRollbackException` (Q1), atomic current-read + optimistic `@Version` + SSI mechanism (Q2), pinning (Q5). New sub-gaps: batch fetching absent + EAGER≠join + HHH000104 in-memory pagination (Q3); per-collection (not per-item) lazy trigger (Q3); orphaned-entry/re-put duplication + immutable-key rules (Q12); idempotency-key insert-before-PSP-call + guarded state transitions (Q9); GC-pause & pool-exhaustion tail causes + traces→metrics→logs order (Q11). Pilot Q10 score (40) was inflated; honest blank is the truer reading. |
