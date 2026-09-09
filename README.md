# Stepway backend deployment

Spring Boot 2.7.16, Java 8, Maven, MySQL. The Dockerfile builds and tests the JAR; Railway uses it automatically. This is a demo deployment baseline, not a complete production security review.

## Railway + Render: first deployment

1. Push the backend and frontend commits to their GitHub repositories.
2. Create a Railway project and add a MySQL service with persistent storage. Name it `MySQL`.
3. Add this backend repository as another service in the same project/environment.
4. Configure the backend variables:

| Variable | Value |
| --- | --- |
| SPRING_DATASOURCE_URL | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}` |
| SPRING_DATASOURCE_USERNAME | `${{MySQL.MYSQLUSER}}` |
| SPRING_DATASOURCE_PASSWORD | `${{MySQL.MYSQLPASSWORD}}` |
| JWT_SECRET | Base64-encoded random key of at least 32 bytes; see below |
| DEMO_ADMIN_EMAIL | Your demo administrator email |
| DEMO_ADMIN_PASSWORD | Unique password, at least 12 characters |
| CORS_ALLOWED_ORIGINS | Frontend origin, e.g. `https://stepway-frontend.onrender.com` |
| PORT | `8080` |

If the database service has a different name, change the variable references accordingly. Use Railway's private database host, not localhost. These are backend variables, never frontend variables.

5. Deploy and generate the backend's public domain with target port 8080. `railway.toml` configures `/health` as a database-aware deployment check.
6. Confirm `https://YOUR-BACKEND/health` returns `{"status":"UP"}`. Swagger is at `/swagger-ui/index.html`.
7. In Render create a Blueprint from the frontend's `render.yaml`, or a Static Site with build command `node scripts/build.mjs` and publish directory `public`. Set its `STEPWAY_API_BASE_URL` to the backend HTTPS origin, without `/api`.
8. Once the frontend URL is known, set backend `CORS_ALLOWED_ORIGINS` to that exact origin (no trailing slash), and redeploy. Multiple origins are comma-separated.
9. Open the frontend login page and use the bootstrap administrator credentials. Create teachers/students from the admin UI, add courses and notices, and rehearse student enrollment.

Roles are created automatically after Hibernate initializes the schema. The optional administrator is created only when the configured email is absent. Restarts do not reset its password or promote an existing account. Remove **both** bootstrap variables after the first successful setup if desired; changing these variables is not a password-reset mechanism.

The historic Liquibase sample data is not enabled: it doesn't match today's user/role schema. No manual SQL seed step is needed. Hibernate `ddl-auto=update` is retained for a persistent demo database; do not use `create-drop`.

## Generate the JWT key (PowerShell)

```powershell
$jwtBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($jwtBytes)
[Convert]::ToBase64String($jwtBytes)
$rng.Dispose()
```

Paste the result into Railway's JWT_SECRET variable. Keep it stable across restarts; changing it invalidates existing tokens. Weak or missing keys fail startup. Previous source-code credentials have been removed; use fresh credentials.

## Local demo with Docker Desktop

1. Start Docker Desktop using Linux containers.
2. Copy `.env.example` to `.env`.
3. Replace database passwords, JWT_SECRET, and bootstrap admin values.
4. Run:

```powershell
docker compose up --build -d
docker compose logs -f backend
```

The database stays in a named volume and has no published host port. Open http://localhost:8080/health. In the frontend repo run `python -m http.server 5500` and open http://localhost:5500. Stop with `docker compose down`; do not add `-v` unless you intend to erase demo data.

## Local Java build

Install JDK 8 and run `.\mvnw.cmd -B clean verify` (Windows) or `bash mvnw -B clean verify` (Linux). Maven fetches dependencies automatically. For a direct application launch set the variables from `.env.example` in your shell; Spring Boot does not automatically load a root `.env` file.

Unit/web security tests run without MySQL. CI additionally provisions MySQL 8 and enables `RUN_DATABASE_TESTS=true` to test fresh schema startup, repeatable bootstrap, login, health, and native SQL queries. Use only a disposable database for these tests.

## Troubleshooting

- Failed build: read Maven/test output; the Docker build intentionally does not skip tests.
- Startup database connection failure: verify JDBC URL and referenced Railway variables.
- MySQL authentication reports public-key retrieval disallowed: on the private demo network append `?allowPublicKeyRetrieval=true` to the JDBC URL. For an externally hosted database, use its required TLS configuration.
- CORS failure: compare the browser Origin with CORS_ALLOWED_ORIGINS and redeploy the backend.
- Invalid login: verify roles and the bootstrap email; existing accounts are not overwritten. Clear browser local storage after rotating JWT_SECRET.
- HTTP 403: only admins can assign privileged roles and perform administrative writes; students enroll via `POST /api/available-enrollment`.
- Empty dashboards: add demo records first. Some inherited template pages have no complete backend implementation.

The stack retains the original framework versions. Use synthetic data and review object-level authorization and dependencies before production use. No cloud service is provisioned by committing these files; the hosting steps and a successful live rehearsal are still required.

References: https://docs.railway.com/guides/spring-boot · https://docs.railway.com/databases/mysql · https://render.com/docs/static-sites
