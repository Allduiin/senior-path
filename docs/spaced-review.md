# Spaced Review — retention ledger (SM-2)

> Lightweight, frequent recall to beat the **Ebbinghaus forgetting curve**. **Distinct from**
> `knowledge-map.md` (deep re-assessment every 2–4 weeks): this is short, per-theme, and
> scheduled by the **SM-2** algorithm. The mentor owns and updates this file.
> Run it with the **`/repeat-knowledge`** skill (or just say "review").

## The algorithm (SM-2, concept-tuned)
Each theme carries three SM-2 fields:
- **EF** — *ease factor* (how "easy" the item is). Starts **2.50**, floor **1.30**.
- **reps** — count of consecutive successful recalls.
- **interval** — days until the next review.

After each review, grade the cold recall **0–100** (rubric below), map it to an SM-2 **quality
`q` (0–5)**, then update:

**1. Map grade → q**
| Grade | q | Meaning |
|---|:--:|---|
| 95–100 | 5 | perfect, instant, cold |
| 80–94 | 4 | correct, minor hesitation |
| 70–79 | 3 | correct but effortful (pass floor) |
| 55–69 | 2 | familiar but wrong |
| 30–54 | 1 | barely |
| 0–29 | 0 | blank |

**2. Update EF (always):**
`EF' = EF + (0.1 − (5 − q) × (0.08 + (5 − q) × 0.02))`, then clamp to ≥ 1.30.

**3. Update interval & reps:**
- **Pass (`q ≥ 3`)** — promote:
  - `reps == 0` → `interval = 7`
  - `reps == 1` → `interval = round(7 × EF')`  (≈ 18 d at EF 2.5)
  - `reps ≥ 2` → `interval = round(interval_prev × EF')`
  - then `reps += 1`
- **Fail (`q < 3`)** — lapse: `reps = 0`, `interval = 2` (relearn next session-ish), and add/raise
  the theme in `progress-log.md` "Open weak spots". EF still takes the downward step above.
- `next_due = review_date + interval`

So a theme you keep recalling cold marches **7 d → ~18 d → ~45 d → ~3.5 mo → ~9 mo …** (faster
if EF rises toward 2.6–2.7, slower if it sinks). You will **not** re-answer the same question
every few days unless you actually forget it.

> Tuning note: canonical SM-2 uses first intervals of 1 d then 6 d. We start at **7 d** (then
> `7 × EF`) because this is conceptual material reviewed at ~6–10 h/week, not flashcard
> vocabulary. The EF mechanics — the real science lever — are unchanged.

## Grading rubric (shared with knowledge-map)
0 = none · 40 = correct instinct, no mechanism · 70 = mechanism + one tradeoff · 90+ = cold with tradeoffs.

## Triggers
- **Every session start** the mentor scans this ledger; if any `next_due ≤ today`, it announces
  what's due and offers to run `/repeat-knowledge`.
- Run **`/repeat-knowledge`** (or say "review") any time to quiz all due themes now.

## Ledger
Reference date for seeding: **2026-06-16**.

| Theme | Q | KB note | EF | reps | interval (d) | Last reviewed | Next due | History |
|---|:--:|---|:--:|:--:|:--:|:--:|:--:|---|
| Spring proxy AOP & `@Transactional` propagation | Q1 | [link](knowledge-base/phase-1-distributed-tx/spring-proxy-and-transactions.md) | 2.50 | 1 | 7 | 2026-06-16 | **2026-06-23** | 2026-06-16 closed cold @80 (q4) → EF 2.50, reps 1, +7d |

> Add a row when a new theme gets a KB note (enters at EF 2.50, reps 0, due in 7 d after its
> first successful review).

## Automation
**Chosen: session-start surfacing only** (2026-06-16). No scheduled cloud agent — due themes are
surfaced when a session opens here, plus on-demand via `/repeat-knowledge`. Reliable, no infra; the
trade-off is it fires only when you start a session, so open one every several days to stay on
the curve. _If a proactive ping is wanted later:_ preferred cadence **every 3 days**, silent
unless something is due — set up via the `schedule` skill and record the routine id here.

## Review log
| Date | Themes reviewed | Grade → q | EF/interval change |
|---|---|---|---|
| _none yet_ | | | |
