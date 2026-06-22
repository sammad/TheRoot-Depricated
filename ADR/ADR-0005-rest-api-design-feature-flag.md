# ADR-0005: REST API Design for Feature Flag Management

**Status:** Accepted

## Context

TheRoot exposes a REST API for managing feature flags. The frontend dashboard and any future API consumers need a consistent, predictable interface for CRUD operations plus a dedicated toggle action. The key design decisions include:

- URL structure and resource naming.
- HTTP methods for each operation.
- How to model the toggle action (update vs dedicated endpoint).
- Request/response shapes.
- Error handling conventions.

## Decision

We will follow **RESTful conventions** with a single resource (`FeatureFlag`) exposed under the `/api/features` base path.

### Base URL

All endpoints are prefixed with `/api/features` as defined in the controller class-level `@RequestMapping("/api/features")`.

### Endpoints

| Method | Path | Description | Status Codes |
|--------|------|-------------|--------------|
| `GET` | `/api/features` | List all feature flags | 200 |
| `GET` | `/api/features/{id}` | Get a single flag by ID | 200, 404 |
| `POST` | `/api/features` | Create a new flag | 201, 400 |
| `PUT` | `/api/features/{id}` | Full update of an existing flag | 200, 404 |
| `PATCH` | `/api/features/{id}/toggle` | Toggle the `enabled` boolean (dedicated action) | 200, 404 |
| `DELETE` | `/api/features/{id}` | Delete a flag | 204, 404 |

### Request / Response Shapes

**Feature Flag entity (JSON representation):**

```json
{
  "id": 1,
  "key": "dark-mode",
  "name": "Dark Mode",
  "description": "Enable dark mode theme across the application",
  "enabled": false,
  "createdAt": "2025-06-23T10:30:00"
}
```

**POST /api/features** — request body (id and createdAt are auto-generated):
```json
{
  "key": "new-feature",
  "name": "New Feature",
  "description": "Description here",
  "enabled": false
}
```

**POST /api/features** — duplicate key:
- Response: `400 Bad Request` (empty body; checked via `repository.existsByKey()`).

### Toggle Pattern

The toggle action is modeled as a **sub-resource action** using `PATCH`:

```
PATCH /api/features/{id}/toggle
```

This is intentionally **not** a `PUT` on the flag itself because:

- Toggle is a specific, idempotent-in-intent action (flip the boolean).
- It conveys clear intent in the URL (the verb "toggle").
- It avoids requiring the client to send the entire flag object just to flip one field.
- The response returns the full updated flag so the client can update its state.

### Error Handling

| Scenario | HTTP Status | Behavior |
|----------|-------------|----------|
| Resource not found | `404 Not Found` | Empty body |
| Duplicate key on create | `400 Bad Request` | Empty body (by design; frontend shows "Key already exists") |
| Validation failure (e.g., blank key/name) | `400 Bad Request` | Spring Boot default error JSON |
| Service error | `500 Internal Server Error` | Default error response |

### Implementation Details (from source)

- **Controller:** `FeatureFlagController` is a `@RestController` with constructor injection of `FeatureFlagRepository`.
- **Cross-origin:** `@CrossOrigin(origins = "http://localhost:5173")` on the controller, supplemented by global `CorsConfig`.
- **Validation:** `@Valid` on request bodies triggers `@NotBlank` constraints on `key` and `name` fields.
- **Update behavior:** `PUT` overwrites all fields (key, name, description, enabled) on the existing entity; `id` and `createdAt` are preserved.

## Consequences

**Positive:**

- **Consistency:** Standard RESTful patterns make the API easy to learn and consume.
- **Explicit toggle:** The dedicated `PATCH .../toggle` endpoint makes the most common operation (flip on/off) a single call without fetching the full entity first.
- **Loose coupling:** The frontend only needs to know the endpoint shapes; it never accesses the database directly.
- **Validation at boundary:** `@Valid` ensures malformed data is rejected before reaching the repository layer.

**Negative:**

- **No pagination/sorting:** `GET /api/features` returns all flags. For a small number of flags this is acceptable; if the count grows significantly, pagination (`?page=&size=`) should be added.
- **No search/filter:** There is no endpoint to search by key or name. Could be added as query parameters on `GET /api/features`.
- **400 on duplicate key is not descriptive:** The empty body on duplicate key forces the frontend to hard-code the message "Key already exists". A more RESTful approach would return a JSON error body with a message field.
- **No DTO layer:** The entity (`FeatureFlag`) is exposed directly as the API representation. This couples the API contract to the JPA entity; using separate DTOs would decouple them.
