# ADR-0006: Use Vite Proxy for Frontend-Backend Communication in Development

**Status:** Accepted

## Context

TheRoot has two processes during development:

- **Frontend:** Vite dev server on `http://localhost:5173` (serving React SPA).
- **Backend:** Spring Boot on `http://localhost:8080` (REST API).

The frontend needs to make HTTP requests to `/api/features` endpoints, but these run on different origins. We need a way to route API calls from the frontend's origin (port 5173) to the backend (port 8080) during development.

Two approaches were considered:

1. **CORS-only:** Configure the backend to allow cross-origin requests from `localhost:5173`. The frontend fetches directly from `http://localhost:8080/api/features`.
2. **Vite proxy + CORS:** Configure Vite to proxy `/api` requests to the backend, combined with backend CORS as a safety net.

## Decision

We will use **both** approaches together:

1. **Vite dev proxy** as the primary mechanism during development.
2. **Backend CORS configuration** as a fallback for any non-proxied requests or direct API access.

### Vite Proxy Configuration

In `frontend/vite.config.js`:

```js
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

**How it works:**

- The Vite dev server intercepts any request starting with `/api`.
- It forwards the request to `http://localhost:8080/api/...` (preserving the path).
- The response is relayed back to the browser as if it came from `localhost:5173`.
- Because the browser sees same-origin responses, no CORS preflight is triggered.
- The frontend code uses relative URLs (`/api/features`) instead of absolute URLs with hard-coded ports.

### Backend CORS Configuration

In `backend/src/main/java/com/theroot/featureflags/config/CorsConfig.java`:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
```

Additionally, the controller has `@CrossOrigin(origins = "http://localhost:5173")` as a class-level annotation.

## Consequences

**Positive:**

- **No CORS in development:** The proxy eliminates CORS preflight requests, reducing latency on every API call during development.
- **Relative URLs in frontend code:** The frontend uses `/api/features` (relative), making it easy to switch to a production backend URL later (by changing the proxy target or using environment variables).
- **CORS as safety net:** If someone accesses the backend directly (e.g., via Postman, curl, or a different frontend), CORS configuration prevents unauthorized cross-origin access in browsers.
- **Zero config change for production:** In production, a reverse proxy (nginx, cloud load balancer) handles the same path-based routing. The frontend code does not change.

**Negative:**

- **Dev-only solution:** The Vite proxy only works during development (`vite dev`). For production, a different routing strategy is needed (reverse proxy, same-origin deployment, or environment-specific API base URLs).
- **Extra complexity:** The proxy adds a hop in the request path, though latency is negligible (localhost to localhost).
- **Potential confusion:** Developers might think CORS alone is sufficient and disable the proxy. Both configurations serve different purposes — proxy for convenience, CORS for security.

**Alternative considered — CORS-only (no proxy):**

- The frontend would use `fetch('http://localhost:8080/api/features')`.
- Every request triggers a CORS preflight `OPTIONS` request, doubling latency.
- Hard-coding the backend URL in frontend code makes it harder to change in the future.
- We rejected this as the sole approach because the proxy provides a cleaner developer experience and production-like URL routing.
