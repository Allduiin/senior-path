# senior-path

A single, long-lived **learning lab** for becoming a Senior Java/Kotlin backend engineer.
Everything lives here: the plan, my live progress, the mentoring rules, and hands-on
exercises I solve myself.

- **Plan:** [`docs/roadmap.md`](docs/roadmap.md) — five phases, calibrated from a 12-question diagnostic.
- **Lifecycle:** [`docs/learning-flow.md`](docs/learning-flow.md) — the 8-stage per-theme flow, its gates, multi-theme arcs & phase capstones.
- **Live status:** [`docs/progress-log.md`](docs/progress-log.md) — per-pillar levels, phase tracker, session history. Maintained by Claude Code.
- **Scores:** [`docs/knowledge-map.md`](docs/knowledge-map.md) — per-question scores & history (single owner of the numbers).
- **Retention:** [`docs/spaced-review.md`](docs/spaced-review.md) — SM-2 spaced-recall ledger.
- **Theory:** [`docs/knowledge-base/`](docs/knowledge-base/README.md) — durable mechanism-first notes per theme.
- **Reading:** [`docs/reading-log.md`](docs/reading-log.md) — the continuous reading/writing track.
- **Operating rules:** [`CLAUDE.md`](CLAUDE.md) — mentor rules, exercise protocol, the no-auto-solve hard rule, exercise index.

## The session model
A study session is: **start Claude Code in this repo.** Then:
1. Claude reads `docs/roadmap.md` + `docs/progress-log.md`, states the current phase and open
   gaps, and proposes today's focus.
2. It teaches and quizzes me (diagnostic Q1–Q12 + new questions), or designs an exercise.
3. **I** solve the exercises (Claude never auto-solves them).
4. Claude checks the test results and discusses.
5. At the end, Claude updates `docs/progress-log.md` directly and commits it.

## Stack
Kotlin 2.4 · Java 21 · Spring Boot 3.5 · Gradle 8.x (Kotlin DSL) · JUnit 5 · AssertJ · MockK ·
Awaitility · Testcontainers (Postgres, RabbitMQ).

**Prerequisites:** JDK 21 and a running **Docker** daemon (Testcontainers spins up Postgres /
RabbitMQ automatically — no manual database or broker setup).

## Common commands
```bash
# Build everything (compiles all exercises; runs their tests)
./gradlew build

# Run a single exercise's tests
./gradlew :p1-01-tx-self-invocation:test

# Lab wiring overview
./gradlew labInfo
```

## Requesting work from the mentor
- **"create the next exercise"** (optionally naming a phase/topic) → Claude designs a new
  challenge: spec + skeleton + failing tests, registered and verified RED. See the protocol in
  [`CLAUDE.md`](CLAUDE.md).
- **"show me the solution"** → only then does Claude put a worked solution under `/solutions/`
  (gitignored), hints first.

## Layout
```
senior-path/
├── CLAUDE.md                 # operating rules + exercise index (status owner)
├── docs/
│   ├── roadmap.md            # the plan (5 phases + continuous track)
│   ├── learning-flow.md      # 8-stage per-theme lifecycle + gates + arcs/capstones
│   ├── progress-log.md       # live status (levels, phases, session history)
│   ├── knowledge-map.md      # per-question scores (single owner of the numbers)
│   ├── spaced-review.md      # SM-2 retention ledger
│   ├── reading-log.md        # continuous reading/writing track
│   └── knowledge-base/       # durable theory notes, indexed by README.md
├── .claude/skills/           # /learn-theme, /next-exercise, /repeat-knowledge, /assess
├── build-logic/              # Gradle convention plugins (shared build setup)
├── gradle/libs.versions.toml # version catalog (single source of truth)
├── exercises/
│   ├── _TEMPLATE/            # copy source for new exercises
│   └── p1-NN-*/              # one module per exercise (SPEC + skeleton + failing tests)
└── solutions/                # gitignored; populated only on explicit request
```
