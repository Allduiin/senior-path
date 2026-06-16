# Spaced Review — retention ledger

> Lightweight, frequent recall to beat the forgetting curve. **Distinct from**
> `knowledge-map.md` (deep re-assessment every 2–4 weeks): this is short, per-theme, and
> scheduled by expanding intervals. The mentor owns and updates this file.

## How it works (Leitner / expanding intervals)
A theme enters the ledger once it has been **taught and has a knowledge-base note**. Each
theme sits in a **box**; the box sets how long until its next review:

| Box | Interval to next review |
|:--:|---|
| 1 (new / lapsed) | 7 days |
| 2 | 14 days |
| 3 | 30 days |
| 4 | 60 days |
| 5 | 120 days |
| 6 (mature) | 240 days |

**On a review** (3–5 quick recall questions per due theme, answered cold), grade with the
`knowledge-map.md` rubric (0–100) and move the box:

- **Recall cold (≥ 80)** → **promote**: `box = min(box+1, 6)`; `next_due = review_date + new interval`.
- **Shaky (50–79)** → **stay**: same box; `next_due = review_date + same interval`.
- **Forgot (< 50)** → **lapse**: `box = 1`; `next_due = review_date + 7d`; add to `progress-log.md` weak spots.

A theme is "**retained**" once it reaches **Box 5+**.

## Triggers
- **Every session start**, the mentor scans this ledger and surfaces any theme with
  `next_due ≤ today` ("N reviews due / overdue").
- Say **"review"** (or "run review") any time to quiz all due themes now.
- Optionally, a scheduled agent pings on a cadence (see `## Automation` below) so reviews
  reach you without asking.

## Ledger
Today's reference date for seeding: **2026-06-16**.

| Theme | Q | KB note | Box | Last reviewed | Next due | History |
|---|:--:|---|:--:|:--:|:--:|---|
| Spring proxy AOP & `@Transactional` propagation | Q1 | [link](knowledge-base/phase-1-distributed-tx/spring-proxy-and-transactions.md) | 1 | 2026-06-16 (closed @80) | **2026-06-23** | 2026-06-16 learned+closed (cold @80) → Box 1 |

> Add a row when a new theme gets a KB note. When Phase-2+ themes are taught they land here at Box 1.

## Automation
_Status: not yet scheduled._ Options for the proactive "ping":
- **Session-start surfacing** (always on, no infra): the ritual above. Reliable but only fires
  when you open a session.
- **Scheduled agent** (optional): a routine that runs on a cadence (e.g. weekly), reads this
  ledger, and quizzes any due theme. Configure via the `schedule` skill. Record the routine
  id/cadence here once set up.

## Review log
| Date | Themes reviewed | Outcome |
|---|---|---|
| _none yet_ | | |
