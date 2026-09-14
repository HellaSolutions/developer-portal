# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

This is a Spring Boot project (group `com.media.portal`, artifact `developer-portal`) described in
`README.md` as managing "APIs published on the platform and the consumers who get keys to call them." The
full vertical slice is implemented: domain model, Flyway-backed persistence, repositories, services,
REST controllers, validation, structured error responses, a dual security setup (gateway shared-secret +
JWT resource server), and API-key hashing/introspection. `./mvnw test` passes.

Configuration lives in `src/main/resources/application.yml` (+ an `application-dev.yml` profile), not
`application.properties`. Neither file carries `spring.datasource.*` properties — connection details come
entirely from whatever supplies the datasource at runtime (Testcontainers `@ServiceConnection` in tests,
Docker Compose in local `spring-boot:run`; see below). There's no property-based fallback for pointing the
app at an arbitrary external Postgres instance.

## Commands

Use the Maven wrapper (`mvnw`/`mvnw.cmd`) rather than a system Maven install.

```
./mvnw spring-boot:run          # run the app locally
./mvnw clean package            # build the jar (target/developer-portal-0.0.1-SNAPSHOT.jar)
./mvnw test                     # run the full test suite
./mvnw test -Dtest=DeveloperPortalApplicationTests            # run a single test class
./mvnw test -Dtest=DeveloperPortalApplicationTests#contextLoads   # run a single test method
```

On Windows use `mvnw.cmd` in place of `./mvnw` from cmd.exe/PowerShell (the Bash tool's POSIX shell can use
`./mvnw` directly).

Java toolchain: **Java 26** (`java.version` in `pom.xml`) on **Spring Boot 4.1.1**.

**Docker is required to run tests**: `DeveloperPortalApplicationTests` pulls in `TestcontainersConfiguration`,
which starts a real `postgres:17` container per test run via `@ServiceConnection` — there is no mocked/H2
fallback. Docker Desktop (or an equivalent daemon) must be running first.

For local dev, `spring-boot:run` uses Spring Boot's Docker Compose support to auto-start the `postgres:17`
service defined in `compose.yaml`.

## Architecture / stack notes

- **Base package**: `com.media.portal.developerportal`. New code should live under
  `src/main/java/com/media/portal/developerportal/...`, in the same `model` / `repositories` / `services` /
  `controllers` (with a `dto` subpackage) / `config` / `filters` / `utils` split used today.
- **Domain model** (`com.media.portal.developerportal.model`), backed by Flyway migrations
  `V1__create_api.sql` and `V2__create_api.sql` under `src/main/resources/db/migration`:
    - `Api` — id, name (unique), basePath (unique, e.g. `/scopus/v1`), status (`DRAFT`/`PUBLISHED`/
      `DEPRECATED`), ownerTeam, openApiSpec (text), createdAt/updatedAt. Status is not settable directly;
      `publish()` and `deprecate()` enforce legal transitions (`publish()` also requires a non-blank
      `openApiSpec`) and throw `IllegalStateTransitionException` otherwise.
    - `Consumer` — id, name, email (unique), organisation, createdAt/updatedAt.
    - `Subscription` — links a `Consumer` to a published `Api`: plan (`SubscriptionType`: `FREE`/`STANDARD`/
      `PARTNER`), status (`SubscriptionStatus`: `ACTIVE`/`SUSPENDED`/`REVOKED`), createdAt. V2 adds a partial
      unique index (`consumer_id, api_id` where `status <> 'REVOKED'`) so a consumer can hold at most one
      non-revoked subscription per API; `ApiService.deprecateApi` bulk-transitions a deprecated API's
      `ACTIVE` subscriptions to `SUSPENDED`.
    - `ApiKey` — belongs to a `Subscription`: keyHash, prefix (first chars, for display), createdAt,
      expiresAt, revokedAt. The raw key is returned once at creation (`SubscriptionService.generateKey`,
      via `ApiKeyCreateResponse`) and never stored — only its SHA-256 hash. Table/column names (`api_key`,
      `key_hash`) must keep matching Hibernate's default snake_case naming for the `ApiKey`/`keyHash` Java
      names — there's no `@Table`/`@Column(name = ...)` override, so a rename on either side (entity or
      migration) breaks schema validation.
    - Every entity uses a `Long` surrogate PK (`@GeneratedValue(SEQUENCE)`, `allocationSize = 50` hi-lo
      batching via a dedicated `*_seq` sequence) plus a separate immutable `UUID uuid`
      (`updatable = false`, assigned client-side via `UUID.randomUUID()`) as the stable public identity.
      `equals`/`hashCode` compare only `uuid`, using `Hibernate.getClass()` (not `instanceof`) so proxy
      instances compare correctly.
    - `@EnableJpaAuditing` is active (`config.JpaAuditingConfig`), so `@CreatedDate`/`@LastModifiedDate` on
      entities are live, not decorative.
