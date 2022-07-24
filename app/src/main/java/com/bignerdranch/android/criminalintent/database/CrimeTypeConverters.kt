package com.bignerdranch.android.criminalintent.database

/**
 * CrimeTypeConverter 数据类型转换器类
 * @author Xilai Jiang
 * @version 1.1
 */
import androidx.room.TypeConverter
import java.util.*

/**
 * 数据类型转换器，将非基本数据类型与基本数据类型互相转换
 */
class CrimeTypeConverters {

    /**
     * 将Date对象转换成时间戳（Long）
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    /**
     * 将时间戳转换成Date
     */
    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let {
            Date(it)
        }
    }

    /**
     * 将String ID转换成UUID
     */
    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }

    /**
     * 将UUID转换成 String ID
     */
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }
}