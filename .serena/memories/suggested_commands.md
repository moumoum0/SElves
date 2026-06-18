# Suggested Commands

- Android build/debug install from project root on Windows PowerShell:
  - `./gradlew.bat assembleDebug`
  - `./gradlew.bat installDebug`
  - `./gradlew.bat testDebugUnitTest`
  - `./gradlew.bat connectedDebugAndroidTest` when a device/emulator is attached.
  - `./gradlew.bat lintDebug`
- Release build requires valid signing values in `local.properties`:
  - `./gradlew.bat assembleRelease`
- Web module:
  - `cd web; npm install`
  - `cd web; npm run dev`
  - `cd web; npm run build`
  - `cd web; npm run preview`
- Useful Windows shell forms:
  - Prefer `./gradlew.bat` over `./gradlew` in PowerShell.
  - Use quoted paths for directories with spaces.
  - Use Cursor/native search tools first; if using shell, prefer `rg` over `grep`.