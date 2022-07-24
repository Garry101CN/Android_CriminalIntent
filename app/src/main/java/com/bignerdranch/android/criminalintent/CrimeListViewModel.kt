/**
 * CrimeList的ViewModel 将Model生命周期与Fragment绑定起来，只有在Fragment销毁时才会销毁ViewModel
 * @author Xilai Jiang
 * @version 1.1
 */
package com.bignerdranch.android.criminalintent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

/**
 * ViewModel仓库类
 */
class CrimeListViewModel : ViewModel() {

    init{
        Log.d("GarryNote", "Repeat call!")
    }

    private val crimeRepository = CrimeRepository.get() // ViewModel中hold一个仓库单例
    val crimeListLiveData = crimeRepository.getCrimes() // ViewModel的数据中心LiveData

    fun addCrime(crime: Crime){ // 调用仓库的addCrime方法来增加crime
        crimeRepository.addCrime(crime)
    }

}