package com.example.heartrate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 开机自启动接收器
 * 设备重启后自动启动心率监测服务
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "设备启动，准备启动心率监测服务")
            
            context?.let { ctx ->
                // 启动心率监测服务
                val serviceIntent = Intent(ctx, HeartRateService::class.java)
                
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        ctx.startForegroundService(serviceIntent)
                    } else {
                        ctx.startService(serviceIntent)
                    }
                    Log.d(TAG, "心率监测服务已启动")
                } catch (e: Exception) {
                    Log.e(TAG, "启动服务失败: ${e.message}")
                }
            }
        }
    }
}
