@echo off
chcp 65001 >nul
echo ==========================================
echo  心率广播 App - 一键构建安装脚本
echo ==========================================
echo.
echo 本脚本将自动：
echo   1. 检查环境（Java、ADB）
echo   2. 构建 APK
echo   3. 连接手表
echo   4. 安装应用
echo.
echo ==========================================
echo.

REM 配置：修改为你的手表 IP 地址
set WATCH_IP=192.168.1.100

REM 检查 Java
echo [1/5] 检查 Java 环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] Java 未安装！
    echo 请访问 https://adoptium.net/ 下载安装 Eclipse Temurin JDK 17
    echo.
    start https://adoptium.net/
    pause
    exit /b 1
)
echo ✓ Java 已安装

REM 检查 Gradle Wrapper
echo.
echo [2/5] 检查 Gradle...
if not exist "gradlew.bat" (
    echo [错误] 未找到 gradlew.bat
    echo 请确保在项目根目录运行此脚本
    pause
    exit /b 1
)
echo ✓ Gradle 可用

REM 构建 APK
echo.
echo [3/5] 构建 Debug APK...
echo 首次构建需要下载依赖，请耐心等待...
call gradlew.bat assembleDebug --no-daemon

if errorlevel 1 (
    echo.
    echo [错误] 构建失败！可能原因：
    echo   - 未安装 Android SDK
    echo   - 网络问题导致依赖下载失败
    echo   - JAVA_HOME 未配置
    echo.
    echo 解决方案：
    echo   1. 安装 Android Studio：https://developer.android.com/studio
    echo   2. 或按照 COMMAND_LINE_BUILD.md 配置 SDK
    pause
    exit /b 1
)

echo ✓ 构建成功

REM 复制 APK
echo.
echo [4/5] 复制 APK...
copy "app\build\outputs\apk\debug\app-debug.apk" "HeartRate-Broadcast.apk" >nul
echo ✓ APK 已复制到 HeartRate-Broadcast.apk

REM 检查 ADB
echo.
echo [5/5] 连接手表并安装...
adb version >nul 2>&1
if errorlevel 1 (
    echo [警告] 未找到 ADB，跳过安装步骤
    echo 请手动安装 APK 到手表：
    echo   adb connect %WATCH_IP%:5555
    echo   adb install -r HeartRate-Broadcast.apk
    goto finish
)

REM 连接手表
echo 正在连接手表 %WATCH_IP%...
adb connect %WATCH_IP%:5555

REM 检查设备
echo 检查设备...
adb devices | findstr "%WATCH_IP%" >nul
if errorlevel 1 (
    echo [警告] 无法连接手表，请检查：
    echo   1. 手表和电脑是否在同一 WiFi
    echo   2. 手表是否开启 ADB 调试
    echo   3. IP 地址是否正确（当前：%WATCH_IP%）
    echo.
    echo 手动修改脚本中的 WATCH_IP 变量，或手动安装：
    echo   adb install -r HeartRate-Broadcast.apk
    goto finish
)

REM 安装 APK
echo 正在安装...
adb install -r "HeartRate-Broadcast.apk"

if errorlevel 1 (
    echo.
    echo [警告] 安装失败，尝试卸载旧版本后重新安装...
    adb uninstall com.example.heartrate
    adb install -r "HeartRate-Broadcast.apk"
    
    if errorlevel 1 (
        echo [错误] 安装失败，请检查错误信息
        goto finish
    )
)

echo ✓ 安装成功！
echo.
echo 正在启动应用...
adb shell am start -n com.example.heartrate/.MainActivity

:finish
echo.
echo ==========================================
echo  完成！
echo ==========================================
echo.
echo APK 文件：HeartRate-Broadcast.apk
echo.
echo 如果应用未自动启动，请在手表上找到：
echo   "心率广播" 应用图标并打开
echo.
echo 首次运行需要授予传感器权限！
echo.
pause
