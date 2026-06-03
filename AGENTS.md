# AGENTS.md

This repository implements an automated API regression test suite for the local Ollama LLM service, built with Rest Assured, JUnit 5, and Maven. It is a coursework submission for a Software Test Engineering class.

## Read first

1. `project_structure.md`
2. `project_plan.md`
3. `README.md` (once it exists; written in Phase 6)

For the assignment requirements, see `docs/assignment.md` 

## Ground rules

- Use `mvn` for all build and test commands. Do not introduce Gradle.
- Java 17 is the baseline. Do not raise or lower the language level.
- Pinned dependency versions (do not bump without approval): Rest Assured `5.5.7`, JUnit Jupiter `5.11.3`, Jackson Databind `2.18.2`, Hamcrest `3.0`, Maven Surefire Plugin `3.5.2`.
- Keep helper infrastructure (specs, config, POJOs, endpoint constants) under `src/main/java/com/example/ollamatests/`. Test classes go under `src/test/java/...`.
- Do not hardcode the base URL, model name, or timeouts. All configuration flows through `TestConfig` (env var → properties file → default).
- Do not commit build artifacts, IDE folders, or local logs. `.gitignore` must cover `target/`, `.idea/`, `*.iml`, `.vscode/`.
- The target service is the local Ollama API at `http://localhost:11434`. The chosen test model is `qwen2.5:0.5b` for speed; do not switch models without updating `test-config.properties`.
- Assertions must follow the strategy in `project_structure.md` section 5: structural checks only (status, JSON shape, field presence, numeric thresholds, response time). No semantic content matching on LLM output.
- All generation/chat requests must set `"stream": false` and use `"options": { "temperature": 0, "seed": 42 }` to reduce variance.
- POJOs must align with Ollama's snake_case JSON. Use Jackson `PropertyNamingStrategies.SNAKE_CASE` on the ObjectMapper or `@JsonProperty` per field.
- Negative tests must observe Ollama's real response (status code, error body) before finalizing assertions. Do not assume a status code based on documentation or general REST conventions.
- Response time assertions must use generous upper bounds (e.g., `lessThan(60_000L)` for generate/chat) to tolerate slow hardware. Do not write tight timing assertions.
- Test method names: English, `should...` convention (e.g., `shouldReturnDoneTrueForSimplePrompt`).
- Code comments and README: Turkish, since the deliverable is presented in Turkish.
- Test classes must use the `@Tag("smoke")` and/or `@Tag("regression")` annotations as specified in `project_plan.md`.

## Scope discipline

The following are **explicitly out of scope** for this submission and must not be added without an explicit request:

- WireMock or any HTTP stubbing layer
- Allure, ExtentReports, or any non-Surefire reporting
- Docker, Docker Compose, or container orchestration
- GitHub Actions or any CI pipeline configuration
- Streaming endpoint tests (NDJSON parsing)
- JSON Schema validation libraries
- OpenAI-compatible endpoint tests (`/v1/...`)
- Embedding endpoint tests (`/api/embeddings`)
- Parameterized prompt-robustness sweeps
- Property-based testing libraries

If a task touches anything on this list, stop and ask before proceeding. Do not proactively suggest adding them.

## Task workflow

For any non-trivial task:

1. Read `project_structure.md` and `project_plan.md` end to end.
2. Identify which phase in `project_plan.md` the task belongs to.
3. Before editing, summarize:
   - the task goal,
   - the files to be created or changed,
   - the acceptance criteria from the relevant phase,
   - any deviation from the plan (if proposed).
4. Wait for user approval if the task is broad, affects multiple phases, or proposes a deviation.
5. Implement only the current phase. Do not pull work forward from later phases.
6. Run `mvn clean test` (or the relevant filtered command) and report the result.
7. At the end of each phase, stop at the checkpoint defined in `project_plan.md` and wait for user review before continuing to the next phase.
8. Update `README.md` only in Phase 6 unless the user explicitly requests an earlier update.

## When in doubt

- Prefer the option that is simpler, more readable, and closer to the documented plan.
- Ask the user rather than guess. A short clarification beats a wrong assumption that propagates across phases.
- Do not refactor unrelated code while working on a task. Keep diffs focused.
