# 心率广播 App - OPPO Watch 3

## 项目说明

这是一个为 OPPO Watch 3 开发的 Wear OS 心率广播应用，可以实时获取心率数据并通过本地广播发送。

## 功能特性

- 实时获取心率传感器数据
- 后台持续监测
- 本地广播心率数据
- 低功耗优化

## 技术栈

- Kotlin
- Wear OS SDK
- Health Services API
- BroadcastReceiver

## 文件结构

```
app/
├── src/main/java/com/example/heartrate/
│   ├── MainActivity.kt          # 主界面
│   ├── HeartRateService.kt      # 心率监测服务
│   ├── HeartRateReceiver.kt     # 心率广播接收器
│   └── HeartRateViewModel.kt    # 数据模型
├── src/main/res/
│   └── layout/activity_main.xml # 主界面布局
└── AndroidManifest.xml          # 应用配置
```

## 使用方法

1. 在 Android Studio 中打开项目
2. 连接 OPPO Watch 3 或启动 Wear OS 模拟器
3. 运行应用
4. 授权传感器权限后，心率数据将开始广播