- **Repositories** (`com.media.portal.developerportal.repositories`) are plain Spring Data
  `JpaRepository`s with a couple of important patterns:
    - `SubscriptionRepository.findByIdForUpdate` and `ApyKeyRepository.findByIdForUpdate` take a
      `PESSIMISTIC_WRITE` lock — used by `SubscriptionService.generateKey`/`revoke` to serialize
      concurrent key issuance/revocation on the same subscription/key row. Note the repository class name
      is `ApyKeyRepository` (typo, not `ApiKeyRepository`) — match it exactly when referencing it.
    - `ApyKeyRepository.findIntrospectionByKeyHash` returns a projection (`IntrospectionView`) joining
      `ApiKey` → `Subscription` → `Api` in one query, purpose-built for the introspect endpoint's hot path.
    - `SubscriptionRepository.updateStatusForApi` is a bulk `@Modifying` update (with
      `clearAutomatically`/`flushAutomatically`) used for the deprecate-cascade above.
- **Services** (`com.media.portal.developerportal.services`) hold all business rules; controllers stay
  thin (map DTOs, delegate, translate to `ResponseEntity`). Exceptions used to signal HTTP-relevant
  failures live alongside the services: `ResourceNotFoundException` (404), `BadRequestException` (400),
  `ConflictException` (409) — plus the model-level `IllegalStateTransitionException` (409). All are handled
  centrally by `controllers.GlobalExceptionHandler` (a `RestControllerAdvice` extending
  `ResponseEntityExceptionHandler`), which renders RFC 7807 `ProblemDetail` bodies (including bean-validation
  failures collected into a `field`/`message` list) and also maps `DataIntegrityViolationException` to 409.
    - A subscription is capped at 2 live (non-revoked, non-expired) API keys at a time
      (`SubscriptionService.generateKey`); keys expire 7 days after issuance.
    - Key material: `utils.TokenUtil.generateToken()` produces a `pk_`-prefixed, 24-random-byte
      base64url token (asserted to be exactly 35 chars in `generateKey`) plus its SHA-256 hex hash;
      only the hash is persisted, the prefix (first 8 chars) is stored for display, and the plaintext is
      returned exactly once in the create response.
    - `SubscriptionService.introspect` is the validation path a gateway calls per request: hash the raw
      key, look up the joined projection, and return an `Introspection` record (active flag +
      consumerId/apiId/basePath/plan, or an all-null inactive singleton) — active requires the key to be
      unrevoked/unexpired *and* the subscription `ACTIVE` *and* the API `PUBLISHED`. Results are counted via
      Micrometer (`portal.introspection` counter, tagged `active=true|false`).
- **Controllers** (`com.media.portal.developerportal.controllers`), all under `/v1`:
    - `ApiController` (`/v1/apis`) — create, publish, deprecate, list (optionally filtered by `status`,
      paginated via `Pageable`), get-by-id.
    - `ConsumerController` (`/v1/consumers`) — create, get-by-id, and
      `POST /v1/consumers/{id}/subscriptions` to subscribe a consumer to an API.
    - `SubscriptionController` (`/v1/subscriptions`) — `POST /{id}/keys` to issue a key,
      `DELETE /{id}/keys/{keyId}` to revoke one.
    - `IntrospectController` (`/v1/introspect`) — the gateway-facing key-validation endpoint; the only
      one guarded by the `GATEWAY` authority instead of a JWT scope (see Security below).
    - DTOs live in `controllers.dto` as validated records (Jakarta Bean Validation annotations, e.g.
      `ApiCreateRequest.basePath` enforces the `/name/vN` shape via regex).
