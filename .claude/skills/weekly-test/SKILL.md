---
name: weekly-test
description: Run a spaced-repetition recall quiz for the senior-path learning lab. Use when the user types /weekly-test, says "review" / "weekly test" / "quiz me on what I've learned", or when the session-start ritual finds themes due in docs/spaced-review.md. Quizzes due themes cold, grades, and updates the retention ledger.
---

# /weekly-test — spaced-repetition review (SM-2)

Run a lightweight, cold recall quiz over the themes that are **due** in
`docs/spaced-review.md`, then update the ledger using the **SM-2** algorithm. This is the
retention layer, separate from the 2–4 week deep re-assessment in `docs/knowledge-map.md`.
The full SM-2 spec (EF formula, grade→q map, interval rules) lives in `docs/spaced-review.md`
— that file is the source of truth; this skill is the procedure.

## Procedure

1. **Select due themes.** Read `docs/spaced-review.md`. Today's date comes from the session
   context. Pick every row where `next_due ≤ today` (overdue included). If none are due, say so
   and offer to review the soonest-upcoming theme anyway (optional early review) — do not force it.

2. **Quiz cold, one theme at a time.** For each due theme, ask **3–5** short recall questions
   drawn from its knowledge-base note (the `KB note` link in the ledger row). Rules:
   - No notes, no scrolling, answer from memory; "not sure" is allowed.
   - Ask all questions for a theme first; wait for the user's answers before grading.
   - Target the mechanism and tradeoffs, not trivia — this mirrors the diagnostic style.

3. **Grade** each theme 0–100 with the rubric
   (0 = none · 40 = instinct, no mechanism · 70 = mechanism + one tradeoff · 90+ = cold w/ tradeoffs),
   then map to SM-2 quality `q` (0–5) per the table in `docs/spaced-review.md`. Point out errors
   directly with the correct mechanism (mentor rule 1).

4. **Apply SM-2** per theme (formula in `docs/spaced-review.md`):
   - Update `EF' = EF + (0.1 − (5−q)(0.08 + (5−q)·0.02))`, floor 1.30.
   - **Pass (`q ≥ 3`)**: `reps==0 → interval=7`; `reps==1 → round(7·EF')`; `reps≥2 → round(interval·EF')`; then `reps += 1`.
   - **Fail (`q < 3`)**: `reps=0`, `interval=2`, and add/raise the theme in `progress-log.md` "Open weak spots".
   - `next_due = today + interval`.

5. **Record.** Update the ledger row(s) (EF, reps, interval, Last reviewed, Next due, History) and
   append a row to the `## Review log` table (date, themes, grade→q, EF/interval change). If a
   deep-assessment cell moved materially, reflect the score in `knowledge-map.md` too.

6. **Commit** the doc changes: `chore(review): <date> spaced review — <themes> (<pass/lapse>)`
   with the project's AI-attribution trailer (`Co-Authored-By: Claude Opus 4.8`, via HEREDOC).

## Notes
- A theme enters the ledger (EF 2.50, reps 0) only once it has been taught **and** has a KB note.
- Keep questions fresh across reviews (don't reuse the same 3 every time) — recall must be genuine.
- Delivery is **session-start surfacing** (no scheduled agent) per the ledger's Automation
  section: at session start, if anything is due, announce it and offer to run `/weekly-test`.
