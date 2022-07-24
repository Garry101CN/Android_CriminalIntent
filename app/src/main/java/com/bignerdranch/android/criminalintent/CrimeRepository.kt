package com.bignerdranch.android.criminalintent
/**
 * CriminalIntent 利用Room框架构造的数据持久化类
 * @author Xilai Jiang
 * @version 1.1
 */
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.criminalintent.database.CrimeDatabase
import com.bignerdranch.android.criminalintent.database.migration_1_2
import java.io.File
import java.util.*
import java.util.concurrent.Executors
/**
 * 数据库名称
 */
private const val DATABASE_NAME = "crime-database"
/**
 * 单例数据仓库类（注意使用私有的构造器）
 */
class CrimeRepository private constructor(context: Context) {

    /**
     * 最重要的Room数据库实例
     */
    private val database : CrimeDatabase = Room.databaseBuilder(
        context.applicationContext, // 参数1 - 全局context
        CrimeDatabase::class.java, // 参数2 - CrimeDatabase字节码文件
        DATABASE_NAME // 参数3 - 数据库名字
    ).addMigrations(migration_1_2) // 数据库版本升级迁移指定
        .build()

    /**
     * Room Dao对象
     */
    private val crimeDao = database.crimeDao()

    /**
     * 用于运行事务的线程池
     */
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * 数据库文件存放的路径
     */
    private val filesDir = context.applicationContext.filesDir


    /**
     * 对外开放的数据库操作函数封装
     */

    /* 获取所有crimes的列表 */
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    /* 通过UUID获取某个crime */
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    // ------  ↑ DQL 查询语句可以完全交给Room自行完成 ↑ --------
    // --------  ↓ DML 操作语句需要自行创建线程池执行 ↓ ---------

    /* 用线程池更新数据库 */
    fun updateCrime(crime: Crime){
        executor.execute{
            crimeDao.updateCrime(crime)
        }
    }
    /* 用线程池添加一个crime */
    fun addCrime(crime: Crime){
        executor.execute{
            crimeDao.addCrime(crime)
        }
    }

    /* 通过crime的model获取照片File */
    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)

    // 伴生类，用于创建Crime仓库单例
    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?:
            throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}