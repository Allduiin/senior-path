# p{PHASE}-{NN} — {TITLE}

| | |
|---|---|
| **Phase** | {N} — {phase theme from docs/roadmap.md} |
| **Targets diagnostic** | **Q{N}** (+ any others) |
| **Start state** | RED — tests fail by design |
| **Done state** | GREEN — tests pass once implemented correctly |

## Objective
{One paragraph: the precise capability this exercise builds, tied to a roadmap topic.}

## Scenario
{Concrete, runnable situation. Describe the seeded bug or the missing implementation and the
observable difference between the buggy/empty state and the correct state.}

## Tasks
1. {Diagnose / explain the mechanism in the Analysis section.}
2. {Implement the change. Be explicit about what NOT to touch.}
3. {Document tradeoffs / alternatives.}

## Acceptance criteria
- `./gradlew :p{PHASE}-{NN}-{slug}:test` is GREEN.
- {Observable end-state assertions.}
- {Correctness must hold for the right reason, not by weakening the test.}

## Constraints
- Do not edit the tests to make them pass.
- {Domain/tech constraints. Testcontainers for any DB/broker — no manual setup.}

## Stretch goals
1. {Deeper variant that pins a subtler part of the mechanism.}
2. {A second, harder extension.}

## How to run
```
./gradlew :p{PHASE}-{NN}-{slug}:test
```

---

## Analysis (you fill this in)
> _TODO._
