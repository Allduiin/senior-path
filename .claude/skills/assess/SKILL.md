---
name: assess
description: Run the periodic deep re-assessment for the senior-path learning lab (every 2-4 weeks, or on demand). Use when the user types /assess, says "run assessment" / "re-assess me" / "recalibrate my levels", or when progress-log.md "Next due" <= today. Retests fragile cells, deep-checks recently-closed ones, probes untested areas for new blind spots, recomputes levels, and updates the docs.
---

# /assess — periodic deep re-assessment  ·  STATUS: v1 (calibrated on the 2026-07-28 baseline run)

The heavy, broad counterpart to `/repeat-knowledge`. Spaced review keeps single themes *fresh*;
this **recalibrates the whole picture**: confirms gaps closed, finds NEW blind spots, and moves
pillar levels. Cadence: every **2–4 weeks** (`progress-log.md` → Meta → Next due), or on demand.

## Procedure

1. **Scope the round.** Read `docs/knowledge-map.md` + `docs/progress-log.md`.
   **Baseline mode:** if the knowledge map has no operative baseline (e.g. after a fresh start),
   run the full Q1–Q12 set cold, record it as the new baseline column, and recalibrate all pillar
   levels from it — skip the bucket logic below. Otherwise build the question set from four buckets:
   - **Retest** open / low cells (score < 70).
   - **Confirm** fragile cells (50–79) and **deep-check recently-closed** ones (≥80) — verify
     retention beyond the spaced-review pass, with *new* questions.
   - **Probe** 1–2 untested or adjacent areas to surface blind spots not yet on the map.
   - Bias toward the **current phase**, but a re-assessment may range across all pillars.

2. **Quiz cold, diagnostic-style.** Several questions per cell, expert level, mechanism +
   tradeoffs. No notes. Ask per-cell, wait for answers, correct errors directly with the mechanism.

3. **Grade** each cell 0–100 (rubric: 0 none · 40 instinct-no-mechanism · 70 mechanism+1 tradeoff ·
   90+ cold w/ tradeoffs). Note any new blind spot discovered as its own cell.

4. **Recalibrate:**
   - Update `knowledge-map.md`: each cell's `Latest` score + `Status`; append a re-test log row;
     add rows for newly-discovered blind spots.
   - Recompute **pillar levels** in `progress-log.md` from that pillar's cell scores:
     pillar mean < 40 → L2 · 40–59 → L3 · 60–79 → L4 · 80–89 → L5 · 90+ → L6, raising
     confidence from "low" once ≥2 cells in the pillar are ≥80.
   - **Single-cell cap** (added after the 2026-07-28 run): a pillar with only one scored cell
     is capped at **L3 (low confidence)** regardless of the mean — one good answer isn't a
     pillar reading. Pillars with no scored cells carry their prior, marked "untested".
   - Sub-gaps found inside an existing cell go to the weak-spots list (and the re-test log
     row), **not** to new Q-cells; a new cell is only for a genuinely new *topic*.
   - Reorder "Open weak spots" (gaps only — scores stay in the knowledge map), set phase status,
     and set **Last assessment = today**, **Next due = today + 2–4 weeks**.

5. **Sync spaced review.** Any theme that is now closed **and** has a KB note must be present in
   `docs/spaced-review.md` (enter at EF 2.50, reps 0 if new). A cell that regressed should lapse
   its ledger row.

6. **Commit** all doc changes: `chore(assess): <date> re-assessment — <summary>` with the
   AI-attribution trailer (`Co-Authored-By:` naming the current model, via HEREDOC).

## Boundaries
- This does **not** create exercises (`/next-exercise`) or run the light retention pass
  (`/repeat-knowledge`) — though it may recommend both as follow-ups.
- Findings are only as honest as the cold answers — never grade up from partial recall.
