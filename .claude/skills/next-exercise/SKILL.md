---
name: next-exercise
description: Design the next hands-on exercise for the senior-path learning lab. Use when the user types /next-exercise, says "create the next exercise" / "give me an exercise" (optionally naming a phase or topic), or wants a new coding challenge. Produces spec + skeleton + FAILING tests (never a solution), registers and verifies it RED, and updates the indexes.
---

# /next-exercise — design a new challenge

Create a new exercise as a **challenge the user solves themselves**: spec + skeleton with
`// TODO`s + tests that FAIL by design. Never implement the solution (see the HARD RULE in
`CLAUDE.md` → EXERCISE PROTOCOL; solutions only go to gitignored `/solutions/` on the explicit
phrase "show me the solution", hints first).

Optional argument: a phase and/or topic (e.g. `/next-exercise Q2 isolation`). Default to the
current phase and the top open gap.

## Procedure

1. **Pick the topic.** Read `docs/roadmap.md` (current phase + topics), `docs/progress-log.md`
   (open weak spots), and `docs/knowledge-map.md` (scores). Choose the highest-leverage gap in the
   current phase unless the user named one. State the chosen Q(s) and why before scaffolding.
   **Arcs & capstones:** tightly coupled themes (e.g. Q6+Q7+Q8) or the phase capstone share ONE
   module with staged tasks (`docs/learning-flow.md` → Multi-theme arcs) — prefer extending the
   arc module over scaffolding a new isolated one.

2. **Scaffold `exercises/p{phase}-{NN}-{slug}/`** by copying `exercises/_TEMPLATE/`. The `{NN}`
   is the next free number within the phase. Include:
   - **`SPEC.md`** — objective; roadmap phase; targeted diagnostic Q(s); scenario; numbered tasks
     (TODOs); acceptance criteria; constraints; **2 stretch goals**; "How to run"; an *Analysis*
     section the user fills in.
   - **Minimal module** — `build.gradle.kts` applying `senior-path.spring-conventions` (or
     `senior-path.kotlin-conventions` if no Spring needed); skeleton `src/main` with `// TODO`
     markers and, where the lesson needs it, a deliberately-seeded bug.
   - **FAILING tests** in `src/test` — Testcontainers (`postgres`/`rabbitmq`) where the topic is
     DB/broker-bound. The test must assert the *correct* end-state so it is RED until implemented.

3. **Register** the module in `settings.gradle.kts` via the `exercise("p{phase}-{NN}-{slug}")` helper.

4. **Verify the red start.** Run `./gradlew :<slug>:test`. Confirm:
   - it **COMPILES** (a compile error is NOT an acceptable "red" — fix it), and
   - the new test **FAILS on its assertion**, by design. Read the test XML
     (`failures>=1, errors=0`) to prove it's an assertion failure, not an infra error.
     (Docker must be running for Testcontainers.)

5. **Index it.** Append a row to the `## Exercise index` table in `CLAUDE.md` (status **RED** —
   the index is the single owner of exercise status) and log the event in `docs/progress-log.md`
   Completed-tasks.

6. **Hand off.** Print the exact test command and what "done" looks like (the observable end-state
   the test checks). Commit: `feat(p{phase}-{NN}): scaffold <slug> (Q<n>) — RED by design` with the
   AI-attribution trailer (`Co-Authored-By:` naming the current model, via HEREDOC).

## Guardrails
- **No auto-solve.** Ship the bug/TODOs and the failing test — not the fix.
- **RED for the right reason.** Verify via the test report that it's an assertion failure.
- **One owner per fact.** Status lives only in the `CLAUDE.md` index; scores only in
  `knowledge-map.md`; the progress log records events.
- On GREEN later, the mentor updates status RED → GREEN → REVIEWED — **REVIEWED requires the
  SPEC Analysis filled (ANALYSIS GATE in `CLAUDE.md`)** — and, once the theme has a KB note,
  ensures it's in `docs/spaced-review.md` (see `/repeat-knowledge`).
