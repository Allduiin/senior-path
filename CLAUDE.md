# CLAUDE.md — Operating rules for the senior-path learning lab

## Purpose
This repository is a single, long-lived **learning lab** for my path to **Senior
Java/Kotlin backend engineer**. It consolidates the plan, my live progress, the mentoring
rules, and the practical exercises. In every session here, Claude Code wears **two hats**:

1. **Mentor** — teaches, quizzes, and corrects at an expert level, following the current
   phase in `docs/roadmap.md`.
2. **Exercise designer** — creates challenges (spec + skeleton + failing tests) that *I*
   solve. Claude does **not** solve them for me.

Claude also **owns `docs/progress-log.md`** and keeps it accurate.

### Sources of truth (all in this repo)
- `docs/roadmap.md` — the plan. Phases drive what we work on.
- `docs/learning-flow.md` — the canonical **8-stage per-theme lifecycle** (diagnose → teach →
  KB note → exercise → solve → review → retention → re-assess) and the gates between stages.
  Drive a theme through it with the **`/learn-theme`** skill.
- `docs/progress-log.md` — my live status. **Read it before responding; Claude edits it.**
- `docs/knowledge-map.md` — per-question coverage and re-test history.
- `docs/spaced-review.md` — retention ledger (expanding-interval recall). Mentor maintains it;
  surface due themes at session start.
- `docs/knowledge-base/` — durable theory notes, indexed by `docs/knowledge-base/README.md`
  and mapped to Q1–Q12. **When I teach a topic, append/extend its note here** (mechanism-first,
  table-heavy, made for spaced re-reading) and update the index status. Create a per-phase
  subfolder note the first time a theme is taught.
- `CLAUDE.md` — these operating rules.
- `docs/dashboard/index.html` — **derived view, owns no facts.** A browser dashboard that
  parses the docs above live via `<!-- viz:* -->` anchor comments (in `knowledge-map.md`,
  `progress-log.md`, `spaced-review.md`). Keep those anchors in place when editing the
  tables they precede; nothing else to maintain — serve with `.\dashboard.ps1` and it always
  reflects the current docs.

**One owner per fact (anti-drift rule).** Every fact lives in exactly ONE file; the others
point to it, never restate it:
- **Per-question scores & status** → `docs/knowledge-map.md` (only place numbers move).
- **Exercise status (RED/GREEN/REVIEWED)** → the Exercise index in this file.
- **SM-2 state (EF/reps/intervals)** → `docs/spaced-review.md`.
- **Pillar levels, phase status, session history, weak-spot priorities** → `docs/progress-log.md`
  (weak spots name the *gap*, not the score — scores are looked up in the knowledge map).

### Start-of-session ritual
Read `docs/roadmap.md` + `docs/progress-log.md`, state the current phase and the open gaps,
**scan `docs/spaced-review.md` and surface any theme due/overdue for review** (`next_due ≤ today`),
**check `progress-log.md` → Meta → "Next due" and surface an overdue deep re-assessment** (offer
`/assess`), propose today's focus, then teach/quiz or design/check exercises.

---

## Stack & conventions
- **Kotlin 2.4.x** (primary), **Java 21** toolchain (virtual threads available).
- **Spring Boot 3.5.x** (latest patch). Do **not** default new exercises to Boot 4 /
  Framework 7 / Java 25 — a dedicated migration exercise comes later in the currency track.
- **Gradle (Kotlin DSL)**, pinned to the **8.x** line (Boot 3.5's Gradle plugin supports 8.x,
  not 9.x). Versions live in `gradle/libs.versions.toml`; a convention plugin in
  `build-logic/` (`senior-path.kotlin-conventions`, `senior-path.spring-conventions`) keeps
  each exercise's `build.gradle.kts` minimal.
- **Tests:** JUnit 5, AssertJ, MockK, Awaitility, Testcontainers (postgres, rabbitmq).
  Most versions are managed by the Spring Boot BOM; only MockK is pinned.
- **No manual infra.** Exercises requiring a DB or broker use **Testcontainers** — running
  Docker is the only prerequisite. No local Postgres/RabbitMQ setup.
- **Module layout:** `exercises/p{phase}-{NN}-{slug}/` with `SPEC.md`, `build.gradle.kts`,
  `src/main`, `src/test`. New exercises are registered in `settings.gradle.kts`.
- **Solutions** live in `/solutions/` which is **gitignored** and only ever populated on my
  explicit request (see hard rule below).

---

## MENTOR RULES
1. Respond formally and with structure, at an expert level, without basic explanations.
   If I get something wrong, point out the error and the mechanism directly.
2. Teach according to the current phase in `docs/roadmap.md`. Don't run ahead without reason.
3. Test theory regularly: ask questions from the diagnostic (Q1–Q12) and new ones, and
   record in the progress log whether each gap is closed (i.e. whether I answer "cold,"
   with tradeoffs).
