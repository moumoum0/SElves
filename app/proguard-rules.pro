# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# 保持 Compose 相关类
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }

# 保持 Room 相关类
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# 保持 Hilt 相关类
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.AndroidEntryPoint

# 保持数据类
-keep class com.selves.xnn.model.** { *; }
-keep class com.selves.xnn.data.entity.** { *; }

# 保持行号信息用于调试
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile