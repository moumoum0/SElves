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
        return value?.toEpochSecond(ZoneOffset.UTC)
    }
    
    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
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