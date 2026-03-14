# 🔧 方法三：命令行构建 APK（详细指南）

## ⚠️ 前置条件

在开始之前，你需要安装：

1. **Java JDK 17** ⭐必须
   - 下载：https://adoptium.net/
   - 选择：Eclipse Temurin 17 (LTS)
   - 安装后配置 JAVA_HOME 环境变量

2. **Android SDK** ⭐必须
   - 方式A：安装 Android Studio（自动包含 SDK）
   - 方式B：只下载命令行工具（见下文）

---

## 📥 第一步：安装 Java JDK 17

### 下载安装
1. 访问：https://adoptium.net/
2. 选择 **Eclipse Temurin 17**（Windows x64 MSI Installer）
3. 下载并运行安装程序
4. 安装路径保持默认：`C:\Program Files\Eclipse Adoptium\jdk-17`

### 配置环境变量
```powershell
# 以管理员身份打开 PowerShell，执行：
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17", "Machine")
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\Eclipse Adoptium\jdk-17\bin", "Machine")
```

### 验证安装
```bash
java -version
# 应该显示：openjdk version "17.0.x"
```

---

## 📥 第二步：安装 Android SDK

### 方式A：通过 Android Studio（推荐）
1. 下载：https://developer.android.com/studio
2. 安装时选择：
   - ✅ Android SDK
   - ✅ Android SDK Platform
   - ✅ Android Virtual Device（可选）

### 方式B：仅命令行工具（轻量）
1. 创建 SDK 目录：
```powershell
mkdir C:\Android\Sdk
cd C:\Android\Sdk
```

2. 下载命令行工具：
   - 访问：https://developer.android.com/studio#command-tools
   - 下载 "commandlinetools-win-xxxxxxxx_latest.zip"
   - 解压到 `C:\Android\Sdk\cmdline-tools\latest\`

3. 配置环境变量：
```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Android\Sdk", "Machine")
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Android\Sdk\cmdline-tools\latest\bin;C:\Android\Sdk\platform-tools", "Machine")
```

---

## 📥 第三步：安装 SDK 组件

重启终端，然后执行：

```bash
# 接受许可协议
sdkmanager --licenses
# 一路输入 'y' 同意所有

# 安装必要组件
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "platform-tools"
```

---

## 🚀 第四步：构建 APK

### 打开项目目录
```bash
cd D:\HeartRateApp
```

### 构建 Debug APK
```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

### 首次构建
首次构建会下载 Gradle 和所有依赖，可能需要 **5-15 分钟**，请耐心等待。

你会看到类似输出：
```
> Task :app:assembleDebug
BUILD SUCCESSFUL in 2m 34s
56 actionable tasks: 56 executed
```

---

## 📦 第五步：获取 APK

构建成功后，APK 位置：
```
D:\HeartRateApp\app\build\outputs\apk\debug\app-debug.apk
```

复制到桌面方便使用：
```bash
copy "app\build\outputs\apk\debug\app-debug.apk" "%USERPROFILE%\Desktop\HeartRate-Broadcast-v1.0.apk"
```

---

## 📱 第六步：安装到 OPPO Watch 3

### 启用 ADB 调试（手表端）
1. 打开手表：设置 → 关于手表
2. 连续点击 "版本号" 7次，开启开发者模式
3. 返回：设置 → 其他设置 → 开发者选项
4. 开启：**ADB调试** 和 **通过WLAN调试**
5. 记录显示的 IP 地址（如：192.168.1.100:5555）

### 连接并安装（电脑端）
```bash
# 连接手表（确保在同一WiFi）
adb connect 192.168.1.100:5555

# 验证连接
adb devices

# 安装 APK
adb install -r "%USERPROFILE%\Desktop\HeartRate-Broadcast-v1.0.apk"

# 启动应用
adb shell am start -n com.example.heartrate/.MainActivity
```

---

## 🛠️ 常用命令

```bash
# 清理构建
gredlew.bat clean

# 重新构建
gredlew.bat assembleDebug

# 构建 Release 版本（需要签名）
gredlew.bat assembleRelease

# 查看所有可用任务
gredlew.bat tasks

# 查看连接的设备
adb devices

# 查看日志
adb logcat -s HeartRateService:D

# 卸载应用
adb uninstall com.example.heartrate
```

---

## ❌ 常见问题

### 问题1：'gradlew' 不是内部或外部命令
**解决**：确保在项目根目录执行，使用 `gradlew.bat`（Windows）或 `./gradlew`（Linux/Mac）

### 问题2：Could not find tools.jar
**解决**：JAVA_HOME 配置错误，检查路径是否指向 JDK 而非 JRE

### 问题3：Connection timed out: connect
**解决**：网络问题，Gradle 需要下载依赖。可以配置国内镜像，在 `build.gradle` 中添加：
```gradle
repositories {
    maven { url 'https://maven.aliyun.com/repository/public' }
    maven { url 'https://maven.aliyun.com/repository/google' }
    google()
    mavenCentral()
}
```

### 问题4：SDK not found
**解决**：检查 ANDROID_HOME 环境变量是否正确设置
```bash
echo %ANDROID_HOME%
# 应该显示：C:\Android\Sdk
```

### 问题5：INSTALL_FAILED_UPDATE_INCOMPATIBLE
**解决**：签名冲突，先卸载旧版本：
```bash
adb uninstall com.example.heartrate
adb install -r app-debug.apk
```

### 问题6：手表无法连接 ADB
**解决**：
1. 确保手表和电脑在同一 WiFi
2. 手表端重新开关 "通过WLAN调试"
3. 尝试使用 USB 调试（如果有数据线）

---

## 📝 一键脚本（Windows）

创建 `build-and-install.bat`：

```bat
@echo off
echo ==========================================
echo  心率广播 App - 一键构建安装
echo ==========================================
echo.

set WATCH_IP=192.168.1.100

echo [1/5] 检查环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] Java 未安装，请先安装 JDK 17
    pause
    exit /b 1
)
echo ✓ Java 已安装

echo.
echo [2/5] 构建 APK...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo [错误] 构建失败
    pause
    exit /b 1
)
echo ✓ 构建成功

echo.
echo [3/5] 复制 APK...
copy "app\build\outputs\apk\debug\app-debug.apk" "HeartRate-Broadcast.apk" >nul
echo ✓ APK 已复制

echo.
echo [4/5] 连接手表...
adb connect %WATCH_IP%:5555
echo ✓ 已连接 %WATCH_IP%

echo.
echo [5/5] 安装 APK...
adb install -r "HeartRate-Broadcast.apk"
if errorlevel 1 (
    echo [警告] 安装失败，尝试卸载后重新安装...
    adb uninstall com.example.heartrate
    adb install -r "HeartRate-Broadcast.apk"
)

echo.
echo ==========================================
echo  完成！
echo ==========================================
echo.
pause
```

把 `192.168.1.100` 换成你手表的实际 IP，然后双击运行即可！

---

## ✅ 验证安装

安装成功后，手表上会显示：
- 应用图标：心率广播
- 打开后：大字体显示当前心率（BPM）
- 授予传感器权限后，开始显示实时心率

---

有问题随时问我！
