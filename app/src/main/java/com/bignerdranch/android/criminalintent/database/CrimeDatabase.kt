package com.bignerdranch.android.criminalintent.database

/**
 * Room数据库类
 * @author Xilai Jiang
 * @version 1.1
 */
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.criminalintent.Crime

/**
 * RoomDatabase是一个抽象类，且需要继承RoomDatabase()
 * 成员方法也都是抽象方法，定义了获取数据库相关操作的方法
 */
@Database(entities = [Crime::class], version = 2) // Database注解定义了一个RoomDatabase的基本属性，即存储的实体由哪些Kotlin class文件构成，当前版本号是多少
@TypeConverters(CrimeTypeConverters::class) // Room 只能自行处理基本数据类型，若遇到非基本数据类型需要些TypeConverter来转换成基本数据类型表达
abstract class CrimeDatabase : RoomDatabase() {

    abstract fun crimeDao(): CrimeDao // 抽象方法crimeDao()定义了Dao对象的获取方法和返回值类型
}

/**
 * 匿名内部类 Migration的实例migration_1_2定义了从版本1升级到版本2需要执行的操作
 */
val migration_1_2 = object : Migration(1,2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''")
    }
}