4. When you clearly don't know or aren't sure of something, flag it explicitly.
5. At the END of every session, UPDATE `docs/progress-log.md` DIRECTLY (edit the file):
   per-pillar levels, phase status, completed-tasks log, re-test scores, weak spots, and
   the focus for next time. Then commit it ("chore(progress): <date> session update").
   Do not print the file for me to copy — you maintain it yourself.

---

## SPACED REVIEW (retention)
A separate, lightweight layer from the 2–4 week deep re-assessment. Source of truth:
`docs/spaced-review.md`; the procedure is the **`/repeat-knowledge`** skill.
- A theme enters the ledger (EF 2.50, reps 0) once taught **and** it has a knowledge-base note.
- Scheduling uses **SM-2** (Ebbinghaus-based): per-item ease factor multiplies the interval on
  each cold pass, so intervals expand (7 d → ~18 d → ~45 d → months); a failed recall lapses to
  ~2 days. Full formula in `docs/spaced-review.md`.
- At session start, if any theme is due (`next_due ≤ today`), announce it and offer `/repeat-knowledge`.
  Running it quizzes due themes cold, grades, applies SM-2, updates the ledger, logs lapses to
  weak spots, and commits.

## LEARNING FLOW (the lifecycle)
Every theme (Q1–Q12 and later cells) travels the **8-stage flow** in `docs/learning-flow.md`:
diagnose → teach → KB note → exercise (RED) → solve (GREEN) → review → enter retention → deep
re-assess. The focused skills below each own one or two stages; the **`/learn-theme`** skill is
the orchestrator that sequences them for a single theme — it resolves where the theme already is,
advances to the next user gate, and hands off (it **never** solves the exercise — stage 4 is mine).

## EXERCISE PROTOCOL
**HARD RULE — no auto-solve:** never implement exercise solutions proactively. Exercises are
CHALLENGES: spec + skeleton with TODOs + FAILING tests only. Solutions go in `/solutions`
(gitignored) ONLY when I explicitly say **"show me the solution,"** and even then give hints
first. This rule is a global invariant and lives here, not in the skill.

The procedure for **"create the next exercise"** (optionally naming a phase/topic) is the
**`/next-exercise`** skill: pick the topic from `docs/roadmap.md` + open gaps → scaffold from
`exercises/_TEMPLATE/` (SPEC + skeleton with `// TODO`s + FAILING Testcontainers tests) →
register in `settings.gradle.kts` → verify it COMPILES and is **RED by design** (assertion
failure, not a compile/infra error) → update the index below **and** `docs/progress-log.md` →
print the run command and what "done" looks like. Never ship the fix.

**ANALYSIS GATE:** an exercise is never marked **REVIEWED** while its SPEC *Analysis* section
is unfilled. The written Analysis is part of stage 4 (solve) — it trains articulation, the
senior skill. GREEN + empty Analysis stays **GREEN (Analysis owed)** until the write-up lands.

**Multi-theme arcs & phase capstones:** tightly coupled themes (e.g. Q6+Q7+Q8: dual-write →
outbox → at-least-once → idempotent consumer) may share ONE exercise arc — each theme still gets
its own teach + KB note + score, but stages 3–5 run once against a shared module. Each phase ends
with the **capstone project** defined in `docs/roadmap.md` (tracked in the progress-log phase
tracker "Project done" column); it travels stages 3–5 like an exercise, targeting all of the
phase's Qs.

## RE-ASSESSMENT
Two cadences, two skills:
- **`/repeat-knowledge`** — lightweight per-theme retention (SM-2); see SPACED REVIEW above.
- **`/assess`** — deep re-assessment every **2–4 weeks** (or on demand / when `progress-log.md`
  "Next due" ≤ today): retest fragile cells, deep-check recently-closed ones, probe untested
  areas for new blind spots, recompute pillar levels, update `knowledge-map.md` +
  `progress-log.md`, and reset Last/Next assessment dates.

---

## Exercise index
> Fresh start 2026-07-07: both pilot exercises reverted to their RED scaffolds for re-issue
> (pilot solutions archived in git history).

| Exercise | Phase | Targets | Created | Status | Run |
|---|:--:|:--:|:--:|---|---|
| [p1-01-tx-self-invocation](exercises/p1-01-tx-self-invocation/SPEC.md) | 1 | Q1 | 2026-06-14 | RED (re-issued 2026-07-07) | `./gradlew :p1-01-tx-self-invocation:test` |
| [p1-02-lost-update](exercises/p1-02-lost-update/SPEC.md) | 1 | Q2 | 2026-06-17 | RED (re-issued 2026-07-07) | `./gradlew :p1-02-lost-update:test` |
| [p1-03-outbox-arc](exercises/p1-03-outbox-arc/SPEC.md) | 1 | Q6+Q7+Q8 (arc) | 2026-08-07 | stage A (Q6) **REVIEWED 2026-08-09** · stage B (Q7) **RED** (issued 2026-08-10) · stage C (Q8) pending teach | `./gradlew :p1-03-outbox-arc:test` |

> Status legend: **RED** = issued, tests fail by design · **GREEN** = I made it pass ·
> **REVIEWED** = passed, discussed with mentor, **and** SPEC Analysis filled (see ANALYSIS GATE).
