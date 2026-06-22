# Architecture Decision Records — TheRoot

This directory contains Architecture Decision Records (ADRs) for **TheRoot**, a feature flag management application.

Each ADR documents a significant architectural choice, including the context, the decision, and its consequences.

## ADR Index

| ID | Title | Status |
|----|-------|--------|
| [ADR-0001](./ADR-0001-record-architecture-decisions.md) | Record Architecture Decisions | Accepted |
| [ADR-0002](./ADR-0002-use-spring-boot-java-17-backend.md) | Use Spring Boot with Java 17 for Backend | Accepted |
| [ADR-0003](./ADR-0003-use-vite-react-18-frontend.md) | Use Vite + React 18 for Frontend | Accepted |
| [ADR-0004](./ADR-0004-use-h2-in-memory-database.md) | Use H2 In-Memory Database for Development | Accepted |
| [ADR-0005](./ADR-0005-rest-api-design-feature-flag.md) | REST API Design for Feature Flag Management | Accepted |
| [ADR-0006](./ADR-0006-use-vite-proxy-frontend-backend.md) | Use Vite Proxy for Frontend-Backend Communication in Development | Accepted |

## Summary

- **ADR-0001** — Establishes the practice of recording architecture decisions using the MADR template.
- **ADR-0002** — Justifies Spring Boot 3.2.5 + Java 17 with a layered architecture (controller, repository, entity, seeder, config).
- **ADR-0003** — Explains choosing Vite 5 over Create React App for faster builds and React 18 for concurrent features.
- **ADR-0004** — Documents the choice of H2 in-memory database for development, trade-offs vs PostgreSQL, and the migration path.
- **ADR-0005** — Defines the RESTful API structure: collection vs resource endpoints, the toggle pattern (`PATCH /{id}/toggle`), and error handling.
- **ADR-0006** — Describes the Vite dev proxy approach for forwarding `/api` requests to the backend on port 8080, supplementing the CORS configuration.
