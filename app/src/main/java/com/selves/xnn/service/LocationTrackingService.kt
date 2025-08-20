package com.selves.xnn.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.AlarmManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.selves.xnn.MainActivity
import com.selves.xnn.R
import com.selves.xnn.data.repository.LocationRecordRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service(), LocationListener {
    
    @Inject
    lateinit var locationRecordRepository: LocationRecordRepository
    
    private lateinit var locationManager: LocationManager
    private lateinit var geocoder: Geocoder
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var currentMemberId: String = ""
    private var recordingInterval: Long = 60000L // 默认1分钟
    private var isTracking = false
    private val saveLock = Any()
    @Volatile private var lastSavedAtMs: Long = 0L
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val TAG = "LocationTrackingService"
        
        const val ACTION_START_TRACKING = "START_TRACKING"
        const val ACTION_STOP_TRACKING = "STOP_TRACKING"
        const val ACTION_AUTO_RESTART = "AUTO_RESTART"
        
        // 状态广播相关常量
        const val ACTION_TRACKING_STATUS_CHANGED = "com.selves.xnn.TRACKING_STATUS_CHANGED"
        const val EXTRA_IS_TRACKING = "is_tracking"
        const val EXTRA_MEMBER_ID = "member_id"
        const val EXTRA_INTERVAL = "interval"
        
        // 静态变量追踪当前状态，便于外部查询
        @Volatile
        private var currentlyTracking = false
        
        /**
         * 检查轨迹记录服务是否正在运行
         */
        fun isTrackingActive(): Boolean = currentlyTracking
        
        fun startTracking(context: Context, memberId: String, interval: Long) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START_TRACKING
                putExtra(EXTRA_MEMBER_ID, memberId)
                putExtra(EXTRA_INTERVAL, interval)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopTracking(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
        
        fun scheduleAutoRestart(context: Context, memberId: String, interval: Long, delaySeconds: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // 检查精确闹钟权限（Android 12+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact alarms - permission not granted")
                    return
                }
            }
            
            val intent = Intent(context, AutoRestartReceiver::class.java).apply {
                action = ACTION_AUTO_RESTART
                putExtra(EXTRA_MEMBER_ID, memberId)
                putExtra(EXTRA_INTERVAL, interval)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
            val triggerAtMs = System.currentTimeMillis() + delaySeconds * 1000L
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                }
                Log.d(TAG, "Auto-restart scheduled in ${delaySeconds}s for member=$memberId")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to schedule auto-restart: ${e.message}")
            }
        }
        
        fun cancelAutoRestart(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AutoRestartReceiver::class.java).apply {
                action = ACTION_AUTO_RESTART
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Auto-restart canceled")
        }


    }
    
    override fun onCreate() {
        super.onCreate()
        
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        geocoder = Geocoder(this, Locale.getDefault())
        
        createNotificationChannel()
        Log.d(TAG, "Location tracking service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                currentMemberId = intent.getStringExtra(EXTRA_MEMBER_ID) ?: ""
                recordingInterval = intent.getLongExtra(EXTRA_INTERVAL, 60000L)
                startLocationTracking()
            }
            ACTION_STOP_TRACKING -> {
                stopLocationTracking()
                stopSelf()
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        serviceScope.cancel()
        // 确保静态状态变量被重置
        currentlyTracking = false
        Log.d(TAG, "Location tracking service destroyed")
    }
    
    private fun startLocationTracking() {
        if (currentMemberId.isEmpty()) {
            Log.e(TAG, "Cannot start tracking: member ID is empty")
            return
        }
        
        if (!hasLocationPermission()) {
            Log.e(TAG, "Cannot start tracking: no location permission")
            return
        }
        
        try {
            isTracking = true
            currentlyTracking = true
            
            // 请求位置更新
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    recordingInterval,
                    0f, // 最小距离0米，确保即使静止也会回调
                    this
                )
                
                // 也监听网络位置提供者作为备用
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    recordingInterval,
                    0f,
                    this
                )
                
                // 立即获取最后一次已知位置并记录
                recordLastKnownLocation()
            }
            
            startForeground(NOTIFICATION_ID, createNotification("位置记录中..."))
            Log.d(TAG, "Location tracking started for member: $currentMemberId")
            
            // 广播状态变化
            broadcastTrackingStatusChanged(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location tracking", e)
        }
    }
    
    private fun stopLocationTracking() {
        isTracking = false
        currentlyTracking = false
        locationManager.removeUpdates(this)
        stopForeground(true)
        Log.d(TAG, "Location tracking stopped")
        
        // 广播状态变化
        broadcastTrackingStatusChanged(false)
    }
    
    // 获取并记录最后一次已知位置（开始记录时立即执行）
    private fun recordLastKnownLocation() {
        if (!hasLocationPermission()) return
        
        try {
            // 尝试获取GPS的最后已知位置
            val gpsLocation = if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else null
            
            // 尝试获取网络的最后已知位置
            val networkLocation = if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null
            
            // 选择更准确的位置（GPS优于网络）
            val bestLocation = when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
                }
                gpsLocation != null -> gpsLocation
                networkLocation != null -> networkLocation
                else -> null
            }
            
            bestLocation?.let { location ->
                Log.d(TAG, "Recording initial location: ${location.latitude}, ${location.longitude}")
                serviceScope.launch {
                    try {
                        // 获取地址信息
                        val address = try {
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            addresses?.firstOrNull()?.getAddressLine(0)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to get address for initial location", e)
                            null
                        }
                        
                        // 保存初始位置记录
                        locationRecordRepository.addLocationRecord(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            altitude = if (location.hasAltitude()) location.altitude else null,
                            accuracy = if (location.hasAccuracy()) location.accuracy else null,
                            address = address,
                            memberId = currentMemberId,
                            note = "开始记录"
                        )
                        // 记录最近一次保存时间，避免紧接着的 provider 回调再次保存
                        lastSavedAtMs = System.currentTimeMillis()
                        
                        Log.d(TAG, "Initial location record saved: $address")
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving initial location record", e)
                    }
                }
            } ?: Log.w(TAG, "No last known location available")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last known location", e)
        }
    }
    
    override fun onLocationChanged(location: Location) {
        if (!isTracking) return
        
        Log.d(TAG, "Location changed: ${location.latitude}, ${location.longitude}")
        // 节流：合并来自不同 provider 的频繁回调，确保最小间隔
        val shouldSave = synchronized(saveLock) {
            val now = System.currentTimeMillis()
            val allow = (now - lastSavedAtMs) >= recordingInterval
            if (allow) {
                lastSavedAtMs = now
            }
            allow
        }
        if (!shouldSave) return

        serviceScope.launch {
            try {
                // 获取地址信息
                val address = try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    addresses?.firstOrNull()?.getAddressLine(0)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get address for location", e)
                    null
                }
                
                // 保存位置记录
                locationRecordRepository.addLocationRecord(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = if (location.hasAltitude()) location.altitude else null,
                    accuracy = if (location.hasAccuracy()) location.accuracy else null,
                    address = address,
                    memberId = currentMemberId
                )
                
                Log.d(TAG, "Location record saved: $address")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error saving location record", e)
            }
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "位置跟踪",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "轨迹记录服务通知"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(contentText: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("轨迹记录")
        .setContentText(contentText)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(createPendingIntent())
        .setOngoing(true)
        .setAutoCancel(false)
        .build()
    
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(this, 0, intent, flags)
    }
    
    /**
     * 广播轨迹记录状态变化
     */
    private fun broadcastTrackingStatusChanged(isTracking: Boolean) {
        val intent = Intent(ACTION_TRACKING_STATUS_CHANGED).apply {
            setPackage(packageName) // 使其成为显式Intent
            putExtra(EXTRA_IS_TRACKING, isTracking)
            putExtra(EXTRA_MEMBER_ID, currentMemberId)
            putExtra(EXTRA_INTERVAL, recordingInterval)
        }
        sendBroadcast(intent)
        Log.d(TAG, "Broadcasted tracking status change: $isTracking for member: $currentMemberId")
    }
}

