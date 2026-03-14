# 使用指南 - OPPO Watch 3 心率广播 App

## 📱 安装步骤

### 1. 开发环境准备
- 安装 Android Studio (Hedgehog 或更新版本)
- 安装 Android SDK 34
- 配置 Wear OS 模拟器或准备 OPPO Watch 3 真机

### 2. 构建项目
```bash
# 打开项目
Open Android Studio -> Open -> 选择 heart-rate-broadcast 文件夹

# 同步 Gradle
点击 "Sync Project with Gradle Files"

# 构建 APK
Build -> Build Bundle(s) / APK(s) -> Build APK(s)
```

### 3. 安装到手表

#### 方式一：ADB 安装（推荐）
```bash
# 连接手表（确保已开启开发者模式）
adb connect <手表IP>:5555

# 安装 APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 启动应用
adb shell am start -n com.example.heartrate/.MainActivity
```

#### 方式二：Wear OS 配对安装
1. 手机安装 "Wear OS by Google" App
2. 配对 OPPO Watch 3
3. 在 Android Studio 中选择手表设备运行

### 4. 授予权限
首次运行时，应用会请求以下权限：
- **BODY_SENSORS**: 访问心率传感器
- **HIGH_SAMPLING_RATE_SENSORS**: 高采样率传感器
- **FOREGROUND_SERVICE**: 后台运行

**必须全部允许，否则无法获取心率数据。**

---

## 🎯 使用方法

### 查看心率
打开应用后，界面上会显示：
- 大字体心率数值（BPM）
- 当前状态（监测中/等待权限等）
- 数据广播状态

### 广播数据格式
应用通过 `Broadcast` 发送心率数据，其他应用可以接收：

```kotlin
// 广播 Action
val ACTION = "com.example.heartrate.HEART_RATE_UPDATE"

// 广播数据
intent.getIntExtra("heart_rate", 0)      // 心率值（BPM）
intent.getLongExtra("timestamp", 0)      // 时间戳
```

### 在其他应用中接收心率数据

```kotlin
class MyHeartRateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.example.heartrate.HEART_RATE_UPDATE") {
            val bpm = intent.getIntExtra("heart_rate", 0)
            val time = intent.getLongExtra("timestamp", 0)
            // 处理心率数据
        }
    }
}

// 注册接收器
val filter = IntentFilter("com.example.heartrate.HEART_RATE_UPDATE")
registerReceiver(receiver, filter)
```

---

## ⚙️ 配置说明

### 广播间隔
默认每秒广播一次心率数据。可在 `HeartRateService.kt` 中修改：
```kotlin
const val MIN_BROADCAST_INTERVAL = 1000L  // 1000ms = 1秒
```

### 心率范围检测
应用会过滤无效数据（心率 <= 0），并在数据模型中定义有效范围：
```kotlin
data class HeartRateData(
    val bpm: Int,
    val timestamp: Long,
    val isValid: Boolean = bpm in 30..220  // 正常心率范围
)
```

### 通知设置
应用以前台服务运行，显示持续通知：
- 通知标题：心率监测中
- 通知内容：当前心率值
- 点击可返回应用

---

## 🔧 常见问题

### Q: 无法获取心率数据？
**A:** 检查以下几点：
1. 是否授予了传感器权限？
2. 手表是否佩戴正确？心率传感器需要接触皮肤
3. 重启应用试试

### Q: 心率数据跳动太快？
**A:** 这是正常现象。心率传感器实时更新，你可以：
- 增加广播间隔时间
- 在接收端做数据平滑处理

### Q: 如何开机自启动？
**A:** `BootReceiver` 已实现开机自启动功能，但需要：
1. 授予 "自启动" 权限（部分系统需要手动设置）
2. 确保没有被电池优化杀掉

### Q: 耗电量如何？
**A:** 应用已做功耗优化：
- 使用前台服务保活
- 1秒广播间隔平衡实时性和功耗
- 智能 WakeLock 管理

---

## 📊 数据格式

### 心率区间
| BPM 范围 | 描述 |
|---------|------|
| 0-60 | 静息 |
| 61-100 | 正常 |
| 101-130 | 轻度运动 |
| 131-160 | 中度运动 |
| 161-220 | 高强度运动 |
| >220 | 异常 |

### 统计数据
应用内置 `HeartRateStatistics` 类，可计算：
- 平均心率
- 最高心率
- 最低心率
- 数据记录数

---

## 🔗 扩展开发

### 添加网络同步功能
```kotlin
// 在 HeartRateService 中添加
override fun onSensorChanged(event: SensorEvent?) {
    // ...原有代码
    
    // 发送到服务器
    sendToServer(heartRate, timestamp)
}

private fun sendToServer(bpm: Int, time: Long) {
    // 使用 Retrofit/OkHttp 发送数据
}
```

### 添加数据存储
```kotlin
// 使用 Room 数据库存储历史数据
@Dao
interface HeartRateDao {
    @Insert
    suspend fun insert(data: HeartRateEntity)
    
    @Query("SELECT * FROM heart_rate ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecent(): List<HeartRateEntity>
}
```

---

## 📄 许可

MIT License - 可自由使用和修改。

## 🆘 技术支持

遇到问题？检查以下几点：
1. 查看 Logcat 日志（Tag: HeartRateService）
2. 确认手表型号支持心率传感器
3. 检查 Android 版本（需要 API 30+）
