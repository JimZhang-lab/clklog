# AGENTS.md

## Repository layout

This repository is a monorepo. Application modules are ordinary directories
tracked directly by the root Git repository:

- `ui`: Vue 2 frontend.
- `api`: ClickHouse analytics API.
- `manage`: MySQL-backed authentication, configuration, and metadata API.
- `init`: MySQL/ClickHouse initialization and upgrade scripts.
- `receiver`: event receiver.
- `processing`: Flink processing job.
- `deploy/local`: local integration seed and bootstrap assets.

Do not add `.gitmodules`, gitlinks, nested `.git` directories, or independent
module repositories. Run Git branch, pull, add, commit, and status commands
from the repository root so all modules remain on one branch and in one
history.

## Development rules

- Use conventional branch prefixes based on the change type:
  `feature/`, `fix/`, `chore/`, `docs/`, `refactor/`, or `test/`.
- Do not use tool- or assistant-specific branch prefixes.
- Use Yarn for frontend development on the local Mac.
- GitHub Actions may use `npm ci` with the committed `ui/package-lock.json`.
- Keep Java source compatible with Java 8.
- Follow existing Spring, JPA, Vue 2, Element UI, and ECharts conventions.
- Prefer focused changes and do not reformat unrelated legacy files.
- Preserve unrelated working-tree changes anywhere in the monorepo.
- Use MySQL for identity, configuration, permissions, funnels, tags, CDP
  assets, bookmarks, and API keys.
- Use ClickHouse for event, visitor, funnel result, and aggregate analytics.
- Every user or metadata operation must include `projectName`.
- Every schema change must update fresh-install SQL and an idempotent upgrade
  SQL.
- Do not commit `.DS_Store`, build output, logs, IDE files, local volumes, or
  credentials.

## Commercial feature contracts

- Funnel definitions are persisted in MySQL and calculated from ClickHouse
  events.
- User drill-down must filter every detail query by both `projectName` and
  `distinctId`.
- Tag categories, tag definitions, assignments, groups, and portraits are
  project-scoped MySQL data.
- API key endpoints live under `/apikey/*`; store only prefix, mask, and hash.
  Return the full key only once from `/apikey/add`.
- Custom SQL lives at `/customsql/query`; require `projectName`, allow only
  read-only statements, reject multiple statements, and cap output at 1000
  rows.
- `/#/mobileSummary/index` is a standalone responsive view and must not inherit
  the desktop sidebar.
- Do not reintroduce links from local routes to `pro.clklog.com` for features
  implemented in this repository.

## Local integration environment

Start dependencies:

```bash
docker compose -f compose.dependencies.yml up -d
docker compose -f compose.dependencies.yml ps
```

Start backend services in separate terminals:

```bash
cd manage
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

```bash
cd api
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Start the frontend:

```bash
cd ui
NODE_OPTIONS=--openssl-legacy-provider yarn dev --host 127.0.0.1
```

Local endpoints:

- Web: `http://127.0.0.1:9527`
- Manage API: `http://127.0.0.1:8080`
- Analytics API: `http://127.0.0.1:8081`
- MySQL: `127.0.0.1:13306`
- ClickHouse HTTP: `127.0.0.1:18123`
- ClickHouse Native: `127.0.0.1:19000`
- Redis: `127.0.0.1:16379`

Local login: `clklog` / `clklog`.

## Database changes

Fresh installs use:

- `init/scripts_init/mysql/mysql_clklog.sql`
- `init/scripts/init.sql`
- `deploy/local/mysql/seed.sql`
- `deploy/local/clickhouse/seed.sql`

Existing MySQL environments use:

```bash
docker exec -i clklog-local-mysql \
  mysql --default-character-set=utf8mb4 -uroot -p123456 clklog \
  < init/scripts_init/mysql/upgrade_analytics_features.sql
```

Keep table and column names aligned with Hibernate naming. Local seeds should
be deterministic and remain scoped to the `clklogapp` project.

## Verification

Use lightweight checks while iterating:

```bash
cd api && mvn -q -DskipTests compile
cd manage && mvn -q -DskipTests compile
cd ui && yarn eslint <changed-files>
```

The full frontend repository has legacy lint debt, so report the baseline
separately and do not mass-format unrelated files.

Before handing off frontend work:

- Confirm the Yarn development build compiles.
- Check each affected route in the browser.
- Test mobile-only views at a narrow viewport and confirm no horizontal
  overflow or desktop navigation overlay.
- Inspect browser console errors for the affected route.

For cross-service changes, verify at least one real request through UI,
Manage, MySQL, API, and ClickHouse. For API keys, exercise create, edit, list,
and delete. For custom SQL, verify both a successful read query and a rejected
write statement.

Root CI is defined in `.github/workflows/ci.yml`. It uses Node 16 and npm for
the frontend because the legacy dependency tree is not compatible with Node
18. Keep module build commands there instead of adding nested
`.github/workflows` directories.

## Documentation

Keep these files synchronized with behavioral changes:

- `README.md`: developer setup, architecture, routes, and operations.
- `docs/commercial-feature-parity.md`: official demo comparison and local
  implementation status.
- `AGENTS.md`: canonical repository instructions.
- `CLAUDE.md`: short assistant checklist that points back to this file.
