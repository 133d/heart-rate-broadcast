package com.example.heartrate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 心率广播接收器示例 - 其他应用可以使用此接收器接收心率数据
 * 
 * 使用方法：
 * 1. 在 AndroidManifest.xml 中注册接收器
 * 2. 或在代码中动态注册
 */
abstract class HeartRateReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "HeartRateReceiver"
        const val ACTION_HEART_RATE_UPDATE = "com.example.heartrate.HEART_RATE_UPDATE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_HEART_RATE_UPDATE -> {
                val heartRate = intent.getIntExtra("heart_rate", 0)
                val timestamp = intent.getLongExtra("timestamp", 0)
                
                if (heartRate > 0) {
                    Log.d(TAG, "收到心率数据: $heartRate BPM")
                    onHeartRateReceived(heartRate, timestamp)
                }
            }
            "${ACTION_HEART_RATE_UPDATE}_STATUS" -> {
                val status = intent.getStringExtra("status")
                status?.let { onStatusUpdate(it) }
            }
        }
    }

    /**
     * 收到心率数据时回调
     * @param heartRate 心率值（BPM）
     * @param timestamp 时间戳
     */
    abstract fun onHeartRateReceived(heartRate: Int, timestamp: Long)

    /**
     * 状态更新回调
     * @param status 状态信息
     */
    abstract fun onStatusUpdate(status: String)
}

/**
 * 简单的心率数据监听器接口
 */
interface OnHeartRateListener {
    fun onHeartRateChanged(heartRate: Int, timestamp: Long)
    fun onStatusChanged(status: String)
}

/**
 * 心率数据模型
 */
data class HeartRateData(
    val bpm: Int,
    val timestamp: Long,
    val isValid: Boolean = bpm in 30..220 // 正常心率范围
) {
    /**
     * 获取心率区间描述
     */
    fun getZoneDescription(): String {
        return when (bpm) {
            in 0..60 -> "静息"
            in 61..100 -> "正常"
            in 101..130 -> "轻度运动"
            in 131..160 -> "中度运动"
            in 161..220 -> "高强度运动"
            else -> "异常"
        }
    }

    /**
     * 格式化为字符串
     */
    override fun toString(): String {
        return "HeartRateData(bpm=$bpm, time=${formatTime()}, zone=${getZoneDescription()})"
    }

    private fun formatTime(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}

/**
 * 心率数据统计器
 */
class HeartRateStatistics {
    private val dataList = mutableListOf<HeartRateData>()
    private val maxSize = 100 // 保留最近100条数据

    /**
     * 添加心率数据
     */
    fun addData(data: HeartRateData) {
        dataList.add(data)
        if (dataList.size > maxSize) {
            dataList.removeAt(0)
        }
    }

    /**
     * 获取平均心率
     */
    fun getAverageBpm(): Int {
        if (dataList.isEmpty()) return 0
        return dataList.map { it.bpm }.average().toInt()
    }

    /**
     * 获取最高心率
     */
    fun getMaxBpm(): Int {
        return dataList.maxOfOrNull { it.bpm } ?: 0
    }

    /**
     * 获取最低心率
     */
    fun getMinBpm(): Int {
        return dataList.minOfOrNull { it.bpm } ?: 0
    }

    /**
     * 获取数据数量
     */
    fun getDataCount(): Int = dataList.size

    /**
     * 清空数据
     */
    fun clear() {
        dataList.clear()
    }
}
