# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

This is a Spring Boot project (group `com.media.portal`, artifact `developer-portal`) described in
`README.md` as managing "APIs published on the platform and the consumers who get keys to call them." It has
grown past the generated skeleton: a domain model exists (`Api`, `Consumer`, `Subscription`, `ApiKey`) backed
by a working Flyway migration, and JPA auditing is enabled. `./mvnw test` passes. **No repositories,
services, or REST controllers exist yet** — `spring-boot-starter-web` and `spring-boot-starter-validation`
are on the classpath but unused so far, so there is no HTTP layer to hit.

`src/main/resources/application.properties` carries no `spring.datasource.*` properties at all — connection
details come entirely from whatever supplies the datasource at runtime (Testcontainers `@ServiceConnection`
in tests, Docker Compose in local `spring-boot:run`; see below). There's no property-based fallback for
pointing the app at an arbitrary external Postgres instance.

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
  `src/main/java/com/media/portal/developerportal/...`. Domain model classes currently live in a `model`
  subpackage.
- **Domain model** (`com.media.portal.developerportal.model`), backed by
  `src/main/resources/db/migration/V1__create_api.sql`:
    - `Api` — id, name (unique), basePath (unique, e.g. `/scopus/v1`), status (`DRAFT`/`PUBLISHED`/
      `DEPRECATED`), ownerTeam, openApiSpec (text), createdAt/updatedAt. Status is not settable directly;
      `publish()` and `deprecate()` enforce legal transitions (`publish()` also requires a non-blank
      `openApiSpec`) and throw `IllegalStateTransitionException` otherwise.
    - `Consumer` — id, name, email (unique), organisation, createdAt/updatedAt.
    - `Subscription` — links a `Consumer` to a published `Api`: plan (`SubscriptionType`: `FREE`/`STANDARD`/
      `PARTNER`), status (`SubscriptionStatus`: `ACTIVE`/`SUSPENDED`/`REVOKED`), createdAt.
    - `ApiKey` — belongs to a `Subscription`: keyHash, prefix (first chars, for display), createdAt, expiresAt,
      revokedAt. The raw key is intended to be returned once at creation and never stored — hashing/generation
      isn't implemented yet, just the storage shape. Table/column names (`api_key`, `key_hash`) must keep
      matching Hibernate's default snake_case naming for the `ApiKey`/`keyHash` Java names — there's no
      `@Table`/`@Column(name = ...)` override, so a rename on either side (entity or migration) breaks schema
      validation.
    - Every entity uses a `Long` surrogate PK (`@GeneratedValue(SEQUENCE)`, `allocationSize = 50` hi-lo
      batching via a dedicated `*_seq` sequence) plus a separate immutable `UUID uuid`
      (`updatable = false`, assigned client-side via `UUID.randomUUID()`) as the stable public identity.
      `equals`/`hashCode` compare only `uuid`, using `Hibernate.getClass()` (not `instanceof`) so proxy
      instances compare correctly.
    - `@EnableJpaAuditing` is active on `DeveloperPortalApplication`, so `@CreatedDate`/`@LastModifiedDate`
      on entities are live, not decorative.
- **Persistence**: `spring-boot-starter-data-jpa` + `postgresql` (runtime driver) + Flyway
  (`flyway-core`, `flyway-database-postgresql` for the Postgres dialect, and
  `spring-boot-starter-flyway` for the Spring Boot autoconfiguration glue — Spring Boot 4 split Flyway's
  Boot integration and per-database dialect support into their own modules, so all three are needed
  together; `flyway-core` alone is inert). `spring.jpa.hibernate.ddl-auto=validate` in
  `application.properties` means Hibernate never generates DDL itself; the schema comes entirely from
  Flyway migrations under `src/main/resources/db/migration`.
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
- **Web layer**: `spring-boot-starter-web` and `spring-boot-starter-validation` are declared but nothing uses
  them yet — no `@RestController`, no repositories. This is the natural next layer to add on top of the
  domain model.
- **Observability stack** is pre-wired and should be kept in mind when adding components:
    - Micrometer tracing via Brave (`spring-boot-micrometer-tracing-brave`,
      `micrometer-tracing-bridge-brave`).
    - Metrics via `spring-boot-starter-micrometer-metrics`, exported to **New Relic**
      (`micrometer-registry-new-relic`, runtime scope) — expect New Relic export config
      (license key/account id) to be required via env vars or properties, not hardcoded.
    - DataSource-level observability via `datasource-micrometer-spring-boot`
      (version pinned through the `datasource-micrometer-bom` in `dependencyManagement`,
      property `datasource-micrometer.version`).
- **Testing**: JUnit 5 via `spring-boot-starter-test` (transitively), plus the tracing- and
  JPA-specific test starters (`spring-boot-micrometer-tracing-test`,
  `spring-boot-starter-data-jpa-test`, `spring-boot-starter-micrometer-metrics-test`) for slice/integration
  tests involving tracing, JPA, and metrics respectively.
- The `pom.xml` intentionally has empty `<license>`/`<developers>`/`<scm>` overrides to stop those blocks
  from being inherited from the `spring-boot-starter-parent` parent POM — leave them empty rather than
  "filling them in" (a `LICENSE` file with unfilled Apache-2.0 boilerplate exists separately at the repo
  root, unrelated to this override).
