# 🚀 快速开始 - 获取 APK

## 方法一：使用 GitHub Actions 自动构建（最简单）

### 步骤 1：创建 GitHub 仓库
1. 登录 https://github.com
2. 点击 "New Repository"
3. 仓库名：`heart-rate-broadcast`
4. 设为 Private（可选）
5. 点击 "Create repository"

### 步骤 2：上传代码
```bash
# 在 D:\HeartRateApp 目录下执行
cd D:\HeartRateApp
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/你的用户名/heart-rate-broadcast.git
git push -u origin main
```

### 步骤 3：获取 APK
1. 进入 GitHub 仓库页面
2. 点击 "Actions" 标签
3. 等待构建完成（约 2-3 分钟）
4. 点击最新完成的 workflow
5. 在 "Artifacts" 部分下载 `HeartRate-Broadcast-APK`

---

## 方法二：使用 Android Studio 构建（推荐开发者）

### 步骤 1：安装 Android Studio
1. 下载：https://developer.android.com/studio
2. 安装时选择：
   - ✅ Android SDK
   - ✅ Android SDK Platform-Tools
   - ✅ Android SDK Build-Tools
   - ✅ Android Emulator（可选）

### 步骤 2：打开项目
1. 启动 Android Studio
2. 点击 "Open"
3. 选择 `D:\HeartRateApp` 文件夹
4. 等待 Gradle 同步完成（首次可能需要 5-10 分钟下载依赖）

### 步骤 3：构建 APK
1. 菜单栏选择 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
2. 等待构建完成
3. 右下角会出现提示 "Build Analyzer detected..."
4. 点击提示中的 "locate" 或在以下路径找到 APK：
   ```
   D:\HeartRateApp\app\build\outputs\apk\debug\app-debug.apk
   ```

### 步骤 4：安装到 OPPO Watch 3
```bash
# 连接手表（确保开启开发者模式）
adb connect 手表IP:5555

# 安装 APK
adb install -r D:\HeartRateApp\app\build\outputs\apk\debug\app-debug.apk

# 启动应用
adb shell am start -n com.example.heartrate/.MainActivity
```

---

## 方法三：使用命令行构建（高级用户）

### 前提条件
- 安装 Java JDK 17：https://adoptium.net/
- 安装 Android SDK：https://developer.android.com/studio#command-tools

### 构建步骤
```bash
cd D:\HeartRateApp

# 设置环境变量
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
set PATH=%PATH%;%ANDROID_HOME%\cmdline-tools\latest\bin

# 接受 SDK 许可
sdkmanager --licenses

# 下载必要组件
sdkmanager "platforms;android-34" "build-tools;34.0.0"

# 构建 APK
gradlew assembleDebug

# 输出位置
# app\build\outputs\apk\debug\app-debug.apk
```

---

## 📱 安装到 OPPO Watch 3

### 方式一：ADB 无线调试（推荐）
```bash
# 1. 确保手表和手机在同一 WiFi
# 2. 手表：设置 → 其他设置 → 开发者选项 → 开启 ADB 调试
# 3. 查看手表 IP：设置 → WLAN → 点击已连接网络

adb connect 192.168.x.x:5555
adb install -r HeartRate-Broadcast-v1.0-debug.apk
```

### 方式二：通过 Wear OS App
1. 手机安装 "Wear OS by Google" App
2. 配对 OPPO Watch 3
3. 在 Android Studio 中直接选择手表设备运行

---

## ⚠️ 常见问题

### Q: 构建时提示 "SDK not found"
**A:** 设置 ANDROID_HOME 环境变量指向 Android SDK 目录

### Q: 安装时提示 "INSTALL_FAILED_UPDATE_INCOMPATIBLE"
**A:** 先卸载旧版本：
```bash
adb uninstall com.example.heartrate
adb install -r HeartRate-Broadcast-v1.0-debug.apk
```

### Q: 应用无法获取心率
**A:** 
1. 检查手表是否佩戴正确（传感器需接触皮肤）
2. 授予传感器权限
3. ColorOS 5.0 可能需要额外开启 "自启动" 和 "后台运行" 权限

### Q: ColorOS 5.0 特殊设置
**A:** OPPO Watch 3 的 ColorOS 5.0 需要：
1. 设置 → 应用管理 → 心率广播 → 权限管理 → 允许所有权限
2. 设置 → 电池 → 应用耗电管理 → 心率广播 → 允许后台运行
3. 设置 → 应用管理 → 自启动管理 → 允许心率广播自启动

---

## 📦 APK 信息

| 项目 | 值 |
|------|-----|
| 应用包名 | com.example.heartrate |
| 版本 | 1.0 |
| 最低 SDK | 30 (Android 11) |
| 目标 SDK | 34 (Android 14) |
| 文件大小 | 约 2-3 MB |

---

## 🔧 需要我帮你做什么？

1. **直接提供预编译 APK** - 我无法直接编译，但你可以用上面的方法获取
2. **添加新功能** - 比如心率报警、数据存储等
3. **修改界面** - 调整 UI 布局
4. **适配其他手表** - 比如小米手表、华为 Watch 等

有什么需要随时说！
