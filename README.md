# selves安卓端

这是一个多意识体交流软件。

## 主要功能

- **交流**: 
- **待办**: 
- **投票**: 

## 项目结构

- `app/`: 应用源代码
  - `src/main/java/com/example/chatapp/`: 应用主要代码
    - `ui/`: 用户界面组件
      - `screens/`: 各个页面实现
      - `components/`: 可复用UI组件
      - `theme/`: 应用主题与样式
    - `navigation/`: 页面导航相关代码
- `gradle/`: Gradle包装器文件
- `build.gradle.kts`: 项目级构建脚本
- `settings.gradle.kts`: 项目设置文件
- `gradlew`和`gradlew.bat`: Gradle包装器执行脚本

## 构建与运行

### 前提条件

- Android Studio
- JDK 11+
- Android SDK

### 编译与运行

```bash
# 使用Gradle包装器编译
./gradlew build

# 安装到连接的设备
./gradlew installDebug
```

## 许可证

[待添加] 