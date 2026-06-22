# ADR-0002: Use Spring Boot with Java 17 for Backend

**Status:** Accepted

## Context

TheRoot requires a backend service that exposes a REST API for managing feature flags. The backend must handle CRUD operations on feature flags, persist data, and support future growth. The team needed to choose a framework and runtime that balances developer productivity, ecosystem maturity, and long-term maintainability.

Key requirements:

- RESTful HTTP API with JSON serialization.
- Data persistence with a relational database.
- Validation, error handling, and CORS support.
- Fast setup for a small-to-medium project.

## Decision

We will use **Spring Boot 3.2.5** with **Java 17** as the backend framework. The project follows a **layered architecture** with the following structure:

```
com.theroot.featureflags
├── FeatureFlagsApplication.java          — Main entry point (@SpringBootApplication)
├── config/
│   └── CorsConfig.java                   — CORS configuration for frontend origin
├── controller/
│   └── FeatureFlagController.java        — REST endpoints at /api/features
├── model/
│   └── FeatureFlag.java                  — JPA entity
├── repository/
│   └── FeatureFlagRepository.java        — Spring Data JPA repository
└── seeder/
    └── FeatureFlagSeeder.java            — CommandLineRunner that seeds 5 default flags
```

**Specific choices:**

| Concern | Decision | Rationale |
|---------|----------|-----------|
| Framework | Spring Boot 3.2.5 | Auto-configuration, embedded server, mature ecosystem. |
| Language | Java 17 (LTS) | Long-term support, enhanced type inference, sealed classes, records. |
| Data access | Spring Data JPA + Hibernate | Declarative repositories, automatic schema generation via `ddl-auto=update`. |
| Validation | `spring-boot-starter-validation` (Jakarta Bean Validation) | Declarative `@NotBlank` constraints on the entity. |
| Build tool | Maven (with `spring-boot-maven-plugin`) | Standard for Spring Boot projects, reproducible builds. |
| Server port | 8080 (via `server.port=8080` in `application.properties`) | Default Spring Boot port, avoids conflicts. |

The `spring-boot-starter-parent` POM provides dependency management, and the project uses `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `h2`, and `spring-boot-starter-validation`.

## Consequences

**Positive:**

- **Rapid development:** Spring Boot's auto-configuration eliminates boilerplate. A single `@SpringBootApplication` class sets up the embedded Tomcat, JPA, and Jackson.
- **Layered separation:** The clear package structure (controller, model, repository, config, seeder) keeps concerns isolated and testable.
- **Mature ecosystem:** Spring Boot has extensive documentation, community support, and third-party integrations.
- **Java 17 LTS:** Long-term support until at least 2029, with performance improvements and language features.
- **Spring Data JPA:** Repository interfaces eliminate manual DAO implementation; methods like `findByKey()` and `existsByKey()` are auto-implemented.

**Negative:**

- **Startup time:** Spring Boot applications can have slower startup compared to lightweight frameworks (e.g., Micronaut, Quarkus). Not a concern for this project's scale.
- **Memory footprint:** An embedded Tomcat + Hibernate stack uses more memory than a simpler HTTP server. Acceptable for a development tool.
- **Vendor lock-in:** Switching away from Spring in the future would require significant rework. Mitigated by keeping business logic decoupled from framework code.
