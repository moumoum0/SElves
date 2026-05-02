package com.selves.xnn.model

import androidx.annotation.StringRes
import com.selves.xnn.R

/**
 * 功能模块配置
 */
data class FunctionModuleConfig(
    val id: String,
    @StringRes val titleResId: Int,
    val iconName: String,
    val enabled: Boolean = true
) {
    companion object {
        fun defaultList(): List<FunctionModuleConfig> = listOf(
            FunctionModuleConfig("todo", R.string.home_module_todo, "Assignment", true),
            FunctionModuleConfig("dynamic", R.string.home_module_dynamic, "Timeline", true),
            FunctionModuleConfig("vote", R.string.home_module_vote, "Poll", true),
            FunctionModuleConfig("location", R.string.home_module_location, "LocationOn", true),
            FunctionModuleConfig("diary", R.string.home_module_diary, "Book", true)
        )
    }
}

/**
 * 首页布局配置
 */
data class HomeLayoutConfig(
    val moduleOrder: List<HomeModuleType> = listOf(
        HomeModuleType.FUNCTION_MODULES,
        HomeModuleType.LOCATION_TRACKING,
        HomeModuleType.TODO,
        HomeModuleType.DYNAMIC,
        HomeModuleType.VOTE,
        HomeModuleType.DIARY
    ),
    val moduleVisibility: Map<HomeModuleType, Boolean> = mapOf(
        HomeModuleType.FUNCTION_MODULES to true,
        HomeModuleType.LOCATION_TRACKING to true,
        HomeModuleType.TODO to true,
        HomeModuleType.DYNAMIC to true,
        HomeModuleType.VOTE to true,
        HomeModuleType.DIARY to true
    ),
    val functionModules: List<FunctionModuleConfig> = emptyList()
)
