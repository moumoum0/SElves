<a id="readme-top"></a>

<div align="center">
  <h1>Selves</h1>

  <!-- <img src="./img/app_icon.png" width="128" height="128" /> -->

  <a href="./README.md">简体中文</a> | <b>English</b>

  <a href="https://github.com/moumoum0/SElves/blob/master/LICENSE">
    <img alt="LICENSE" src="https://img.shields.io/badge/license-GPLv3-green"></a>
  <a href="https://github.com/moumoum0/SElves/stargazers">
    <img alt="Github Stars" src="https://img.shields.io/github/stars/moumoum0/SElves?style=flat&logo=github&logoColor=white"></a>
  <a href="https://github.com/moumoum0/SElves/releases/latest">
    <img alt="GitHub Release" src="https://img.shields.io/github/v/release/moumoum0/SElves?logo=github"></a>
  <a href="https://github.com/moumoum0/SElves/issues">
    <img alt="GitHub Issues" src="https://img.shields.io/github/issues/moumoum0/SElves?logo=github"></a>

  <h6>Built with</h6>

  <img alt="Kotlin" src="https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white">
  <img alt="React" src="https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB">
  <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white">

  <h6>Supported Platform</h6>

  <img alt="Android" src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img alt="Web" src="https://img.shields.io/badge/Web-5A0FC8?style=for-the-badge&logo=googlechrome&logoColor=white" />

  <p>
    An app designed for communication and collaboration within plural systems. The Android app embeds a web server, so any device on the same network can access the web frontend from a browser for cross-device collaboration.
  </p>
</div>

---

## Features

- **Group Chat**: Real-time messaging between system members, with text, images, and multi-group support
- **Todo**: Shared task lists with priorities and completion tracking
- **Dynamics**: Post and view member updates, with images, likes, and comments
- **Voting**: Create collective decision polls with multi-choice and anonymous options, view vote details and results
- **Diary**: Personal diaries for each member
- **Location Tracking**: Track and record system members' location information
- **Online Stats**: View each member's online history and duration statistics
- **Member Management**: Create, switch, and manage system members, with member groups
- **Web Access**: Embedded web server (REST API + WebSocket, token auth) — enable it in Settings and any device on the same network can connect from a browser
- **Settings**: Theme modes (dark/light/system), multi-language (English / 简体中文), customizable home screen modules
- **Welcome Guide**: First-launch onboarding flow with backup import support
- **Local Storage**: All data stored locally to protect privacy

## Screenshots

<!-- Screenshots coming soon -->
<!-- 
|                        Home                          |                       Group Chat                          |
|:----------------------------------------------------:|:-------------------------------------------------------:|
| <img src="img/screenshot_main.png" width="300" /> | <img src="img/screenshot_chat.png" width="300" /> |
-->

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable)
- JDK 17+
- Android SDK (API 26+)
- [Node.js](https://nodejs.org/) and npm (the web frontend is built automatically and bundled into the APK)

### Installation

1. Clone the repository
   ```sh
   git clone https://github.com/moumoum0/SElves.git
   ```
2. Install web frontend dependencies
   ```sh
   cd web && npm install
   ```
3. Open the project in Android Studio, then build and install to your device (Gradle builds the web frontend automatically)
   ```sh
   ./gradlew installDebug
   ```

### Web Development (optional)

```sh
cd web
npm run dev   # start the Vite dev server (port 5173)
```

In production, the web frontend is served by the web server embedded in the Android app (port 8080). Enable it in the app's Settings, and any device on the same network can access it from a browser.

## Roadmap

- [x] Group chat
- [x] Todo list
- [x] Dynamics feed
- [x] Voting system
- [x] Diary
- [x] Location tracking
- [x] Online stats
- [x] Member management
- [x] Settings & theming
- [x] English localization
- [x] Web frontend (embedded web server, cross-device browser access)
- [ ] Standalone server deployment
- [ ] Cloud data sync

See [Issues](https://github.com/moumoum0/SElves/issues) for proposed features and known bugs.

## Contributing

Contributions of all kinds are welcome! Whether it's reporting a bug, suggesting a feature, helping with translations, or contributing code — please get involved.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Contributors

<a href="https://github.com/moumoum0/SElves/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=moumoum0/SElves&max=100&columns=8" />
</a>

Made with [contrib.rocks](https://contrib.rocks).

## License

Distributed under the GPL-3.0 License. See [`LICENSE`](LICENSE) for more information.

## Star History

<a href="https://www.star-history.com/#moumoum0/SElves&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=moumoum0/SElves&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=moumoum0/SElves&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=moumoum0/SElves&type=date&legend=top-left" />
 </picture>
</a>
