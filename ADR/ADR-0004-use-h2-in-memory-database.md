# ADR-0004: Use H2 In-Memory Database for Development

**Status:** Accepted

## Context

TheRoot needs a relational database to persist feature flags (id, key, name, description, enabled, createdAt). During development and initial deployment, we need a database that:

- Requires zero installation or infrastructure setup.
- Supports JPA / Hibernate with standard SQL dialect.
- Can be pre-seeded with data for demo and testing purposes.
- Provides a web console for ad-hoc querying.
- Can be swapped for a production-grade database later without major code changes.

## Decision

We will use **H2 in-memory database** for development, configured via `application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:featureflags
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**Key configuration details:**

| Setting | Value | Purpose |
|---------|-------|---------|
| Database name | `featureflags` | In-memory database created on startup. |
| DDL auto | `update` | Hibernate creates/updates tables from entity annotations automatically. |
| H2 console | Enabled at `/h2-console` | Web-based SQL client for debugging (dev only). |
| Show SQL | `true` | Logs generated SQL to stdout for debugging. |

**Data seeding:**

A `CommandLineRunner` bean (`FeatureFlagSeeder`) checks if the database is empty and inserts five default flags:

| Key | Name | Enabled |
|-----|------|---------|
| `dark-mode` | Dark Mode | false |
| `new-checkout` | New Checkout Flow | true |
| `beta-reports` | Beta Reports Module | false |
| `maintenance-mode` | Maintenance Mode | false |
| `ai-suggestions` | AI Suggestions | true |

## Consequences

**Positive:**

- **Zero setup:** No database server installation, no schema migration scripts to write. Clone, build, and run.
- **Ephemeral data:** Each restart starts fresh — ideal for development and CI pipelines.
- **H2 console:** The built-in web console at `/h2-console` lets developers inspect data directly during debugging.
- **JPA abstraction:** The entire data access layer is written against JPA interfaces (`JpaRepository`). The SQL dialect is abstracted by Hibernate, so swapping databases requires only a properties change.
- **Fast test execution:** In-memory databases make integration tests run quickly.

**Negative:**

- **Data loss on restart:** All data is lost when the application stops. Not suitable for production.
- **No persistence:** Cannot share data across restarts or instances.
- **H2 SQL dialect differences:** H2 is not 100% compatible with PostgreSQL/MySQL. Some advanced queries or functions may differ.

**Migration path to PostgreSQL:**

1. Add `org.postgresql:postgresql` dependency to `pom.xml`.
2. Replace `application.properties` entries:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/theroot
   spring.datasource.driverClassName=org.postgresql.Driver
   spring.datasource.username=theroot_user
   spring.datasource.password=${DB_PASSWORD}
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=validate   # use Flyway/Liquibase for production
   spring.h2.console.enabled=false
   ```
3. Replace the seeder with proper database migration scripts (Flyway or Liquibase).
4. Remove the H2 dependency and console configuration.

The code in `FeatureFlagRepository`, `FeatureFlagController`, and the entity class requires **zero changes** because they depend only on JPA abstractions.
