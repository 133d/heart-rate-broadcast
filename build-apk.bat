@echo off
chcp 65001 >nul
echo ==========================================
echo  OPPO Watch 3 心率广播 App - APK 构建脚本
echo ==========================================
echo.

REM 检查是否安装了 Android Studio
if exist "C:\Program Files\Android\Android Studio" (
    set ANDROID_STUDIO=C:\Program Files\Android\Android Studio
) else if exist "%LOCALAPPDATA%\Android\Sdk" (
    set ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk
) else (
    echo [错误] 未找到 Android SDK！
    echo 请先安装 Android Studio：https://developer.android.com/studio
    pause
    exit /b 1
)

REM 设置环境变量
if defined ANDROID_STUDIO (
    set ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk
)

set PATH=%PATH%;%ANDROID_SDK%\platform-tools;%ANDROID_SDK%\cmdline-tools\latest\bin

echo [1/4] 检查项目目录...
if not exist "app\build.gradle" (
    echo [错误] 请在项目根目录运行此脚本！
    pause
    exit /b 1
)
echo ✓ 项目结构检查通过

echo.
echo [2/4] 清理旧构建...
call gradlew clean 2>nul
if errorlevel 1 (
    echo [警告] 未找到 gradlew，尝试使用系统 gradle...
    gradle clean 2>nul
)
echo ✓ 清理完成

echo.
echo [3/4] 构建 Debug APK...
call gradlew assembleDebug --no-daemon

if errorlevel 1 (
    echo.
    echo [错误] 构建失败！
    echo 可能原因：
    echo   1. 未安装 Android SDK
    echo   2. 未配置 JAVA_HOME
    echo   3. 网络问题导致依赖下载失败
    echo.
    echo 请尝试用 Android Studio 打开项目并点击 "Sync Project"
    pause
    exit /b 1
)

echo ✓ 构建成功！

echo.
echo [4/4] 复制 APK 到输出目录...
if not exist "output" mkdir output

copy "app\build\outputs\apk\debug\app-debug.apk" "output\HeartRate-Broadcast-v1.0-debug.apk" >nul

echo.
echo ==========================================
echo  构建完成！
echo ==========================================
echo.
echo APK 位置：output\HeartRate-Broadcast-v1.0-debug.apk
echo.
echo 安装到 OPPO Watch 3：
echo   adb install -r output\HeartRate-Broadcast-v1.0-debug.apk
echo.
pause
