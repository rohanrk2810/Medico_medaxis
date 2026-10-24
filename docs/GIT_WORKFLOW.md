# Repository workflow

Repository: https://github.com/rohanrk2810/Medico_medaxis

Work in feature branches, with small meaningful commits and regular pushes after relevant checks. Initial application branch: feature/a1-foundation-auth. Subsequent areas use feature/<milestone>-<scope>. Record actual validation and known gaps in each change. Keep main as the reviewed integration branch; do not force-push or merge unreviewed work automatically. Never commit local keys, environment secrets, patient data, build output or dependency directories.

GitHub Actions runs Maven verify (including PostgreSQL Testcontainers tests) and the Angular production build on main/feature pushes and pull requests. Backend test reports are retained for seven days. CI is validation, not deployment. Frontend unit/browser E2E coverage is still pending. Check workflow results before describing a pushed commit as verified in CI.
