# Development Plan — Ollama API Regression Test Automation

> Incremental development plan. Each phase ends with a working artifact and a checkpoint. The next phase begins only after user review at the checkpoint.

---

## Phase 0 — Project Skeleton (~20 min)

**Goal:** an empty but compilable Maven project.

**Tasks:**
- [ ] Initialize the Maven project (`mvn archetype:generate` or a hand-written `pom.xml`)
- [ ] Add dependencies to `pom.xml`:
  - `io.rest-assured:rest-assured:5.5.7`
  - `org.junit.jupiter:junit-jupiter:5.11.3` (BOM acceptable)
  - `com.fasterxml.jackson.core:jackson-databind:2.18.2`
  - `org.hamcrest:hamcrest:3.0`
- [ ] Configure `maven-compiler-plugin` for Java 17
- [ ] Configure `maven-surefire-plugin:3.5.2` for JUnit 5 Platform
- [ ] Add `.gitignore` (`target/`, `.idea/`, `*.iml`, `.vscode/`)
- [ ] Empty `README.md` (filled in Phase 6)
- [ ] Create the directory tree per `project_structure.md`

**Definition of done:** `mvn clean compile` exits with no errors.

**Checkpoint:** ✋ Stop after the build succeeds and notify the user.

---

## Phase 1 — Test Infrastructure (~30 min)

**Goal:** the helper classes that every test will lean on.

**Tasks:**
- [ ] `TestConfig.java`:
  - Resolution order: system property → environment variable → properties file → default
  - Public methods: `getBaseUrl()`, `getTestModel()`, `getTimeoutMs()`
- [ ] `Endpoints.java`:
  - `public static final String TAGS = "/api/tags";`
  - Constants for `GENERATE` and `CHAT`
- [ ] `OllamaSpecs.java`:
  - `defaultRequestSpec()` — base URI, `Content-Type: application/json`
  - `defaultResponseSpec()` — optional shared assertions
- [ ] `test-config.properties` with default values
- [ ] `junit-platform.properties` — `junit.jupiter.execution.parallel.enabled=false` (parallel calls to a local LLM are not useful)
- [ ] `BaseTest.java`:
  - `@BeforeAll static void setup()` that sets Rest Assured globals (baseURI, request/response specs)
  - Parent class for all test classes

**Definition of done:** `mvn test` reports "0 tests, 0 failures" cleanly.

**Checkpoint:** ✋ Stop for infrastructure review.

---

## Phase 2 — GET /api/tags Tests (~20 min)

**Goal:** the simplest endpoint as an end-to-end smoke path.

**Tasks:**
- [ ] POJOs: `model/tags/TagsResponse.java` and `ModelInfo.java`
- [ ] `TagsApiTest.java`:
  - **`@Tag("smoke")`** `shouldReturn200WhenListingModels()` — status 200
  - `shouldReturnJsonContentType()` — Content-Type check
  - `shouldContainModelsArray()` — `body("models", notNullValue())` and `instanceOf(List.class)`
  - `shouldRespondUnderTimeout()` — `time(lessThan(5_000L))`
  - `shouldDeserializeToTagsResponse()` — POJO mapping smoke

**Precondition:** Ollama is running and at least one model has been pulled (`ollama pull qwen2.5:0.5b`).

**Definition of done:** 5 green tests.

**Checkpoint:** ✋ Share the test output with the user.

---

## Phase 3 — POST /api/generate Tests (~45 min)

**Goal:** the first endpoint with a request body; the core LLM tests.

**Tasks:**
- [ ] `GenerateRequest.java`:
  - Fields: `model`, `prompt`, `stream`, `options` (Map or nested class), `format`
- [ ] `GenerateResponse.java`:
  - Fields: `model`, `created_at`, `response`, `done`, `done_reason`, `context`, `total_duration`, `load_duration`, `prompt_eval_count`, `eval_count`
- [ ] `payloads/generate-deterministic.json` — fixed prompt with `temperature=0`, `seed=42`
- [ ] `GenerateApiTest.java`:
  - **`@Tag("smoke")`** `shouldGenerateResponseForSimplePrompt()` — status 200, `response` non-empty
  - `shouldReturnDoneTrue()` — `body("done", equalTo(true))`
  - `shouldReportEvalCountGreaterThanZero()` — `body("eval_count", greaterThan(0))`
  - `shouldReturnModelNameInResponse()` — `body("model", equalTo(testModel))`
  - `shouldRespondUnderTimeout()` — `time(lessThan(60_000L))`
  - `shouldHandleDeterministicOptions()` — request honors `temperature=0`, `seed=42`
- [ ] `GenerateNegativeTest.java`:
  - `shouldFailForInvalidModelName()` — non-existent model
  - `shouldHandleEmptyPrompt()` — empty prompt behavior
  - `shouldReturn400ForMalformedJson()` — malformed JSON body

