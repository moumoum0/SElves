package com.selves.xnn.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.selves.xnn.model.MapApp

/**
 * 地图导航工具类
 * 
 * 重要说明：关于应用列表权限管控
 * 
 * 根据中国工信部发布的行业标准：
 * - 《YD/T 2407-2021 移动智能终端安全能力技术要求》
 * - 《T/TAF 108-2022 移动终端应用软件列表权限实施指南》
 * 
 * 从2023年5月开始，所有在中国销售的Android设备（包括小米、华为、OPPO、vivo、
 * 三星等品牌）都需要遵循该标准，对获取应用列表的权限进行管控。
 * 
 * 影响：
 * - 应用需要申请"获取应用列表"权限才能检测其他应用是否已安装
 * - 在无权限的情况下，PackageManager.getInstalledPackages() 等API
 *   只能返回调用方应用的信息，无法获取完整的应用列表
 * 
 * 本工具类的适配策略：
 * 1. 优先尝试使用PackageManager检测应用安装状态
 * 2. 如果受到权限限制，使用Intent解析作为备选方案
 * 3. 提供详细的日志记录，帮助诊断权限问题
 * 
 * 参考资料：
 * https://dev.mi.com/xiaomihyperos/documentation/detail?pId=1619
 */
object MapNavigationUtils {
    
    private const val TAG = "MapNavigationUtils"
    
