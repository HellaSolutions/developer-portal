# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

This is a Spring Boot project generated from Spring Initializr (group `com.media.portal`, artifact
`developer-portal`) and has not yet grown beyond the generated skeleton: a single empty
`@SpringBootApplication` entry point, no controllers/services/entities, no `application.yml` config beyond
the application name, and no `README.md`. Treat the architecture notes below as "what the build is wired
for," not "what exists in code today."

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

## Architecture / stack notes

- **Base package**: `com.media.portal.developerportal`. New code should live under
  `src/main/java/com/media/portal/developerportal/...`, mirroring the existing `DeveloperPortalApplication`
  package root.
- **Persistence**: `spring-boot-starter-data-jpa` + `postgresql` (runtime driver) are on the classpath —
  this app is expected to talk to PostgreSQL via Spring Data JPA, but no datasource config, entities, or
  repositories exist yet. A datasource URL/credentials will need to be added (e.g. via
  `application.properties`/`application.yml` or environment variables) before the app can start against a
  real database.
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
  "filling them in" unless the project actually gains a license/SCM.
