---
name: learn-theme
description: Drive one theme through the full learning flow end-to-end for the senior-path lab. Use when the user types /learn-theme, says "learn <topic>" / "take Q2 through the flow" / "start the full flow on <theme>", or wants to work a diagnostic question from diagnose to retention. Orchestrates the 8-stage lifecycle in docs/learning-flow.md, delegating to /next-exercise, /repeat-knowledge, and /assess. Never solves the exercise.
---

# /learn-theme — run the full learning flow for one theme

The orchestrator. Takes one theme (a diagnostic Q, or a named topic) and walks it through the
8-stage lifecycle defined in `docs/learning-flow.md` — diagnose → teach → KB note → exercise
RED → (user solves) GREEN → review → enter retention → (later) deep re-assess. It does not
replace the focused skills; it **sequences** them, calling `/next-exercise`, `/repeat-knowledge`,
and `/assess` at the right gates and doing the teach + KB-note work itself.

`docs/learning-flow.md` is the source of truth for the stages and gates; this skill is the
procedure that drives them. Honor the two hats and the HARD RULE: **never implement the
exercise solution** (stage 4 is always the user's).

Argument: the theme — `/learn-theme Q2`, `/learn-theme isolation levels`, or no arg (default to
the current phase's top open gap in `progress-log.md`).

## Procedure

1. **Locate the theme & its stage.** Read `docs/roadmap.md`, `docs/progress-log.md`,
   `docs/knowledge-base/README.md`, `docs/spaced-review.md`, and the `CLAUDE.md` exercise index.
   Resolve the theme to a Q (or name a new cell). Determine which of the 8 stages it is **already
   past**, and announce: "Theme X is at stage N; here's the plan to advance it." Do not redo
   completed stages.

2. **Run from the current stage forward**, stopping at the first stage that needs the user:
   - **Stage 0 — Diagnose** (only if no baseline exists): ask 2–3 cold questions, grade 0–100,
     record the cell in `knowledge-map.md` + `progress-log.md`.
   - **Stage 1 — Teach:** teach the mechanism + tradeoffs at expert level, anchored to the
     current phase. Correct the diagnostic gaps directly.
   - **Stage 2 — Capture theory:** write/extend the KB note under
     `docs/knowledge-base/<phase>/<slug>.md` (mechanism-first, table-heavy, made for re-reading);
     create the per-phase subfolder note on first use; flip the status in the KB index.
   - **Stage 3 — Exercise (RED):** invoke **`/next-exercise`** for this theme. That skill
     scaffolds, registers, verifies RED-by-design, and updates the indexes. Do not duplicate its
     work here. **Arcs:** if the theme belongs to a multi-theme arc or the phase capstone
     (`docs/learning-flow.md` → Multi-theme arcs), target the shared module instead of a new
     isolated one — each theme still gets its own stages 0–2 and its own score.
   - **Stage 4 — Solve (GREEN): HAND OFF AND STOP.** Print the run command and what "done" looks
     like. The user implements the fix. **Never solve it** (solutions only to gitignored
     `/solutions/` on the explicit phrase "show me the solution", hints first). Resuming
     `/learn-theme` after GREEN continues at stage 5.
   - **Stage 5 — Review:** when the user reports GREEN, review the diff + the SPEC Analysis,
     mark the exercise **REVIEWED** in the `CLAUDE.md` index, score the Q in
     `knowledge-map.md` (single owner of scores). **ANALYSIS GATE: if the SPEC Analysis is
     unfilled, the status stays GREEN (Analysis owed) — never REVIEWED.** The written
     mechanism-and-tradeoffs record is part of solving, not optional.
   - **Stage 6 — Enter retention:** once the theme is taught **and** has a KB note, add its row to
     `docs/spaced-review.md` (EF 2.50, reps 0, due +7 d). This is the 2→6 gate — enforce it.
   - **Stage 7 / 8** are recurring and out of scope for a single run: point the user to
     `/repeat-knowledge` (when due) and `/assess` (every 2–4 weeks) rather than running them here.

3. **Update the docs as each stage completes** — same files the focused skills touch. Keep the
   `CLAUDE.md` exercise index, `progress-log.md`, `knowledge-base/README.md`, and
   `spaced-review.md` in sync. End every run by updating `progress-log.md` "Next session focus"
   to name the theme's current stage and the next action (mentor rule 5).

4. **Commit** at meaningful boundaries with the AI-attribution trailer (HEREDOC,
   `Co-Authored-By:` naming the current model):
   - after teach + KB note: `docs(kb): <theme> note (Q<n>) + flow advance`
   - exercise scaffolding is committed by `/next-exercise` itself
   - after review: `chore(progress): <date> <theme> reviewed (Q<n> → <score>)`

## Guardrails
- **Never auto-solve.** Stage 4 is the wall; hand off and stop.
- **One stage at a time when the user is needed.** Don't teach *and* quiz *and* scaffold in one
  turn past a user gate — advance to the gate, then wait.
- **Don't re-run completed stages.** Detect prior progress from the docs and resume.
- **Respect the gates** in `docs/learning-flow.md` (esp. 2→6: no KB note ⇒ no retention; 3→4:
  RED for the right reason before handing off).
- **Delegate, don't duplicate:** exercise creation is `/next-exercise`, retention is
  `/repeat-knowledge`, deep recalibration is `/assess`. This skill only sequences them.