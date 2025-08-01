package com.selves.xnn.data

import androidx.room.TypeConverter
import com.selves.xnn.model.DynamicType
import com.selves.xnn.model.VoteStatus
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }
    
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): Long? {
        return try {
            value?.toEpochSecond(ZoneOffset.UTC)
        } catch (e: Exception) {
            android.util.Log.e("Converters", "LocalDateTime序列化失败: $value", e)
            null
        }
    }
    
    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? {
        return try {
            value?.let { 
                val result = LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
                android.util.Log.v("Converters", "LocalDateTime转换: $value -> $result")
                result
            }
        } catch (e: Exception) {
            android.util.Log.e("Converters", "LocalDateTime反序列化失败: $value", e)
            null
        }
    }
    
    @TypeConverter
    fun fromDynamicType(type: DynamicType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toDynamicType(type: String?): DynamicType? {
        return type?.let { DynamicType.valueOf(it) }
    }
    
    @TypeConverter
    fun fromVoteStatus(status: VoteStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toVoteStatus(status: String?): VoteStatus? {
        return status?.let { VoteStatus.valueOf(it) }
    }
} 