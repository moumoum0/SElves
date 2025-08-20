package com.selves.xnn.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object LocationPermissionHelper {
    
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    /**
     * 检查是否有位置权限
     */
    fun hasLocationPermission(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 检查是否有精确位置权限
     */
    fun hasFineLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查是否有粗略位置权限
     */
    fun hasCoarseLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 请求位置权限
     */
    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            REQUIRED_PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * 检查权限请求结果
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): LocationPermissionResult {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return LocationPermissionResult.UNKNOWN
        }
        
        val fineLocationIndex = permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationIndex = permissions.indexOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        
        val hasFineLocation = fineLocationIndex != -1 && 
                grantResults[fineLocationIndex] == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = coarseLocationIndex != -1 && 
                grantResults[coarseLocationIndex] == PackageManager.PERMISSION_GRANTED
        
        return when {
            hasFineLocation -> LocationPermissionResult.FINE_LOCATION_GRANTED
            hasCoarseLocation -> LocationPermissionResult.COARSE_LOCATION_GRANTED
            else -> LocationPermissionResult.DENIED
        }
    }
    
    /**
     * 检查是否应该显示权限说明
     */
    fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
        return REQUIRED_PERMISSIONS.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }
}

enum class LocationPermissionResult {
    FINE_LOCATION_GRANTED,
    COARSE_LOCATION_GRANTED,
    DENIED,
    UNKNOWN
}

