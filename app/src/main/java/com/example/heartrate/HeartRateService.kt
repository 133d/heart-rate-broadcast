package com.example.heartrate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log

/**
 * 心率监测服务 - 后台持续获取心率数据并广播
 */
class HeartRateService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private lateinit var wakeLock: PowerManager.WakeLock

    private var lastHeartRate: Int = 0
    private var lastBroadcastTime: Long = 0

    companion object {
        const val TAG = "HeartRateService"
        const val ACTION_HEART_RATE_UPDATE = "com.example.heartrate.HEART_RATE_UPDATE"
        const val CHANNEL_ID = "heart_rate_channel"
        const val NOTIFICATION_ID = 1
        
        // 最小广播间隔（毫秒）- 避免过于频繁
        const val MIN_BROADCAST_INTERVAL = 1000L
    }

    override fun onCreate() {
        super.onCreate()
        
        // 初始化传感器
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        
        // 获取 WakeLock 保持CPU运行
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HeartRateService::WakeLock"
        )
        
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "心率监测服务启动")
        
        // 获取 WakeLock
        if (!wakeLock.isHeld) {
            wakeLock.acquire(10*60*1000L) // 10分钟
        }
        
        // 启动为前台服务
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // 开始监听心率传感器
        startHeartRateMonitoring()
        
        return START_STICKY
    }

    /**
     * 开始心率监测
     */
    private fun startHeartRateMonitoring() {
        heartRateSensor?.let { sensor ->
            // 注册传感器监听 - 使用正常采样率
            val success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            
            if (success) {
                Log.d(TAG, "心率传感器注册成功")
                broadcastStatus("传感器已连接")
            } else {
                Log.e(TAG, "心率传感器注册失败")
                broadcastStatus("传感器连接失败")
            }
        } ?: run {
            Log.e(TAG, "设备不支持心率传感器")
            broadcastStatus("设备不支持心率传感器")
        }
    }

    /**
     * 停止心率监测
     */
    private fun stopHeartRateMonitoring() {
        sensorManager.unregisterListener(this)
        Log.d(TAG, "心率传感器已注销")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0].toInt()
            
            // 过滤无效数据
            if (heartRate > 0) {
                lastHeartRate = heartRate
                
                // 检查广播间隔
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBroadcastTime >= MIN_BROADCAST_INTERVAL) {
                    broadcastHeartRate(heartRate, currentTime)
                    lastBroadcastTime = currentTime
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "传感器精度变化: $accuracy")
    }

    /**
     * 广播心率数据
     */
    private fun broadcastHeartRate(heartRate: Int, timestamp: Long) {
        val intent = Intent(ACTION_HEART_RATE_UPDATE).apply {
            putExtra("heart_rate", heartRate)
            putExtra("timestamp", timestamp)
            setPackage(packageName)
        }
        
        sendBroadcast(intent)
        Log.d(TAG, "广播心率: $heartRate BPM")
        
        // 更新通知
        updateNotification(heartRate)
    }

    /**
     * 广播状态信息
     */
    private fun broadcastStatus(status: String) {
        val intent = Intent("${ACTION_HEART_RATE_UPDATE}_STATUS").apply {
            putExtra("status", status)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "心率监测"
            val descriptionText = "持续监测心率数据"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建前台服务通知
     */
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("心率监测中")
            .setContentText("正在获取心率数据...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * 更新通知内容
     */
    private fun updateNotification(heartRate: Int) {
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("心率监测中")
            .setContentText("当前心率: $heartRate BPM")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopHeartRateMonitoring()
        
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        
        Log.d(TAG, "心率监测服务已停止")
    }
}