    /**
     * 导航到指定位置
     */
    fun navigateToLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
        mapApp: MapApp,
        name: String? = null
    ) {
        try {
            val intent = when (mapApp) {
                MapApp.GAODE -> createGaodeIntent(latitude, longitude, name)
                MapApp.BAIDU -> createBaiduIntent(latitude, longitude, name)
                MapApp.TENCENT -> createTencentIntent(latitude, longitude, name)
                MapApp.GOOGLE -> createGoogleIntent(latitude, longitude, name)
            }
            
            // 检查应用是否已安装
            if (isAppInstalled(context, mapApp.packageName)) {
                context.startActivity(intent)
            } else {
                // 如果应用未安装，尝试使用通用地图意图或打开应用商店
                val fallbackIntent = createFallbackIntent(latitude, longitude, name)
                if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(fallbackIntent)
                } else {
                    // 打开应用商店下载地图应用
                    openAppStore(context, mapApp.packageName)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开地图应用: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 创建高德地图意图
     */
    private fun createGaodeIntent(latitude: Double, longitude: Double, name: String?): Intent {
        // dev=1 表示传入 WGS84 坐标（常见于系统定位）；若已转换为 GCJ-02，可将 dev 设为 0
        val base = "androidamap://navi?sourceApplication=selves&lat=$latitude&lon=$longitude&dev=1"
        val uri = if (name.isNullOrEmpty()) base else "$base&poiname=${Uri.encode(name)}"
        return Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setPackage(MapApp.GAODE.packageName)
        }
    }
    
    /**
     * 创建百度地图意图
     */
    private fun createBaiduIntent(latitude: Double, longitude: Double, name: String?): Intent {
        // 明确坐标类型，WGS84 系统坐标：coord_type=wgs84；若已是 GCJ-02 可改为 gcj02
        val uri = if (name.isNullOrEmpty()) {
            "baidumap://map/direction?destination=latlng:$latitude,$longitude&mode=driving&coord_type=wgs84"
        } else {
            "baidumap://map/direction?destination=name:${Uri.encode(name)}|latlng:$latitude,$longitude&mode=driving&coord_type=wgs84"
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setPackage(MapApp.BAIDU.packageName)
        }
    }
    
    /**
     * 创建腾讯地图意图
     */
    private fun createTencentIntent(latitude: Double, longitude: Double, name: String?): Intent {
        // 使用 to + tocoord，并携带 referer
        val toName = if (name.isNullOrEmpty()) "目的地" else Uri.encode(name)
        val uri = "qqmap://map/routeplan?type=drive&to=$toName&tocoord=$latitude,$longitude&referer=Selves"
        return Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setPackage(MapApp.TENCENT.packageName)
        }
    }
    
    /**
     * 创建Google地图意图
     */
    private fun createGoogleIntent(latitude: Double, longitude: Double, name: String?): Intent {
        val uri = if (name.isNullOrEmpty()) {
            "google.navigation:q=$latitude,$longitude"
        } else {
            "google.navigation:q=$latitude,$longitude(${Uri.encode(name)})"
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setPackage(MapApp.GOOGLE.packageName)
        }
    }
    
    /**
     * 创建通用地图意图（备用方案）
     */
    private fun createFallbackIntent(latitude: Double, longitude: Double, name: String?): Intent {
        val uri = if (name.isNullOrEmpty()) {
            "geo:$latitude,$longitude?q=$latitude,$longitude"
        } else {
            "geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(name)})"
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    }
    
    /**
     * 检查中国工信部规范的获取应用列表权限
     * 根据 YD/T 2407-2021 标准要求
     */
    fun checkGetInstalledAppsPermission(context: Context): Boolean {
        return try {
            // 检查是否支持动态申请权限（主要适用于小米等厂商）
            val permissionInfo = context.packageManager.getPermissionInfo(
                "com.android.permission.GET_INSTALLED_APPS", 0
            )
            
            // 如果权限信息存在且来自系统安全组件，说明支持动态申请
            val supportsDynamicRequest = permissionInfo != null && 
                (permissionInfo.packageName.contains("miui") ||
                 permissionInfo.packageName.contains("security") ||
                 permissionInfo.packageName.contains("system"))
            
            if (supportsDynamicRequest) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, 
                    "com.android.permission.GET_INSTALLED_APPS"
                ) == PackageManager.PERMISSION_GRANTED
                
                return hasPermission
            } else {
                return true
            }
        } catch (e: PackageManager.NameNotFoundException) {
            true
        } catch (e: Exception) {
            Log.w(TAG, "检查获取应用列表权限时出错: ${e.message}")
            true
        }
    }
    
    /**
     * 检查应用是否已安装
     * 使用多种方式检测，以适应不同Android版本和厂商的限制
     * 遵循中国工信部YD/T 2407-2021标准要求
     */
    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        // 首先检查是否有获取应用列表的权限
        if (!checkGetInstalledAppsPermission(context)) {
            Log.w(TAG, "缺少获取应用列表权限，无法检测应用安装状态")
            // 在没有权限的情况下，使用intent检测作为备选方案
            return isAppAvailableViaIntent(context, packageName)
        }
        
        return try {
            // 方法1：尝试使用PackageManager获取包信息
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            // 方法2：尝试检查是否能解析启动意图（备选方案）
            isAppAvailableViaIntent(context, packageName)
        } catch (e: Exception) {
            Log.w(TAG, "Error checking app $packageName via PackageManager: ${e.message}")
            // 如果出现其他异常（如权限问题），尝试备选方法
            isAppAvailableViaIntent(context, packageName)
        }
    }
    
    /**
     * 通过意图检测应用是否可用（备选检测方案）
     */
    private fun isAppAvailableViaIntent(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            intent != null
        } catch (e: Exception) {
            Log.w(TAG, "Error checking app $packageName via intent: ${e.message}")
            false
        }
    }
    
    /**
     * 打开应用商店
     */
    private fun openAppStore(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            context.startActivity(intent)
        } catch (e: Exception) {
            // 如果没有应用商店，使用浏览器打开
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            context.startActivity(intent)
        }
    }
    
    /**
     * 获取可用的地图应用列表
     * 遵循中国工信部YD/T 2407-2021标准，支持权限管控的应用检测
     * 增强版检测，包含详细日志记录
     */
    fun getAvailableMapApps(context: Context): List<MapApp> {
        // 检查获取应用列表权限状态
        val hasPermission = checkGetInstalledAppsPermission(context)
        
        val availableApps = MapApp.values().filter { mapApp ->
            isAppInstalled(context, mapApp.packageName)
        }
        
        // 如果没有检测到任何地图应用，提供详细的诊断信息
        if (availableApps.isEmpty()) {
            if (!hasPermission) {
                Log.w(TAG, "警告：未检测到任何已安装的地图应用。可能是因为缺少获取应用列表权限（工信部YD/T 2407-2021标准要求）。")
            } else {
                Log.w(TAG, "警告：未检测到任何已安装的地图应用。建议用户安装地图应用。")
            }
        }
        
        return availableApps
    }
}

