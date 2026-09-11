# Stepway: from course administration to practical learning

## Purpose and scope

The original final-year report describes an academia-to-industry learning platform. It specifically discusses progress tracking, applied learning, course enrollment, tutorials, assessments, and student tasks moving between states. The original implementation primarily supplied CRUD APIs and template-based administrative pages.

This iteration adds a personal learning-goal workflow and redesigns the primary user journey. It does not claim to implement every concept in the report, verify employability, or improve admissions outcomes. In an application or interview, describe your own contributions and acknowledge the original team project.

## Report-to-implementation mapping

| Report requirement | Implementation in this iteration | Boundary |
| --- | --- | --- |
| Progress tracking and practical learning | Personal goal board with planned, in-progress and completed states | Completion is self-reported, not a credential |
| Tasks and scheduling | Optional target dates and overdue indicators | No reminder service or calendar integration |
| Career readiness | Skill focus and evidence links to a repository, project or write-up | External evidence is not fetched or verified |
| Courses and enrollment | Searchable course cards, pagination and enrollment controls using existing APIs | Existing course and enrollment models retained |
| Tutorials and assessments | Course details expose existing assessment and resource APIs | Resources open as external links; no video hosting |
| Usability and accessibility | Responsive layouts, visible focus, labeled controls, keyboard-friendly dialogs, explicit empty/error states | Not a formal accessibility certification |
| Performance tracking | Saved-goal summary and browser print/PDF export | Not an institution-issued transcript |

## Architecture decisions

1. **Keep the deployment model.** Static HTML/CSS/JavaScript still runs on Render and Spring Boot/MySQL on Railway. No new runtime service or frontend framework is required.
2. **Add an isolated table and API.** `learning_goal` is independent of existing domain tables. Legacy endpoint contracts are preserved. The configured Hibernate `update` creates the new table without replacing existing data.
3. **Derive ownership from authentication.** `/api/me/goals` never accepts an owner from the request. The controller extracts the authenticated user's ID and the repository includes that ID in update/delete lookups. Absent and foreign IDs both return 404.
4. **Separate persistence and API shapes.** Request DTOs validate input; response DTOs do not serialize internal ownership/version fields. A scoped controller advice gives the new feature structured errors without changing legacy responses.
5. **Record completion transitions.** Completing a goal records a timestamp; repeated completion preserves it; reopening clears it. JPA optimistic locking detects overlapping database updates. This does not implement browser-side ETag preconditions for stale forms.
6. **Render user content as text.** New UI code constructs DOM nodes using `textContent`. Evidence links accept only HTTP(S), reject embedded credentials, and use `noopener noreferrer`. The backend independently validates the link.
7. **Keep results honest.** Metrics come from API records. A failed API call displays an unavailable state rather than invented data. The landing-page journey is an illustration, not user statistics.

## Data and endpoints

`LearningGoal`: ID, owner ID, title, description, skill, evidence URL, due date, status, creation/update/completion timestamps, optimistic-lock version. Owner ID is indexed and stored as a scalar to avoid changing legacy user-deletion behavior. Orphan cleanup after account deletion is a future maintenance task.

| Method | Path | Behavior |
| --- | --- | --- |
| GET | `/api/me/goals` | Current user's goals, most recently updated first |
| POST | `/api/me/goals` | Validated creation, HTTP 201 |
| PUT | `/api/me/goals/{id}` | Replace editable fields on an owned goal |
| DELETE | `/api/me/goals/{id}` | Delete an owned goal, HTTP 204 |

All endpoints require a valid bearer token, including for administrators. Administration roles do not grant access to another user's personal goals. The goal list is not paginated; large-scale use would need pagination and account quotas.

Example request:

```json
{
  "title": "Build and test a REST API",
  "description": "Document the design and demonstrate negative test cases.",
  "skill": "Software testing",
  "status": "IN_PROGRESS",
  "dueDate": "2026-12-01",
  "evidenceUrl": "https://github.com/example/project"
}
```

## Verification and demo

Run `mvnw.cmd -B clean verify` on Windows or `bash mvnw -B clean verify` on Linux with Java 8. Unit and MVC tests cover ownership, validation, status transitions, URL rejection and authentication. An H2 repository test exercises persistence and owner filtering. The existing optional MySQL smoke test remains enabled by `RUN_DATABASE_TESTS=true` in CI; H2 is not a substitute for MySQL verification.

The frontend check command remains `node scripts/check.mjs`. Browser interaction checks should exercise login, registration, creating/editing/deleting a goal, status transitions, persistence after refresh, enrollment, course resources and mobile layout. Mocked browser responses verify UI behavior, not a live deployment.

Suggested demonstration: explain the report's progress-tracking gap; register a student; create a goal; attach evidence; move it through the board; refresh to demonstrate persistence; print a progress summary; explain how cross-account access is prevented and show its tests. Discuss one limitation candidly rather than presenting a prototype as production-complete.

## Deployment and rollback

Deploy the backend first, then the frontend. Keep the existing JWT, database and CORS variables. No new required variable is introduced. New users see an empty board. Back up MySQL before any deployment with schema changes. Roll back the application code if necessary; leaving the additive `learning_goal` table in place preserves the new records and does not require older code to read it.

## Further work

Institution/teacher-verified submissions, course ownership, mentor feedback, full authorization auditing of legacy endpoints, versioned schema migrations, rate limiting, pagination, dependency upgrades and production observability remain separate work. This iteration deliberately avoids claiming these capabilities.
