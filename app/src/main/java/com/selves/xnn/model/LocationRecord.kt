package com.selves.xnn.model

import android.content.Context
import com.selves.xnn.R
import java.time.LocalDateTime

/**
 * 位置记录数据模型
 */
data class LocationRecord(
    val id: String,
    val latitude: Double,        // 纬度
    val longitude: Double,       // 经度
    val altitude: Double? = null, // 海拔
    val accuracy: Float? = null,  // 精度
    val address: String? = null,  // 地址描述
    val timestamp: LocalDateTime, // 记录时间
    val memberId: String,        // 记录者ID
    val note: String? = null     // 备注
)

/**
 * 轨迹记录配置
 */
data class TrackingConfig(
    val isEnabled: Boolean = false,           // 是否启用轨迹记录
    val recordingInterval: Int = 60,          // 记录间隔（秒）
    val autoRestartDelay: Int = 300,          // 关闭后自动重启延迟（秒）
    val enableAutoStart: Boolean = false      // 是否启用自动开启记录
)

/**
 * 轨迹记录状态
 */
enum class TrackingStatus {
    STOPPED,    // 已停止
    RECORDING   // 记录中
}

/**
 * 轨迹统计信息
 */
data class TrackingStats(
    val totalRecords: Int = 0,
    val todayRecords: Int = 0,
    val lastRecordTime: LocalDateTime? = null,
    val recordingDuration: Long = 0 // 记录时长（毫秒）
)

/**
 * 地图导航类型
 */
enum class MapApp(val packageName: String) {
    GAODE("com.autonavi.minimap"),
    BAIDU("com.baidu.BaiduMap"),
    TENCENT("com.tencent.map"),
    GOOGLE("com.google.android.apps.maps");

    fun getDisplayName(context: Context): String = when (this) {
        GAODE -> context.getString(R.string.map_app_amap)
        BAIDU -> context.getString(R.string.map_app_baidu)
        TENCENT -> context.getString(R.string.map_app_tencent)
        GOOGLE -> context.getString(R.string.map_app_google)
    }
}

