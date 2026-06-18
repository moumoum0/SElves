# Core

- Project root: `D:\\project\\selves`; Android app plus separate web mirror/prototype.
- Android module: `app`; package namespace/applicationId `com.selves.xnn`.
- Main Android source root: `app/src/main/java/com/selves/xnn`.
- Major Android areas:
  - `data/dao`, `data/entity`, `data/repository`: Room-backed local persistence per feature domain.
  - `data/di`: Hilt data/database dependency graph.
  - `model`: app/domain models used by UI and services.
  - `ui/screens`, `ui/components`, `ui/theme`, `ui/viewmodels`: Jetpack Compose UI layer.
  - `viewmodel`: feature-level Hilt ViewModels outside `ui/viewmodels`.
  - `service`: Android/Ktor services: location tracking, embedded web server, WebSocket.
  - `navigation`: bottom navigation model.
- App entry symbols: `ChatApplication` (`@HiltAndroidApp`), `MainActivity` (`@AndroidEntryPoint`).
- Web module: `web`; React/Vite implementation with pages under `web/src/pages`, API glue under `web/src/lib`.
- Feature surface from README: group chat, todos, dynamics, voting, location records, online status, member management, settings, onboarding/import, local-first privacy.
- Read `mem:tech_stack` for versions/tooling, `mem:conventions` for architecture/style, `mem:suggested_commands` for practical commands, and `mem:task_completion` before closing implementation work.