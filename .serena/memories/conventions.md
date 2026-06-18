# Conventions

- Android architecture is local-first MVVM-style: Room DAO/entity/repository layers feed Hilt ViewModels and Compose screens/components.
- Dependency injection is Hilt-centered; add Android/service entry points via `@AndroidEntryPoint`, app via `@HiltAndroidApp`, ViewModels via `@HiltViewModel`.
- Compose UI is split by screens/components/theme; preserve Material3/Compose patterns and avoid XML UI additions unless integrating platform-only resources.
- Data domains are mirrored across `data/dao`, `data/entity`, `data/repository`, `model`, and feature ViewModels; feature changes usually need all affected layers checked.
- Services are real app runtime surfaces, not just helpers: `LocationTrackingService`, `WebServerService`, `WebSocketManager`, `AutoRestartReceiver`.
- App includes an embedded Ktor web access path; Android data/API changes may affect `web/src/lib/api.ts` and web pages.
- Existing project uses Chinese UI/docs/comments in places; keep user-facing text multilingual-aware (`values`, `values-zh`, `values-en`).
- Release signing is local-properties based; never move signing secrets into tracked files.
- Gradle lint disables `MutableCollectionMutableState` and `AutoboxingStateCreation`; do not assume those diagnostics are currently enforced.