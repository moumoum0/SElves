package com.selves.xnn.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.repository.SystemRepository
import com.selves.xnn.model.System
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    
    // 异常处理器，用于处理协程取消
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable is CancellationException) {
            Log.d(TAG, "协程被取消: ${throwable.message}")
        } else {
            Log.e(TAG, "协程异常: ${throwable.message}", throwable)
            _error.value = "系统操作失败: ${throwable.message}"
            _isLoading.value = false
        }
    }
    
    init {
        loadCurrentSystem()
    }
    
    /**
     * 加载当前系统
     */
    private fun loadCurrentSystem() {
        viewModelScope.launch(exceptionHandler) {
            try {
                _isLoading.value = true
                systemRepository.getCurrentSystem()
                    .catch { e ->
                        if (e is CancellationException) {
                            Log.d(TAG, "加载系统流被取消")
                        } else {
                            Log.e(TAG, "加载系统失败: ${e.message}", e)
                            _error.value = "加载系统失败: ${e.message}"
                        }
                        _isLoading.value = false
                    }
                    .collect { system ->
                        _currentSystem.value = system
                        _isLoading.value = false
                        Log.d(TAG, "当前系统已加载: ${system?.name}")
                    }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, "加载系统失败: ${e.message}", e)
                    _error.value = "加载系统失败: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }
    
    /**
     * 创建系统
     */
    fun createSystem(name: String, avatarUrl: String?) {
        viewModelScope.launch(exceptionHandler) {
            try {
                val system = System(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    avatarUrl = avatarUrl,
                    createdAt = java.lang.System.currentTimeMillis(),
                    updatedAt = java.lang.System.currentTimeMillis()
                )
                
                systemRepository.saveSystem(system)
                _currentSystem.value = system
                Log.d(TAG, "系统已创建: $name")
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, "创建系统失败: ${e.message}", e)
                    _error.value = "创建系统失败: ${e.message}"
                }
            }
        }
    }
    
    /**
     * 更新系统
     */
    fun updateSystem(system: System) {
        viewModelScope.launch(exceptionHandler) {
            try {
                val updatedSystem = system.copy(updatedAt = java.lang.System.currentTimeMillis())
                systemRepository.updateSystem(updatedSystem)
                _currentSystem.value = updatedSystem
                Log.d(TAG, "系统已更新: ${system.name}")
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, "更新系统失败: ${e.message}", e)
                    _error.value = "更新系统失败: ${e.message}"
                }
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
            if (e !is CancellationException) {
                Log.e(TAG, "检查系统存在性失败: ${e.message}", e)
            }
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