# AGENTS.md

Operational guide for coding agents working in this repository.

## Project Snapshot

- Stack: Java 21 + Quarkus 3 (Maven build)
- Build tool: Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Main source: `src/main/java`
- Tests: `src/test/java`
- Runtime config entrypoint: `src/main/resources/application.properties`
- Packaging: Quarkus fast-jar (`target/quarkus-app/quarkus-run.jar`)

## Command Reference

Use the Maven wrapper so local Maven installation differences do not affect builds.

### Windows (PowerShell)

- Dev mode: `./mvnw.cmd quarkus:dev`
- Clean: `./mvnw.cmd clean`
- Compile (no tests): `./mvnw.cmd clean compile`
- Unit tests: `./mvnw.cmd test`
- Package (skip integration tests by default): `./mvnw.cmd clean package`
- Verify (runs unit tests + failsafe lifecycle): `./mvnw.cmd verify`
- Native build: `./mvnw.cmd clean package -Dnative`

### macOS/Linux

- Dev mode: `./mvnw quarkus:dev`
- Clean: `./mvnw clean`
- Compile (no tests): `./mvnw clean compile`
- Unit tests: `./mvnw test`
- Package (skip integration tests by default): `./mvnw clean package`
- Verify (runs unit tests + failsafe lifecycle): `./mvnw verify`
- Native build: `./mvnw clean package -Dnative`

## Running A Single Test (Important)

### Single JUnit test class

- `./mvnw.cmd -Dtest=GreetingResourceTest test`
- Linux/macOS equivalent: `./mvnw -Dtest=GreetingResourceTest test`

### Single JUnit test method

- `./mvnw.cmd -Dtest=GreetingResourceTest#testHelloEndpoint test`
- Linux/macOS equivalent: `./mvnw -Dtest=GreetingResourceTest#testHelloEndpoint test`

### Single integration test (failsafe)

- `./mvnw.cmd -Dit.test=GreetingResourceIT verify -DskipITs=false`
- Linux/macOS equivalent: `./mvnw -Dit.test=GreetingResourceIT verify -DskipITs=false`

Notes:
- `pom.xml` sets `skipITs=true` by default.
- Integration tests are executed by the Maven Failsafe plugin in `integration-test` and `verify`.
- For local quick loops, prefer `-Dtest=... test` unless integration behavior is required.

## Build/Lint/Test Expectations

- There is no dedicated lint plugin configured (no Checkstyle/Spotless/PMD in `pom.xml`).
- Use `./mvnw.cmd clean compile` as the minimum static safety check.
- Use `./mvnw.cmd test` before finishing code changes.
- Use `./mvnw.cmd verify` when touching integration boundaries (HTTP endpoints, persistence, external provider adapters).
- If touching packaging/runtime behavior, confirm `./mvnw.cmd clean package` still succeeds.

## Code Style Guidelines

Follow existing repository conventions over generic Java defaults.

### Imports

- Group imports in this order:
  1. JDK (`java.*`, `javax.*` if present)
  2. Third-party libraries (`io.*`, `org.*`, `com.*`, etc.)
  3. Project packages (`service.cloud.request...`, `com.miempresa...`)
- Keep static imports separate and placed after normal imports.
- Avoid wildcard imports in new code unless already established in the file and necessary.
- Do not reorder imports if a formatter is not enforced and the file has a stable local style.

### Formatting

- Use 4 spaces for indentation; do not use tabs.
- Keep one top-level public class per file.
- Keep methods focused and small when practical.
- Preserve existing brace style (K&R / same-line braces).
- Keep line length readable (target ~120 chars; break fluent chains sensibly).
- Avoid introducing decorative comment banners in new code.

### Types and Modeling

- Java version is 21; use modern language features only when they improve readability and match surrounding code.
- Prefer explicit domain types over `Map<String, Object>` for internal flows.
- DTO/model classes frequently use Lombok (`@Data`, `@Getter/@Setter`, `@Builder`); keep usage consistent with neighboring classes.
- Do not add Lombok to a class that currently uses explicit methods unless there is a clear refactor reason.
- For nullable data from external inputs, validate early at boundaries.

