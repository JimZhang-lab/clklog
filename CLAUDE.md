# CLAUDE.md

Follow [AGENTS.md](AGENTS.md) as the canonical repository instruction file.

Before editing:

- Confirm the current branch uses a conventional prefix such as `feature/`,
  `fix/`, `chore/`, `docs/`, `refactor/`, or `test/`.
- Do not create tool- or assistant-specific branch prefixes.
- Treat `ui`, `api`, `manage`, `init`, `receiver`, and `processing` as ordinary
  directories in the root monorepo.
- Do not create `.gitmodules`, gitlinks, or nested Git repositories.
- Preserve unrelated working-tree changes across every module directory.

Implementation checklist:

- Use Yarn for Vue 2 frontend work.
- Keep Java source compatible with Java 8.
- Store identity and configuration data in MySQL.
- Store event and aggregate analytics in ClickHouse.
- Include `projectName` in user, metadata, funnel, tag, CDP, bookmark, API key,
  and custom SQL operations.
- Keep fresh-install and upgrade SQL synchronized.
- Store only masked and hashed API keys; reveal a generated key once.
- Keep custom SQL read-only and limited to 1000 rows.
- Keep the mobile summary route independent from the desktop layout.
- Do not replace local capabilities with links to the hosted commercial demo.

Verification checklist:

- Compile `manage` and `api` with Maven using `-DskipTests`.
- Run targeted ESLint through Yarn for changed frontend files.
- Confirm the frontend development build compiles.
- Exercise affected routes in the browser, including a narrow viewport for
  responsive pages.
- For cross-service changes, verify UI, Manage, MySQL, API, Redis, and
  ClickHouse connectivity.

Update `README.md`, `AGENTS.md`, `CLAUDE.md`, and
`docs/commercial-feature-parity.md` when setup, routes, data contracts, or
commercial feature coverage changes.
