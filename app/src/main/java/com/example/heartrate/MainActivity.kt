package com.example.heartrate

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 主界面 - 显示心率数据
 */
class MainActivity : ComponentActivity() {

    private lateinit var tvHeartRate: TextView
    private lateinit var tvStatus: TextView
    private val handler = Handler(Looper.getMainLooper())

    // 心率广播接收器
    private val heartRateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val heartRate = intent?.getIntExtra("heart_rate", 0)
            val timestamp = intent?.getLongExtra("timestamp", 0)
            
            heartRate?.let { bpm ->
                updateUI(bpm, timestamp ?: System.currentTimeMillis())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvHeartRate = findViewById(R.id.tv_heart_rate)
        tvStatus = findViewById(R.id.tv_status)

        // 检查权限
        if (checkPermissions()) {
            startHeartRateService()
        } else {
            requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        // 注册广播接收器
        val filter = IntentFilter("com.example.heartrate.HEART_RATE_UPDATE")
        registerReceiver(heartRateReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        // 注销广播接收器
        unregisterReceiver(heartRateReceiver)
    }

    /**
     * 检查必要权限
     */
    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, 
            Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 请求权限
     */
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
                Manifest.permission.FOREGROUND_SERVICE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startHeartRateService()
            } else {
                tvStatus.text = "需要传感器权限"
            }
        }
    }

    /**
     * 启动心率监测服务
     */
    private fun startHeartRateService() {
        val serviceIntent = Intent(this, HeartRateService::class.java)
        startService(serviceIntent)
        tvStatus.text = "监测中..."
    }

    /**
     * 更新UI显示
     */
    private fun updateUI(heartRate: Int, timestamp: Long) {
        handler.post {
            tvHeartRate.text = "$heartRate BPM"
            tvStatus.text = "最后更新: ${formatTime(timestamp)}"
        }
    }

    /**
     * 格式化时间戳
     */
    private fun formatTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