### Naming Conventions

- Classes/interfaces: `PascalCase` (`NotificationManager`, `EmailProvider`).
- Methods/fields/local vars: `camelCase`.
- Constants: `UPPER_SNAKE_CASE` (`MAX_ATTEMPTS`, `BATCH_SIZE`).
- Endpoint classes use `*Controller`; business logic classes use `*Service` or `*Manager`.
- Repository abstractions use `*Repository` with concrete adapters in `repo/impl`.
- Keep package naming consistent with existing modules; do not flatten package structure.

### Dependency Injection and Scopes

- Use Quarkus CDI annotations already used in the codebase (`@ApplicationScoped`, `@Inject`, `@Named`).
- Prefer constructor injection for new classes when practical; if field injection is already prevalent in a file/module, stay consistent.
- Keep injected dependencies `private` and avoid mutable reassignment.

### REST/API Patterns

- Use Jakarta REST annotations (`@Path`, `@GET`, `@POST`, `@Consumes`, `@Produces`).
- Return typed responses or `Response` where status control is needed.
- Validate request payloads before entering deep service logic.
- Keep controller methods thin: delegate orchestration to service/manager classes.

### Reactive and Async Code

- Existing code uses both Mutiny (`Uni`) and Reactor (`Mono`); avoid unnecessary cross-conversion.
- If a module is Reactor-based, keep new logic Reactor-based there.
- In scheduled/async flows, ensure errors are logged with context (IDs, document refs, recipient email).
- Avoid blocking calls on event-loop threads; offload blocking work appropriately.

### Error Handling

- Prefer domain-specific exceptions where available (`VenturaExcepcion`, validation/report exceptions).
- Do not silently swallow exceptions.
- Include actionable context in log/error messages (document ID, client, job ID, operation).
- Catch narrow exception types when possible; avoid `catch (Exception)` unless boundary protection is required.
- Preserve cause chaining when rethrowing (`new RuntimeException("...", e)`).

### Logging

- Use SLF4J (`Logger`/`LoggerFactory`) or Lombok `@Slf4j` consistently within a file.
- Use parameterized logging (`log.info("Job {}", jobId)`) instead of string concatenation.
- `info`: business milestones, `warn`: recoverable anomalies, `error`: failed operations.
- Avoid logging secrets (credentials, API keys, raw auth headers).

### Configuration and Secrets

- Externalized config is critical in this repo (`@ConfigProperty`, YAML/property sources).
- Never hardcode credentials, tokens, or environment-specific endpoints.
- New config keys should be documented close to their consumer class and default behavior defined.

### Testing Conventions

- Quarkus tests use `@QuarkusTest`.
- Packaged/integration tests use `@QuarkusIntegrationTest` and often mirror unit test behavior.
- Test class naming: `*Test` for unit/Quarkus tests, `*IT` for integration tests.
- Keep endpoint tests assertion-first and HTTP-centric (status + response body/JSON fields).

## Repository-Specific Notes

- `pom.xml` currently includes both generated/XSD-heavy code and business code in the same module.
- Be careful editing generated XSD-bound classes under `xmlFormatSunat/xsd`; avoid manual edits unless explicitly required.
- Current codebase contains mixed language comments/messages (Spanish/English); preserve local language in touched module.

## Cursor/Copilot Rules Check

- No `.cursorrules` file found.
- No `.cursor/rules/` directory found.
- No `.github/copilot-instructions.md` file found.
- If these files are added later, treat them as higher-priority local agent instructions and update this document.

## Agent Workflow Recommendation

When making changes:

1. Read adjacent classes in the same package before editing.
2. Implement minimal, scoped changes aligned with existing module style.
3. Run at least compile + targeted tests (`-Dtest=...`).
4. Run full `test` (and `verify` when integration behavior changed).
5. Document any new config/env requirements in README or module docs.
