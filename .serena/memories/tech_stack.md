# Tech Stack

- Android:
  - Kotlin `2.0.21`; Java/Kotlin target `17`.
  - Android Gradle Plugin `8.12.3`; KSP `2.0.21-1.0.25`.
  - compileSdk/targetSdk `36`; minSdk `26`.
  - Jetpack Compose enabled; Compose BOM `2024.12.01`; Material3.
  - Navigation Compose `2.8.5`.
  - Room `2.6.1` with KSP schema arg; schema location configured in Gradle.
  - Hilt `2.52`; `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel` used across app/services/viewmodels.
  - DataStore preferences `1.1.1`.
  - Coil `2.7.0`; CanHub image cropper `4.3.2`; SplashScreen `1.0.1`; Accompanist system UI `0.36.0`.
  - Ktor `3.0.3` embedded server stack on Android, with Gson serialization, WebSockets, CORS, status pages.
  - Gson `2.11.0`; TinyPinyin `2.0.3`; ZXing `3.5.3`; SLF4J no-op `2.0.9`.
- Gradle repositories: Google, Maven Central, Aliyun public mirror, JitPack.
- Release signing pulls `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` from `local.properties`; do not commit secrets.
- Android packaging excludes many `META-INF`/Kotlin/debug probe resources; release minifies and shrinks resources.
- Web:
  - React `19.1.0`, React DOM `19.1.0`, React Router DOM `7.5.3`.
  - Vite `5.4.10`, TypeScript `5.8.3`, MDUI `2.1.3`.