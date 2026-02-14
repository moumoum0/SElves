# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# 注意：不要使用 -keep class androidx.compose.** { *; } 这种宽泛规则
# R8 已原生支持 Compose，无需额外 keep 规则
# 宽泛的 keep 规则会阻止 R8 移除未使用的 material-icons-extended 图标类(~30MB)

# 保持 Room 相关类
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# 保持数据类（Gson 序列化需要）
-keep class com.selves.xnn.model.** { *; }
-keep class com.selves.xnn.data.entity.** { *; }

# 保持行号信息用于调试
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile