<a id="readme-top"></a>

<div align="center">

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![GPL-3.0 License][license-shield]][license-url]

</div>

<br />
<div align="center">
  <h3 align="center">Selves</h3>
  <p align="center">
    一款为多意识体系统设计的交流与协作 Android 应用
    <br />
    <a href="https://github.com/moumoum0/SElves"><strong>查看文档 »</strong></a>
    <br />
    <br />
    <a href="https://github.com/moumoum0/SElves/issues/new?labels=bug">报告 Bug</a>
    &nbsp;·&nbsp;
    <a href="https://github.com/moumoum0/SElves/issues/new?labels=enhancement">请求新功能</a>
  </p>
</div>

---

## 目录

- [关于项目](#关于项目)
- [主要功能](#主要功能)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
  - [前提条件](#前提条件)
  - [安装与运行](#安装与运行)
- [项目结构](#项目结构)
- [路线图](#路线图)
- [贡献](#贡献)
- [许可证](#许可证)
- [联系方式](#联系方式)

---

## 关于项目

Selves 是一款专为多意识体（系统）设计的 Android 应用，帮助系统成员进行内部交流、任务管理与集体决策。

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 主要功能

- **交流**：系统成员之间的消息沟通
- **待办**：共享任务列表与进度追踪
- **投票**：集体决策投票功能

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 技术栈

[![Kotlin][Kotlin-shield]][Kotlin-url]
[![Android][Android-shield]][Android-url]
[![Jetpack Compose][Compose-shield]][Compose-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 快速开始

### 前提条件

- [Android Studio](https://developer.android.com/studio) (最新稳定版)
- JDK 11+
- Android SDK (API 26+)

### 安装与运行

1. 克隆仓库
   ```sh
   git clone https://github.com/moumoum0/SElves.git
   ```
2. 使用 Android Studio 打开项目
3. 编译项目
   ```sh
   ./gradlew build
   ```
4. 安装到已连接设备
   ```sh
   ./gradlew installDebug
   ```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 项目结构

```
app/
└── src/main/java/com/selves/xnn/
    ├── ui/
    │   ├── screens/       # 各页面实现
    │   ├── components/    # 可复用 UI 组件
    │   └── theme/         # 应用主题与样式
    ├── navigation/        # 页面导航
    ├── model/             # 数据模型
    └── viewmodel/         # ViewModel 层
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 路线图

- [x] 交流功能
- [x] 待办功能
- [ ] 投票功能
- [ ] 多语言支持

查看 [open issues](https://github.com/moumoum0/SElves/issues) 了解已提出的功能与已知问题。

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 贡献

欢迎任何贡献！如果你有改进建议，请 fork 本仓库并创建 Pull Request，也可以直接提交 issue。

1. Fork 本项目
2. 创建你的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 许可证

本项目基于 GPL-3.0 许可证发布，详见 [`LICENSE`](LICENSE)。

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 联系方式

项目链接：[https://github.com/moumoum0/SElves](https://github.com/moumoum0/SElves)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<!-- MARKDOWN LINKS & BADGES -->
[contributors-shield]: https://img.shields.io/github/contributors/moumoum0/SElves.svg?style=for-the-badge
[contributors-url]: https://github.com/moumoum0/SElves/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/moumoum0/SElves.svg?style=for-the-badge
[forks-url]: https://github.com/moumoum0/SElves/network/members
[stars-shield]: https://img.shields.io/github/stars/moumoum0/SElves.svg?style=for-the-badge
[stars-url]: https://github.com/moumoum0/SElves/stargazers
[issues-shield]: https://img.shields.io/github/issues/moumoum0/SElves.svg?style=for-the-badge
[issues-url]: https://github.com/moumoum0/SElves/issues
[license-shield]: https://img.shields.io/github/license/moumoum0/SElves.svg?style=for-the-badge
[license-url]: https://github.com/moumoum0/SElves/blob/master/LICENSE
[Kotlin-shield]: https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white
[Kotlin-url]: https://kotlinlang.org/
[Android-shield]: https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white
[Android-url]: https://developer.android.com/
[Compose-shield]: https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white
[Compose-url]: https://developer.android.com/jetpack/compose