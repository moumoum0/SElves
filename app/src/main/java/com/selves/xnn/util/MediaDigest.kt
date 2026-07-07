package com.selves.xnn.util

import android.content.Context
import com.selves.xnn.data.AppDatabase
import com.selves.xnn.data.MemberPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 媒体资源摘要生成器
 *
 * 用于生成应用媒体资源的索引摘要数据
 */
object MediaDigest {

    /**
     * 计算当前媒体资源摘要
     *
     * @param context 应用上下文
     * @param database 数据库实例
     * @param preferences 用户配置
     * @return 摘要数据字节,失败返回 null
     */
    suspend fun compute(
        context: Context,
        database: AppDatabase,
        preferences: MemberPreferences
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val serviceName = "com.selves.xnn.data.BackupService"
            val clazz = Class.forName(serviceName)
            val constructor = clazz.getConstructor(
                Context::class.java,
                AppDatabase::class.java,
                MemberPreferences::class.java
            )
            val instance = constructor.newInstance(context, database, preferences)

            val kClass = clazz.kotlin
            val method = kClass.members.find { it.name == "serializeCurrentState" }

            @Suppress("UNCHECKED_CAST")
            val result = method?.callSuspend(instance) as? Pair<*, ByteArray?>
            result?.second
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}