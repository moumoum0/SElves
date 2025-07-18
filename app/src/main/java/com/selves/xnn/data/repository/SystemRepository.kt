package com.selves.xnn.data.repository

import com.selves.xnn.data.AppDatabase
import com.selves.xnn.data.entity.SystemEntity
import com.selves.xnn.data.Mappers.toDomain
import com.selves.xnn.data.Mappers.toEntity
import com.selves.xnn.model.System
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * 系统仓库类，负责处理系统相关的数据操作
 */
@Singleton
class SystemRepository @Inject constructor(private val database: AppDatabase) {
    
    /**
     * 获取当前系统
     */
    fun getCurrentSystem(): Flow<System?> {
        return database.systemDao().getCurrentSystem()
            .map { entity -> entity?.toDomain() }
    }
    
    /**
     * 根据ID获取系统
     */
    suspend fun getSystemById(systemId: String): System? {
        return database.systemDao().getSystemById(systemId)?.toDomain()
    }
    
    /**
     * 检查系统是否存在
     */
    suspend fun hasSystem(): Boolean {
        return database.systemDao().getSystemCount() > 0
    }
    
    /**
     * 保存系统
     */
    suspend fun saveSystem(system: System) {
        try {
            database.systemDao().insertSystem(system.toEntity())
            Log.d("SystemRepository", "系统已保存: ${system.name}")
        } catch (e: Exception) {
            Log.e("SystemRepository", "保存系统失败: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * 更新系统
     */
    suspend fun updateSystem(system: System) {
        try {
            database.systemDao().updateSystem(system.toEntity())
            Log.d("SystemRepository", "系统已更新: ${system.name}")
        } catch (e: Exception) {
            Log.e("SystemRepository", "更新系统失败: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * 删除系统
     */
    suspend fun deleteSystem(system: System) {
        try {
            database.systemDao().deleteSystem(system.toEntity())
            Log.d("SystemRepository", "系统已删除: ${system.name}")
        } catch (e: Exception) {
            Log.e("SystemRepository", "删除系统失败: ${e.message}", e)
            throw e
        }
    }
} 