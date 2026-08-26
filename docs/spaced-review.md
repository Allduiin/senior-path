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

### Session budget (revised 2026-08-26 — counted in questions, not themes)
A run asks at most **10 questions**. Each theme contributes **3–5** (its own count, set by how
fragile it is), so a run holds **2–3 themes**. This replaces the old 8-theme cap, which allowed a
40-question run — the 2026-08-26 session ran ~1 h on two themes and was cut short, which is the
evidence behind the change.

**Whole themes only — never split a theme across runs.** Pack in priority order, adding each
theme *entire* while the running total stays ≤ 10; when the next theme would push past 10, stop.
The remainder carries to a **follow-up run the next day**, not to "whenever".

> **Worked example.** Four themes due, needing 5 + 4 + 3 + 4 questions (16 total).
> Run 1 = **5 + 4 = 9** — adding the 3 would make 12, over the cap, so it stops at two themes.
> Run 2 (next day) = **3 + 4 = 7**. Never 5 + 4 + 1 / 2 + 3 + 4.

**Priority** for the packing order: ① lapsed / lowest-EF first (most at-risk) → ② most overdue
(largest `today − next_due`) → ③ soonest due.

**Overrides:** `/repeat-knowledge N` reviews up to N **themes**, ignoring the question cap;
`/repeat-knowledge all` clears the whole backlog in one run. Both are the user's call — the
skill never widens the budget on its own.

**Carry-over:** themes beyond the budget are NOT touched — they keep their `next_due` (so they
stay due/overdue) and lead the follow-up run. The run's closing report names the follow-up date.

Because SM-2 spreads `next_due` out as themes mature, steady-state load stays inside one run even
at 30–50 themes; the cap only bites during ramp-up or after a gap in sessions.

## Ledger
> **Fresh start 2026-07-07** — ledger reset (pilot rows archived in git history). Themes re-enter
> as they are re-taught with a KB note (EF 2.50, reps 0). Q1/Q2 KB notes already exist from the
> pilot; those themes re-enter after their fresh teach/review, not automatically.

<!-- viz:ledger -->
| Theme | Q | KB note | EF | reps | interval (d) | Last reviewed | Next due | History |
|---|:--:|---|:--:|:--:|:--:|:--:|:--:|---|
| Transactional outbox & dual-write | Q6 | [transactional-outbox.md](knowledge-base/phase-1-distributed-tx/transactional-outbox.md) | 2.50 | 1 | 7 | 2026-08-17 | 2026-08-24 | entered 2026-08-07 · 2026-08-17 pass 82 (q4) |
| Idempotent consumption | Q7 | [idempotent-consumption.md](knowledge-base/phase-1-distributed-tx/idempotent-consumption.md) | 2.04 | 1 | 7 | 2026-08-26 | 2026-09-02 | entered 2026-08-10 · 2026-08-17 **lapse** 68 (q2) · 2026-08-26 pass 77 (q3) |
| Delivery semantics & effectively-once | Q8 | [delivery-semantics.md](knowledge-base/phase-1-distributed-tx/delivery-semantics.md) | 2.36 | 1 | 7 | 2026-08-26 | 2026-09-02 | entered 2026-08-13 · 2026-08-26 pass 70 (q3) |
| Isolation levels & MVCC | Q2 | [isolation-levels-and-mvcc.md](knowledge-base/phase-1-distributed-tx/isolation-levels-and-mvcc.md) | 2.50 | 0 | 7 | — (re-taught 2026-08-17) | **parked** → 2026-09-09 | entered 2026-08-17 (fresh re-teach; pilot note carried) · **2026-08-26 parked**: user reports the theme is not yet learned and stage 4 (`p1-02`) is untouched — a retention quiz would measure the teach, not consolidated recall. First pass runs after the stage-5 review; the date is a placeholder, not a schedule |

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
| 2026-08-17 | Q6 Transactional outbox | 82 → q4 (pass) | EF 2.50→2.50, reps 0→1, interval 7 d → due 2026-08-24. Misses: retries attributed to publisher confirms (confirm = broker→publisher ack; retry is app logic), `published_at IS NULL` re-scan mitigation not named explicitly |
| 2026-08-17 | Q7 Idempotent consumption | 68 → q2 (**lapse**; raised 65→68 on challenge — PSP-side dedup was named cold, though framed as the PSP's property rather than a producer-sent key, with query-first preferred over safe resend) | EF 2.50→2.18, reps→0, interval 2 d → due 2026-08-19. Misses: redelivery re-framed as retry-policy (recurring — it's protocol requeue of unacked deliveries on connection death); crash-window ordering (record-first ⇒ silent loss = worse) not reproduced when asked directly; TTL lower bound answered as processing time instead of max redelivery window (broker retention + DLQ replay + manual reprocessing). Held: atomic claim via PK/ON CONFLICT, Redis-as-second-resource trap, guarded transition, record-intent-before-PSP-call |
| 2026-08-26 | Q7 Idempotent consumption | 77 → q3 (**pass**, off the lapse) | EF 2.18→2.04, reps 0→1, interval 2→7 d → due 2026-09-02. Ledger was **7 d past** the relearn date (9-day session gap). Recovered vs 08-17: crash-window asymmetry reproduced cold on a code-review framing (record-first ⇒ silent permanent loss vs effect-first ⇒ duplicate); TTL floor re-derived as "max time a duplicate can still arrive"; the PSP **producer-sent** `Idempotency-Key` named as the first-call prerequisite — the design inversion flagged on 08-17, now closed. Best answer of the set: the claim signal plus why the exception-based variant is worse (constraint violation at flush leaves the Hibernate session undefined and the tx rollback-only, for what is an *expected* business outcome). Still open: **(a) requeue-vs-retry, 3rd session running** — tied redelivery to "no success / no exception" when the premise was a dead connection, where the ack can never arrive however the method ends; broker requeue of unacked deliveries on channel close is AMQP-mandated, not a configurable retry (`defaultRequeueRejected` governs only the reject/nack path); (b) TTL contributors named were the small ones (outbox retention, queue latency) — DLQ replay and manual reprocessing are what set the number; (c) minor: `UPDATE … WHERE owner IS NULL` offered as a claim, but it needs the row to pre-exist (guarded transition, not a fresh-key claim) |
| 2026-08-26 | Q8 Delivery semantics & effectively-once | 70 → q3 (pass, **at the floor**) | EF 2.50→2.36, reps 0→1, interval 7 d → due 2026-09-02. First recall test since the 08-13 teach. Outstanding on Kafka EOS: PID+partition+sequence dedups transport retries only, so a restarted relay's fresh `send()` is a legitimately new record; named `InitProducerId` PID recovery + epoch fencing under `transactional.id` unprompted (beyond the KB note). NONE-vs-AUTO mapped correctly incl. the broker auto-acking on dispatch under NONE. Misses: **(a) the perfect-network window missed** — assumed away process failure too, making the premise vacuous, then reintroduced network drops; the answer is process death between effect and ack (the window relocates inside one machine) and exactly-once *effect* survives because the effect lives in storage you control; (b) **publisher-confirm direction still unanswered** — the residual slip from 08-16, carry to the next pass; (c) producer-side gap named as the confirm-lost variant rather than the publish that never started; (d) under AUTO the *container* acks after the listener returns — the broker does not "wait for processing" |
| 2026-08-26 | *(carried, not reviewed)* Q6, Q2 | — | Session closed early at the user's request after ~1 h on two themes. Q6 stays due (next_due 2026-08-24) and leads the next run. Q2 **parked** — see its ledger row |
