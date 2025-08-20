package com.selves.xnn.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AutoRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != LocationTrackingService.ACTION_AUTO_RESTART) return
        val memberId = intent.getStringExtra(LocationTrackingService.EXTRA_MEMBER_ID) ?: return
        val interval = intent.getLongExtra(LocationTrackingService.EXTRA_INTERVAL, 60000L)
        Log.d("AutoRestartReceiver", "Received auto-restart alarm, restarting tracking for member=$memberId")

        val serviceIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
            putExtra(LocationTrackingService.EXTRA_MEMBER_ID, memberId)
            putExtra(LocationTrackingService.EXTRA_INTERVAL, interval)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}


