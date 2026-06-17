# Learning Flow — how one theme travels end-to-end

> The canonical per-theme lifecycle for the senior-path lab. Every diagnostic question
> (**Q1–Q12**) and every later-discovered theme travels these stages. The mechanics live in
> `CLAUDE.md` and the individual doc headers; **this file is the single map that ties them
> together**. Drive a theme through it with the **`/learn-theme`** skill.
>
> Related: `roadmap.md` (the plan) · `progress-log.md` (live status) · `knowledge-map.md`
> (scores) · `knowledge-base/` (the theory) · `spaced-review.md` (retention).

## The two hats
Claude wears both, never blurring them:
- **Mentor** — teaches, quizzes, corrects at expert level; owns all the docs below.
- **Exercise designer** — issues challenges (spec + skeleton + failing tests). **Never solves
  them** (the HARD RULE in `CLAUDE.md` → EXERCISE PROTOCOL).

## The 8 stages

| # | Stage | What happens | Recorded in | Driven by |
|:--:|---|---|---|---|
| 0 | **Diagnose** | Cold baseline score 0–100 for the theme | `knowledge-map.md`, `progress-log.md` (diagnostic table) | mentor / `/assess` |
| 1 | **Teach** | Mentor teaches the mechanism + tradeoffs for the current phase, expert level, no basics | conversation | mentor |
| 2 | **Capture theory** | Append/extend the theme's **knowledge-base note** (mechanism-first, table-heavy) and flip its status in `knowledge-base/README.md` | `knowledge-base/<phase>/<slug>.md` + KB index | mentor |
| 3 | **Exercise (RED)** | Scaffold spec + skeleton + a test that FAILS by design; verify it compiles and is red *on the assertion* | exercise module, `CLAUDE.md` index, `progress-log.md` tracker | `/next-exercise` |
| 4 | **Solve (GREEN)** | *You* implement the fix and fill the SPEC **Analysis**; test goes green | exercise module | **the user** (mentor stays hands-off) |
| 5 | **Review** | Mentor reviews the diff + Analysis, marks the exercise **REVIEWED**, scores the Q | `CLAUDE.md` index → REVIEWED, `progress-log.md` | mentor |
| 6 | **Enter retention** | Once taught **and** it has a KB note, the theme enters the spaced-review ledger (EF 2.50, reps 0, due +7 d) | `spaced-review.md` | mentor |
| 7 | **Spaced recall** | Cold recall on the SM-2 schedule; intervals expand (7 d → ~18 d → ~45 d …), a miss lapses to ~2 d | `spaced-review.md`, lapses → `progress-log.md` weak spots | `/repeat-knowledge` |
| 8 | **Deep re-assess** | Every 2–4 weeks: retest fragile cells, probe for new blind spots, recompute pillar levels | `knowledge-map.md`, `progress-log.md` | `/assess` |

```
0 diagnose → 1 teach → 2 KB note → 3 exercise RED → 4 you GREEN → 5 review
                          └────────────┬───────────────────────────┘
                                        ▼
                          6 enter retention → 7 spaced recall ⟲ (forever)
                                        ▼
                          8 deep re-assess ⟲ (every 2–4 weeks, all themes)
```

## Gates (a stage cannot be skipped)
- **2 → 6:** a theme enters the retention ledger only after it is *both* taught **and** has a KB
  note. No note, no spaced review.
- **3 → 4:** the exercise must be **RED for the right reason** — an assertion failure, never a
  compile/infra error — before the user attempts it.
- **4 stays the user's:** the mentor never implements the fix. Solutions go to gitignored
  `/solutions/` only on the explicit phrase **"show me the solution"**, hints first.
- **5 → score:** the Q score moves only on a *cold* answer with tradeoffs, never up from partial
  recall (same honesty rule as `/assess`).

## Not every theme uses every stage
- A **pure-theory** theme (no good exercise) can skip 3–5: teach → KB note → retention.
- A theme **re-opened** by a lapse (stage 7 miss) or a regression (stage 8) loops back to teach
  (1) — it does not restart at diagnose.
- The current phase in `roadmap.md` decides *which* theme is in flight; don't run ahead without
  reason (mentor rule 2).

## Where each theme is right now
That's not tracked here — it lives in `progress-log.md` (status, weak spots, next focus) and the
`CLAUDE.md` exercise index. This file defines the *shape* of the journey, not the live position.
