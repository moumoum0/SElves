package com.selves.xnn.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.repository.SystemRepository
import com.selves.xnn.model.System
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SystemViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : ViewModel() {
    
    private val TAG = "SystemViewModel"
    
    // 当前系统
    private val _currentSystem = MutableStateFlow<System?>(null)
    val currentSystem: StateFlow<System?> = _currentSystem.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误状态
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadCurrentSystem()
    }
    
    /**
     * 加载当前系统
     */
    private fun loadCurrentSystem() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                systemRepository.getCurrentSystem().collect { system ->
                    _currentSystem.value = system
                    _isLoading.value = false
                    Log.d(TAG, "当前系统已加载: ${system?.name}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载系统失败: ${e.message}", e)
                _error.value = "加载系统失败: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 创建系统
     */
    fun createSystem(name: String, avatarUrl: String?, description: String) {
        viewModelScope.launch {
            try {
                val system = System(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    avatarUrl = avatarUrl,
                    description = description,
                    createdAt = java.lang.System.currentTimeMillis(),
                    updatedAt = java.lang.System.currentTimeMillis()
                )
                
                systemRepository.saveSystem(system)
                _currentSystem.value = system
                Log.d(TAG, "系统已创建: $name")
            } catch (e: Exception) {
                Log.e(TAG, "创建系统失败: ${e.message}", e)
                _error.value = "创建系统失败: ${e.message}"
            }
        }
    }
    
    /**
     * 更新系统
     */
    fun updateSystem(system: System) {
        viewModelScope.launch {
            try {
                val updatedSystem = system.copy(updatedAt = java.lang.System.currentTimeMillis())
                systemRepository.updateSystem(updatedSystem)
                _currentSystem.value = updatedSystem
                Log.d(TAG, "系统已更新: ${system.name}")
            } catch (e: Exception) {
                Log.e(TAG, "更新系统失败: ${e.message}", e)
                _error.value = "更新系统失败: ${e.message}"
            }
        }
    }
    
    /**
     * 检查系统是否存在
     */
    suspend fun hasSystem(): Boolean {
        return try {
            systemRepository.hasSystem()
        } catch (e: Exception) {
            Log.e(TAG, "检查系统存在性失败: ${e.message}", e)
            false
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }
} 