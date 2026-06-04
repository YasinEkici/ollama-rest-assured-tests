# Project Structure — Ollama API Regression Test Automation

> A Java 17 + Maven + JUnit 5 + Rest Assured project that exercises the local Ollama service through automated API regression tests. Submitted as coursework for the Software Test Engineering class.

---

## 1. Technology Stack

| Layer | Choice | Version | Rationale |
|---|---|---|---|
| Language | Java | 17 | Required by Rest Assured 5.5+ and modern Maven plugins |
| Build | Maven | 3.9+ | Required by the assignment |
| Test framework | JUnit Jupiter | 5.11.3 | Modern, supports `@Tag`-based filtering and parameterized tests |
| HTTP / assertions | Rest Assured | 5.5.7 | Required by the assignment; BDD-style fluent API |
| JSON mapping | Jackson Databind | 2.18.2 | Native integration with Rest Assured, POJO ↔ JSON |
| Test runner | Maven Surefire | 3.5.2 | First-class JUnit 5 Platform support, tag filtering |

**Target service:** local Ollama server at `http://localhost:11434`, exercised against a small model (`qwen2.5:0.5b` by default) to keep runs fast.

---

## 2. Directory Layout

```
ollama-api-tests/
├── pom.xml
├── README.md
├── AGENTS.md
├── project_structure.md
├── project_plan.md
├── .gitignore
├── docs/
│   ├── presentation/                  # Slides (.pptx / .pdf) placed here
│   └── screenshots/                   # Images for README / presentation
│
├── src/
│   └── test/
│       ├── java/com/example/ollamatests/
│       │   ├── BaseTest.java          # @BeforeAll lifecycle, RestAssured global config
│       │   ├── client/
│       │   │   ├── Endpoints.java     # Endpoint path constants
│       │   │   └── OllamaSpecs.java   # Request/Response Specification factory
│       │   ├── config/
│       │   │   └── TestConfig.java    # Env + properties loader (BASE_URL, MODEL, TIMEOUT)
│       │   ├── model/
│       │   │   ├── generate/
│       │   │   │   ├── GenerateRequest.java
│       │   │   │   └── GenerateResponse.java
│       │   │   ├── chat/
│       │   │   │   ├── ChatRequest.java
│       │   │   │   ├── ChatResponse.java
│       │   │   │   └── Message.java
│       │   │   └── tags/
│       │   │       ├── TagsResponse.java
│       │   │       └── ModelInfo.java
│       │   ├── tags/
│       │   │   └── TagsApiTest.java   # GET /api/tags
│       │   ├── generate/
│       │   │   ├── GenerateApiTest.java       # Positive scenarios
│       │   │   └── GenerateNegativeTest.java  # Error / edge-case scenarios
│       │   └── chat/
│       │       ├── ChatApiTest.java
│       │       └── ChatNegativeTest.java
│       │
│       └── resources/
│           ├── test-config.properties         # Default values (overridable via env)
│           ├── junit-platform.properties      # JUnit 5 configuration
│           └── payloads/
│               ├── generate-deterministic.json
│               └── chat-multi-turn.json
```

---

## 3. Layer Responsibilities

### `src/test` — Test Suite Code

This repository is a single-module API test suite, not an application with separate production code. Helper infrastructure lives under `src/test/java` so Rest Assured can remain a test-scoped Maven dependency while test classes stay focused on the given-when-then flow.

- **`client/Endpoints.java`** — String constants for `/api/tags`, `/api/generate`, `/api/chat`. Keeps endpoint paths out of test bodies.
- **`client/OllamaSpecs.java`** — Factory methods for `RequestSpecification` and `ResponseSpecification`. Centralizes base URI, content type, and default timeout configuration.
- **`config/TestConfig.java`** — Configuration loader. Precedence: system property → environment variable → `test-config.properties` → hardcoded default. No raw `localhost:11434` strings in test code.
- **`model/*`** — POJOs for request and response bodies. Jackson handles serialization both ways; mismatched field names fail at compile time rather than at runtime.

### Endpoint Tests And Resources

- **`BaseTest.java`** — Common parent class. `@BeforeAll` initializes Rest Assured globals (base URI, request/response specs). All test classes extend this.
- **One package per endpoint** — `tags/`, `generate/`, `chat/`. Positive and negative scenarios live in separate test classes so that a single file never balloons.
- **`resources/payloads/`** — Complex JSON request bodies live in versioned files rather than being embedded as Java strings. Tests stay readable.

---

## 4. Test Tagging Strategy (`@Tag`)

The suite is partitioned with JUnit 5 tags:

- **`@Tag("smoke")`** — One minimum-viable test per endpoint. Fast (~10 seconds total), suitable for every commit.
- **`@Tag("regression")`** — Full positive + negative suite (~1–2 minutes).

Maven Surefire filtering:
```bash
mvn test -Dgroups=smoke          # smoke only
mvn test -Dgroups=regression     # regression only
mvn test                         # all tests (default)
```

This provides a concrete demo for the "smoke vs regression" and "test pyramid" sections of the presentation.

---

## 5. Assertion Strategy (LLM Non-Determinism)

Because LLM responses are not deterministic, assertions are restricted to **structural** checks:

| Check type | Example | Strategy |
|---|---|---|
| HTTP status | `statusCode(200)` | ✅ Use |
| JSON shape | `body("response", notNullValue())` | ✅ Use |
| Field presence / type | `body("done", equalTo(true))` | ✅ Use |
| Numeric threshold | `body("eval_count", greaterThan(0))` | ✅ Use |
| Response time | `time(lessThan(60_000L))` | ✅ Use, with hardware tolerance |
| Semantic content | `body("response", containsString("Paris"))` | ⚠️ Avoid; only acceptable in smoke tests with a very strong prompt→answer anchor |
| Exact string match | `body("response", equalTo("..."))` | ❌ Never |

To reduce variance, every generation/chat request must set:
- `"stream": false`
- `"options": { "temperature": 0, "seed": 42 }`

This does not eliminate non-determinism (model version, hardware, and Ollama version still vary), but it minimizes it for stable regression behavior.

---

## 6. Configuration Management

`test-config.properties`:
```properties
ollama.base.url=http://localhost:11434
ollama.test.model=qwen2.5:0.5b
ollama.timeout.ms=60000
```

Environment variable override:
```bash
OLLAMA_BASE_URL=http://192.168.1.50:11434 mvn test
```

This keeps the suite portable across machines and CI environments without code changes.

---

## 7. Deliberate Exclusions

To preserve the assignment's scope, the following are **not** included in this submission. See `AGENTS.md` "Scope discipline" for the full list and enforcement rules.

- **WireMock / mock layer** — Useful for letting graders run tests without Ollama, but the assignment requires a live demo by the student, not third-party reproduction. The added surface area outweighs the benefit for this deliverable.
- **Allure / ExtentReports** — Surefire's default HTML report is sufficient.
- **Docker Compose** — README setup instructions are sufficient.
- **Streaming endpoint tests** — All requests use `stream: false`; NDJSON parsing is avoided.
- **OpenAI-compatible endpoints (`/v1/...`)** — Focus is on native Ollama endpoints.
- **Embedding endpoint** — Out of scope.

These can be revisited in a future iteration. The first deliverable prioritizes a clean, correct, and presentable suite.
