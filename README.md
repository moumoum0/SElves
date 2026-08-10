<a id="readme-top"></a>

<div align="center">
  <h1>Selves</h1>

  <!-- <img src="./img/app_icon.png" width="128" height="128" /> -->

  <b>简体中文</b> | <a href="./README_en.md">English</a>

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
    一款为多意识体系统设计的交流与协作应用。Android 端内嵌 Web 服务器，同一网络下的任意设备均可通过浏览器访问 Web 端，实现跨设备协作。
  </p>
</div>

---

## 功能特性

- **群聊**：系统成员之间的实时消息沟通，支持文本与图片，多群组管理
- **待办**：共享任务列表，支持优先级与完成状态追踪
- **动态**：发布与查看成员动态，支持图片、点赞与评论
- **投票**：发起集体决策投票，支持多选与匿名，查看投票详情与结果
- **日记**：成员个人日记
- **位置记录**：追踪与记录系统成员的位置信息
- **在线状态**：查看各成员的在线记录与时长统计
- **成员管理**：创建、切换、管理系统成员，支持成员分组
- **Web 端访问**：内嵌 Web 服务器（REST API + WebSocket，Token 认证），在设置中开启后，同一网络下的设备可通过浏览器访问
- **系统设置**：主题模式（深色/浅色/跟随系统）、多语言（简体中文/English）、首页模块个性化配置
- **首次引导**：全新的欢迎引导流程，支持备份导入
- **数据本地化**：所有数据存储在本地，保护隐私

## 截图

<!-- 待添加应用截图 -->
<!-- 
|                        主界面                        |                       群聊界面                            |
|:----------------------------------------------------:|:-------------------------------------------------------:|
| <img src="img/screenshot_main.png" width="300" /> | <img src="img/screenshot_chat.png" width="300" /> |
-->

## 快速开始

### 前提条件

- [Android Studio](https://developer.android.com/studio) (最新稳定版)
- JDK 17+
- Android SDK (API 26+)
- [Node.js](https://nodejs.org/) 与 npm（构建时会自动编译 Web 前端并打包进 APK）

### 安装与运行

1. 克隆仓库
   ```sh
   git clone https://github.com/moumoum0/SElves.git
   ```
2. 安装 Web 前端依赖
   ```sh
   cd web && npm install
   ```
3. 使用 Android Studio 打开项目，编译并安装到设备（Gradle 会自动构建 Web 前端）
   ```sh
   ./gradlew installDebug
   ```

### Web 端开发（可选）

```sh
cd web
npm run dev   # 启动 Vite 开发服务器（端口 5173）
```

Web 端正式版本由 Android 应用内嵌的 Web 服务器（端口 8080）提供，在应用设置中开启后，同一网络下的设备可通过浏览器访问。

## 路线图

- [x] 交流功能
- [x] 待办功能
- [x] 投票功能
- [x] 多语言支持（English）
- [x] Web 端（内嵌 Web 服务器，跨设备浏览器访问）
- [ ] 独立服务端部署
- [ ] 数据云同步

查看 [Issues](https://github.com/moumoum0/SElves/issues) 了解已提出的功能与已知问题。

## 贡献

欢迎任何形式的贡献！无论是报告 Bug、建议新功能、帮助翻译还是贡献代码，请查看我们的贡献指南开始参与。

1. Fork 本项目
2. 创建你的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 贡献者

<a href="https://github.com/moumoum0/SElves/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=moumoum0/SElves&max=100&columns=8" />
</a>

Made with [contrib.rocks](https://contrib.rocks).

## 许可证

本项目基于 GPL-3.0 许可证发布，详见 [`LICENSE`](LICENSE)。

## Star History

<a href="https://www.star-history.com/#moumoum0/SElves&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=moumoum0/SElves&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=moumoum0/SElves&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=moumoum0/SElves&type=date&legend=top-left" />
 </picture>
</a>