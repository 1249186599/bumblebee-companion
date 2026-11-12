# 大黄蜂伙伴 · Bumblebee Companion

面向 PICO 空间设备的混合现实伙伴体验：让机器人伙伴出现在身边，通过手柄、手部追踪和空间面板进行互动。

## 主要功能

- **跟随**：按手柄 A 键或点击面板的跟随按钮，切换跟随状态；根据距离慢走或跑步，靠近时停下。
- **比心**：按手柄 X 键或点击比心按钮，播放比心动作。
- **挥手回应**：抬手左右挥动，触发伙伴的招手动作；包含重复触发冷却。
- **触摸反馈**：利用手部追踪检测手指与角色胸前区域的接触。
- **近身避让**：保持与用户的距离，减少角色与用户位置重叠。
- **空间面板**：展示角色状态和互动入口。

## 技术栈

| 项目 | 配置 |
| --- | --- |
| 语言 | Kotlin 2.1.20 |
| 空间框架 | PICO Spatial SDK 6.1.9 |
| UI | PICO Spatial UI / Compose |
| 构建 | Gradle Wrapper 8.13、Android Gradle Plugin 8.13.2 |
| Android | compileSdk / targetSdk / minSdk 35，arm64-v8a |
| 应用 ID | `com.example.bumblebee.pa4453393` |
| 资源 | GLB 动画模型、PNG 图片、USDZ 资源 |
| 测试 | JUnit 4.13.2；Android 仪器测试配置 |

## 安装开发环境

1. 安装 Android Studio、JDK 17 或兼容版本（项目曾使用 JDK 21）、Android SDK Platform 35 和相应构建工具。
2. 克隆仓库并进入项目：

   ```sh
   git clone https://github.com/1249186599/bumblebee-companion.git
   cd bumblebee-companion
   ```

3. 在 Android Studio 中打开根目录，让 IDE 配置本机 Android SDK 路径并同步 Gradle。命令行使用者可在本地 `local.properties` 中配置 `sdk.dir`；该文件不应提交。
4. 首次构建需要联网下载 Gradle、Android 和 PICO Maven 依赖。版本和仓库配置见 `gradle/libs.versions.toml`、`settings.gradle.kts`。

## 测试和构建

macOS / Linux：

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Windows 使用 `gradlew.bat` 替代 `./gradlew`。

调试 APK 输出到 `app/build/outputs/apk/debug/app-debug.apk`。可使用 `./gradlew assembleRelease` 构建发布版本；项目未包含发布签名密钥，正式分发前需自行配置签名。

## 启动方式

使用兼容 PICO Spatial SDK 的设备及系统环境，开启开发者模式后，通过 Android Studio 安装运行；也可在确认目标设备后执行：

```sh
adb -s <设备序列号> install -r app/build/outputs/apk/debug/app-debug.apk
```

安装后在头显应用列表中启动“大黄蜂伙伴”。手部互动需要设备支持并启用手部追踪。普通 Android 手机不能提供完整空间体验。

## 目录说明

- `app/src/main/java/`：空间场景、UI、角色互动和跟随逻辑。
- `app/src/main/assets/model/bumblebee.glb`：包含站立、走路、跑步、比心和招手等动画的角色模型。
- `app/src/main/res/`：应用图标、图片和 Android 资源。
- `app/src/test/`：跟随逻辑、挥手检测等单元测试。
- `app/src/androidTest/`：设备侧测试。
- `gradle/`：Wrapper 与依赖版本配置。

模型中走路和跑步动画的水平根位移已处理，角色的位置由程序控制；替换模型时请避免重复叠加动画位移。

## 资源和许可说明

角色 GLB 的元数据标记生成工具为 Tripo。模型、图片及角色形象的具体授权凭证未随项目提供，本仓库不额外授予这些资源的再分发或商业使用许可，也不代表相关角色权利方的官方作品。正式公开发行或商业使用前，应由项目所有者确认相关授权。

第三方依赖通过 Gradle 获取，遵循各自许可条款；仓库不包含本机配置、签名密钥、访问令牌或依赖缓存。
