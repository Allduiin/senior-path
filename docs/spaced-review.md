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
  what's due (and how many) and offers to run `/repeat-knowledge`.
- Run **`/repeat-knowledge`** (or say "review") any time to quiz due themes now.

### Session budget (scales to many themes)
A run reviews at most **8 themes** (≈15 min); the rest stay due and carry to the next run.
Priority: lapsed / lowest-EF → most overdue → soonest due. Override with `/repeat-knowledge N`
or `/repeat-knowledge all`. Because SM-2 spreads `next_due` out as themes mature, steady-state
load stays small even at 30–50 themes; the budget only bites during ramp-up or after a gap.

## Ledger
> **Fresh start 2026-07-07** — ledger reset (pilot rows archived in git history). Themes re-enter
> as they are re-taught with a KB note (EF 2.50, reps 0). Q1/Q2 KB notes already exist from the
> pilot; those themes re-enter after their fresh teach/review, not automatically.

<!-- viz:ledger -->
| Theme | Q | KB note | EF | reps | interval (d) | Last reviewed | Next due | History |
|---|:--:|---|:--:|:--:|:--:|:--:|:--:|---|

> Add a row when a new theme gets a KB note (enters at EF 2.50, reps 0, due in 7 d after its
> first successful review).

## Automation
**Current: session-start surfacing** — due themes are surfaced when a session opens here, plus
on-demand via `/repeat-knowledge`. **Planned: Telegram notifications** (decided 2026-07-07) — a
proactive ping when something is due, to be built later; record the integration details here when
it lands. Until then, open a session every several days to stay on the curve.

## Review log
<!-- viz:review-log -->
| Date | Themes reviewed | Grade → q | EF/interval change |
|---|---|---|---|
