package com.selves.xnn.model

/**
 * 功能模块配置
 */
data class FunctionModuleConfig(
    val id: String,
    val title: String,
    val iconName: String,
    val enabled: Boolean = true
) {
    companion object {
        fun defaultList(): List<FunctionModuleConfig> = listOf(
            FunctionModuleConfig("todo", "待办", "Assignment", true),
            FunctionModuleConfig("dynamic", "动态", "Timeline", true),
            FunctionModuleConfig("vote", "投票", "Poll", true),
            FunctionModuleConfig("location", "轨迹", "LocationOn", true)
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
        HomeModuleType.VOTE
    ),
    val moduleVisibility: Map<HomeModuleType, Boolean> = mapOf(
        HomeModuleType.FUNCTION_MODULES to true,
        HomeModuleType.LOCATION_TRACKING to true,
        HomeModuleType.TODO to true,
        HomeModuleType.DYNAMIC to true,
        HomeModuleType.VOTE to true
    ),
    val functionModules: List<FunctionModuleConfig> = FunctionModuleConfig.defaultList()
)
