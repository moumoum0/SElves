# Task Completion

- For Android code changes, run the narrowest practical check first:
  - `./gradlew.bat assembleDebug`
  - `./gradlew.bat testDebugUnitTest` for logic/ViewModel/repository changes with tests.
  - `./gradlew.bat lintDebug` when UI/resources/manifest/build config changed.
- For web changes under `web`, run:
  - `cd web; npm run build`
- For Room schema/entity/DAO changes, verify KSP/Room generation through `./gradlew.bat assembleDebug`; check migration/schema implications before claiming completion.
- For services/location/web server behavior, static build is insufficient; mention need for device/emulator smoke test if not executed.
- Before final response after edits: check lints/diagnostics on edited files when available; do not fix unrelated existing diagnostics unless they block the task.