- **Security** (`config.SecurityConfig`, `spring-boot-starter-security` +
  `spring-boot-starter-oauth2-resource-server`) defines three ordered, stateless `SecurityFilterChain`s:
    1. `/v1/introspect/**` — `GatewayKeyFilter` (a `OncePerRequestFilter`) checks a constant-time-compared
       `X-Gateway-Key` header against `app.security.token` (default `my-default-secret-token`, override in
       real environments) and grants the `GATEWAY` authority; no JWT involved on this path.
    2. `/actuator/health/**` — open (`permitAll`).
    3. Everything else — JWT resource server (`oauth2ResourceServer().jwt()`), verified against the RSA
       public key at `src/main/resources/jwt-public.pem` (configured via
       `spring.security.oauth2.resourceserver.jwt.public-key-location`), requiring the
       `SCOPE_portal:admin` authority. There's no issuer/JWK-set wiring yet — it's a static public key, so
       token issuance/rotation is out of scope of this app.
    - `filters.CorrelationIdFilter` runs at `Ordered.HIGHEST_PRECEDENCE` (outside the security chains,
      registered as a plain `@Component`), threading an `X-Correlation-Id` header through MDC for
      structured logs and echoing it back on the response.
- **Persistence**: `spring-boot-starter-data-jpa` + `postgresql` (runtime driver) + Flyway
  (`flyway-core`, `flyway-database-postgresql` for the Postgres dialect, and
  `spring-boot-starter-flyway` for the Spring Boot autoconfiguration glue — Spring Boot 4 split Flyway's
  Boot integration and per-database dialect support into their own modules, so all three are needed
  together; `flyway-core` alone is inert). `spring.jpa.hibernate.ddl-auto=validate` in
  `application.yml` means Hibernate never generates DDL itself; the schema comes entirely from Flyway
  migrations under `src/main/resources/db/migration`.
- **Local/test Postgres**, two separate mechanisms — don't conflate them:
    - Tests: `spring-boot-testcontainers` + `testcontainers-postgresql` (test scope). See
      `TestcontainersConfiguration` (a `@TestConfiguration` with a `@ServiceConnection`-annotated
      `PostgreSQLContainer` bean, imported into `DeveloperPortalApplicationTests`) and
      `TestDeveloperPortalApplication` (a test-scope `main()` for running the full app locally against the same
      Testcontainers-managed Postgres instead of `compose.yaml`).
    - Local run: `spring-boot-docker-compose` (optional dependency) + `compose.yaml` at the repo root, auto-
      detected by `spring-boot:run`.
    - Testcontainers 2.x (managed via the `testcontainers-bom` import in `spring-boot-dependencies`) renamed
      its module artifacts with a `testcontainers-` prefix (e.g. `testcontainers-postgresql`, not
      `postgresql`) and dropped the generic self-type from container classes — use `PostgreSQLContainer`, not
      `PostgreSQLContainer<?>`/`new PostgreSQLContainer<>(...)`. Import from `org.testcontainers.postgresql`,
      not the older `org.testcontainers.containers` package.
- **Observability stack** is pre-wired and should be kept in mind when adding components:
    - Micrometer tracing via Brave (`spring-boot-micrometer-tracing-brave`,
      `micrometer-tracing-bridge-brave`).
    - Metrics via `spring-boot-starter-micrometer-metrics`, exported to **New Relic**
      (`micrometer-registry-new-relic`, runtime scope) — expect New Relic export config
      (license key/account id) to be required via env vars or properties, not hardcoded. Disabled in the
      `dev` profile (`application-dev.yml` sets `management.newrelic.metrics.export.enabled=false`).
    - DataSource-level observability via `datasource-micrometer-spring-boot`
      (version pinned through the `datasource-micrometer-bom` in `dependencyManagement`,
      property `datasource-micrometer.version`).
    - Structured JSON (ECS format) console logging is on by default (`logging.structured.format.console:
      ecs` in `application.yml`), turned back off in the `dev` profile for human-readable console output.
- **Testing**: JUnit 5 via `spring-boot-starter-test` (transitively), `spring-boot-webmvc-test` for
  `@WebMvcTest` controller slice tests (e.g. `ConsumerControllerWebMvcTest`), plus the tracing- and
  JPA-specific test starters (`spring-boot-micrometer-tracing-test`, `spring-boot-starter-data-jpa-test`,
  `spring-boot-starter-micrometer-metrics-test`) for integration tests involving tracing, JPA, and metrics
  respectively. Service- and controller-level tests exist per feature (`*ServiceTest`, `*ControllerTest`)
  alongside the Testcontainers-backed full-context test.
- The `pom.xml` intentionally has empty `<license>`/`<developers>`/`<scm>` overrides to stop those blocks
  from being inherited from the `spring-boot-starter-parent` parent POM — leave them empty rather than
  "filling them in" (a `LICENSE` file with unfilled Apache-2.0 boilerplate exists separately at the repo
  root, unrelated to this override).