**Note:** for negative tests, Ollama's actual response (status code, error body shape) must be observed in the first run and the assertions tightened to match. Do not assume HTTP codes from general REST conventions.

**Definition of done:** ~9 green tests.

**Checkpoint:** ✋ Inspect negative-test results; surface unexpected behavior to the user.

---

## Phase 4 — POST /api/chat Tests (~30 min)

**Goal:** multi-turn conversation interface.

**Tasks:**
- [ ] POJOs: `ChatRequest.java`, `ChatResponse.java`, `Message.java`
  - `Message`: `role` (system / user / assistant), `content`
- [ ] `payloads/chat-multi-turn.json` — system + user messages
- [ ] `ChatApiTest.java`:
  - **`@Tag("smoke")`** `shouldReturnAssistantMessageForUserPrompt()` — status 200, `message.role == assistant`
  - `shouldReturnNonEmptyContent()` — `body("message.content", not(emptyString()))`
  - `shouldHandleMultiTurnConversation()` — system + user history
  - `shouldRespondUnderTimeout()`
  - `shouldReturnDoneTrue()`
- [ ] `ChatNegativeTest.java`:
  - `shouldFailForEmptyMessagesArray()` — empty `messages: []`
  - `shouldFailForInvalidRole()` — `role: "invalid_role"`
  - `shouldFailForMissingModel()` — missing `model` field

**Definition of done:** ~8 green tests.

**Checkpoint:** ✋ All endpoints covered; share results.

---

## Phase 5 — Tagging and Maven Profiles (~15 min)

**Goal:** smoke / regression separation and demo-ready commands for the presentation.

**Tasks:**
- [ ] Add `@Tag("regression")` to every test (smoke tests get both tags)
- [ ] Update Surefire config to honor the `groups` property:
  - `<groups>${test.groups}</groups>` with a default empty value
- [ ] Verify:
  - `mvn test -Dgroups=smoke` → smoke only (3 tests)
  - `mvn test -Dgroups=regression` → full suite
  - `mvn test` → full suite (default)

**Definition of done:** all three commands run cleanly.

**Checkpoint:** ✋ Confirm filtering works before moving on.

---

## Phase 6 — Documentation (~30 min)

**Goal:** a `README.md` the grader can read in one pass and act on.

**Tasks:**
- [ ] `README.md` sections:
  1. **About** — 2–3 sentences: assignment context + LLM testing angle
  2. **Architecture** — short paragraph; link to `project_structure.md`
  3. **Requirements** — Java 17, Maven 3.9+, Ollama
  4. **Setup**:
     ```bash
     # Ollama (https://ollama.com)
     ollama serve
     ollama pull qwen2.5:0.5b

     # Project
     git clone <repo>
     cd ollama-api-tests
     mvn clean test
     ```
  5. **Configuration** — env var overrides
  6. **Running tests** — smoke / regression / single class
  7. **Test strategy** — short version of `project_structure.md` section 5 (structural assertions, determinism approach)
  8. **Known limitations** — non-deterministic LLM, hardware-dependent timing

**Definition of done:** README is self-contained and matches the actual project state.

**Checkpoint:** ✋ Stop for README review.

---

## Phase 7 — Final Pass (~15 min)

**Goal:** pre-submission checklist.

**Tasks:**
- [ ] `mvn clean test` is fully green
- [ ] `mvn clean package` succeeds
- [ ] Surefire HTML report inspected (`target/surefire-reports/`)
- [ ] `docs/presentation/` placeholder exists for the slide deck
- [ ] `.gitignore` works — `target/` is not staged
- [ ] Repository description and README top section are clean
- [ ] First commit prepared

**Definition of done:** ready to push the public repository.

---

## Time Estimate

| Phase | Duration |
|---|---|
| 0. Skeleton | 20 min |
| 1. Infrastructure | 30 min |
| 2. Tags | 20 min |
| 3. Generate | 45 min |
| 4. Chat | 30 min |
| 5. Tagging | 15 min |
| 6. README | 30 min |
| 7. Final | 15 min |
| **TOTAL** | **~3.5 hours of active development** |

LLM response time (proportional to model size) and the first-run observation pass on negative tests add to this.

---

## Out-of-Scope Extensions

This plan deliberately excludes the items listed in `AGENTS.md` "Scope discipline". They may be revisited in a separate conversation.

---

## Working Rules

1. **Do not start a phase until the previous one is finished.** Each phase ships a working artifact.
2. **Stop at every checkpoint.** Wait for user review.
3. **Do not exceed scope.** Anything in `AGENTS.md` "Scope discipline" is off-limits.
4. **Test names in English** using the `should...` convention.
5. **POJO field names** must match Ollama's snake_case JSON, via Jackson `PropertyNamingStrategies.SNAKE_CASE` or `@JsonProperty`.
