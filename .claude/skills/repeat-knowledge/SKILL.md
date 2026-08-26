---
name: repeat-knowledge
description: Run a spaced-repetition recall quiz for the senior-path learning lab. Use when the user types /repeat-knowledge, says "review" / "repeat knowledge" / "quiz me on what I've learned", or when the session-start ritual finds themes due in docs/spaced-review.md. Quizzes due themes cold, grades, and updates the retention ledger.
---

# /repeat-knowledge — spaced-repetition review (SM-2)

Run a lightweight, cold recall quiz over the themes that are **due** in
`docs/spaced-review.md`, then update the ledger using the **SM-2** algorithm. This is the
retention layer, separate from the 2–4 week deep re-assessment in `docs/knowledge-map.md`.
The full SM-2 spec (EF formula, grade→q map, interval rules) lives in `docs/spaced-review.md`
— that file is the source of truth; this skill is the procedure.

## Procedure

1. **Select due themes and pack the run.** Read `docs/spaced-review.md`. Today's date comes from
   the session context. Collect every row where `next_due ≤ today` (overdue included).
   - **Budget: counted in questions, not themes** — the cap, the packing rule and the overrides
     live in `docs/spaced-review.md` → *Session budget*, which you have just read. Do not restate
     or reinterpret the number here; apply what that section says.
   - **Decide each theme's question count first (3–5)**, weighted by fragility: 5 for a lapsed or
     low-EF theme, 3 for one that passed comfortably last time. *Then* pack.
   - **Whole themes only.** Add themes entire, in priority order, while the running total stays
     within the cap; when the next theme would exceed it, stop. **Never split a theme's questions
     across two runs** — a partial theme cannot be graded, and a grade is what SM-2 consumes.
   - **Carry-over:** untouched themes keep their `next_due` and lead the **follow-up run the next
     day**. Announce the split up front ("2 themes today, 2 tomorrow") so the user knows the plan
     before answering, and name the follow-up date again in the closing report.
   - If nothing is due, say so and offer an optional early review of the soonest-upcoming theme —
     do not force it.

2. **Quiz cold, one theme at a time.** For each theme in the run, ask its 3–5 questions from the
   knowledge-base note (the `KB note` link in the ledger row). Rules:
   - **Each question must be answerable in 1–2 sentences.** Size the question to that: one
     mechanism, one comparison, or one named term per question. If a question needs a paragraph,
     it is really two questions — split it or narrow it.
   - **No multi-part questions.** "Name X, and explain Y, and say why Z" is three questions wearing
     one number, and it silently blows the budget. One ask per number.
   - No notes, no scrolling, answer from memory; "not sure" is allowed and more useful than a guess.
   - Ask all of a theme's questions in one batch; wait for the user's answers before grading.
   - Target the mechanism and tradeoffs, not trivia — this mirrors the diagnostic style.
   - **Follow-ups are free.** A clarifying re-ask on a wrong or thin answer is teaching, not quiz;
     it does not count against the cap. Grade the cold answer, not the recovered one.

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
   append a row to the `## Review log` table (date, themes, grade→q, EF/interval change).
   - If the run was **split** by the budget, append a `*(carried, not reviewed)*` row naming the
     deferred themes and the follow-up date, so the backlog is visible in the log itself.
   - If a deep-assessment cell moved materially, reflect the score in `knowledge-map.md` too —
     but the `Latest` column is **`/assess`-owned**: when a retention grade contradicts it, log
     the grade in the `## Re-test log` as evidence and leave the reconciliation to `/assess`
     rather than blending two different instruments into one number.

6. **Commit** the doc changes: `chore(review): <date> spaced review — <themes> (<pass/lapse>)`
   with the project's AI-attribution trailer (`Co-Authored-By:` naming the current model, via
   HEREDOC).

## Notes
- A theme enters the ledger (EF 2.50, reps 0) only once it has been taught **and** has a KB note.
  If the user says a theme isn't actually learned yet, or its exercise stage is untouched, **park
  the row** rather than quizzing it — a quiz would measure the teach, not retention.
- Keep questions fresh across reviews (don't reuse the same 3 every time) — recall must be genuine.
- **Brevity is a hard rule, not a style preference.** Short questions with 1–2 sentence answers
  test recall of the mechanism; long ones test stamina and willingness to write. The user has
  asked for the short form explicitly (2026-08-26) — a run that produces essay-length answers is
  a run that will get abandoned halfway, which costs a grade and leaves the ledger stale.
- Delivery is **session-start surfacing** per the ledger's Automation section: at session start,
  if anything is due, announce it and offer to run `/repeat-knowledge`. (**Telegram notifications
  are planned** as the proactive channel — see `docs/spaced-review.md` → Automation.)
