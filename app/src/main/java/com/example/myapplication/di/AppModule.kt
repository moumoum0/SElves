package com.example.myapplication.di

import android.content.Context
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 此模块已弃用，所有数据库相关依赖已移至DatabaseModule
 * 保留此文件仅作为历史记录
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // 所有依赖已移至 DatabaseModule
} 