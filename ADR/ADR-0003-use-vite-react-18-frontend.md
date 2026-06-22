# ADR-0003: Use Vite + React 18 for Frontend

**Status:** Accepted

## Context

TheRoot needs a web-based user interface to display, create, toggle, and delete feature flags. The frontend communicates with the backend REST API and must provide a responsive, modern single-page application (SPA) experience. The team needed to decide on a build tool and UI library.

Key considerations:

- Fast development server with hot module replacement (HMR).
- Simple build configuration.
- Modern React with hooks-based components.
- Low bundle size for production.

## Decision

We will use **Vite 5** as the build tool and **React 18** as the UI library.

**Specific choices:**

| Concern | Decision | Rationale |
|---------|----------|-----------|
| Build tool | Vite 5.3.1 | Native ES module dev server, sub-second HMR, optimized production builds via Rollup. |
| UI library | React 18.3.1 | Mature ecosystem, hooks, `createRoot` API, concurrent features. |
| Plugin | `@vitejs/plugin-react` 4.3.0 | Official Vite plugin; enables JSX transform and React Refresh for HMR. |
| Language | JavaScript (JSX) | Matches team familiarity; TypeScript can be added later. |
| Entry point | `src/main.jsx` with `ReactDOM.createRoot` | React 18's new root API enables concurrent rendering. |

**Frontend file structure:**

```
frontend/src/
├── main.jsx                              — ReactDOM.createRoot entry
├── App.jsx                               — Root component
├── index.css                             — Global reset + body styles
├── components/
│   ├── FeatureFlagDashboard.jsx          — Main dashboard with table, toasts, state
│   ├── FlagRow.jsx                       — Single table row with toggle checkbox
│   ├── AddFlagModal.jsx                  — Modal form for creating a flag
│   └── Dashboard.css                     — All component styles (CSS, not modules)
```

**Why Vite over Create React App (CRA):**

| Factor | Vite | CRA |
|--------|------|-----|
| Dev server startup | ~300ms (cold) | ~5-10s (cold) |
| HMR | Instant (ESM-based) | Slower (bundle-based) |
| Build tool | Rollup (fast, tree-shaken) | Webpack (heavier) |
| Maintenance | Active development, Vite 5 current | CRA is in maintenance mode; no major updates expected |
| Configuration | Explicit `vite.config.js` | Abstracted `react-scripts`, harder to customize |

## Consequences

**Positive:**

- **Fast feedback loop:** Vite's ESM-based dev server starts instantly and HMR updates are near-instant, improving developer productivity.
- **React 18 concurrent features:** Access to automatic batching, `startTransition`, and Suspense improvements.
- **No configuration overhead:** Vite's default config works out of the box; `vite.config.js` for the proxy is minimal.
- **Small production bundles:** Rollup's tree-shaking produces optimized output.

**Negative:**

- **CJS/ESM interoperability:** Some older Node packages may have CJS/ESM issues, but the dependencies used (React, ReactDOM) are fully ESM-compatible.
- **No built-in TypeScript:** The project uses JSX; adding TypeScript requires a config change and renaming files to `.tsx`. Not a blocker.
- **Vite-specific proxy config:** The proxy setup in `vite.config.js` is Vite-specific; switching build tools would require reconfiguring the proxy.
