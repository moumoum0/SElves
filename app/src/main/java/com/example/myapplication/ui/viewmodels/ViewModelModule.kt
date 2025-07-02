package com.example.myapplication.ui.viewmodels

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * ViewModel相关依赖注入模块
 * 
 * 注意：MainViewModel使用了@HiltViewModel注解，所以不需要在这里手动提供
 * Hilt会自动通过构造函数注入来创建MainViewModel实例
 */
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    
    // MainViewModel已经使用@HiltViewModel注解，不需要手动提供
    // Hilt会自动处理所有依赖注入
} 