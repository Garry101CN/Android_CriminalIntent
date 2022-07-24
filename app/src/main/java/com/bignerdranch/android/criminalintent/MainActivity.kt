/**
 * CriminalIntent 主Activity
 * @author Xilai Jiang
 * @version 1.1
 */

package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.*

/**
 * 主Activity名称TAG
 */
private const val TAG = "MainActivity"

/**
 * 主Activity类，仅承载一个FrameLayout，支撑Fragment
 * 继承：AppCompatActivity
 * 实现：CrimeListFragment.Callbacks回调接口
 * 接口用于响应"点击CrimeListFragment"事件，替换主Activity中FrameLayout显示的Fragment
 */
class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * 利用Activity自带的supportFragmentManager来管理Fragments
         */
        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) // 检查缓存中是否已经存在Fragment

        if (currentFragment == null) { // 如果不存在，那么利用CrimeListFragment的伴生类创建新的Fragment，这样做是因为可以通过newInstance的参数向Fragment传递数据
            val fragment = CrimeListFragment.newInstance()
            supportFragmentManager // 通过提交事务的方式，将fragment添加到fragment管理器中
                .beginTransaction() // 开始事务
                .add(R.id.fragment_container, fragment) // 添加fragment
                .commit() // 确认执行
        }
    }

    /**
     * 替换Fragment的回调，发生在CrimeListFragment中点击列表项时
     * 在Fragment通过context.onCrimeSelected()回到这里
     */
    override fun onCrimeSelected(crimeId: UUID) {
        // 创建Fragment，并将点击的crime的UUID传入Fragment页面
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment) // 注意，这里要用replace，前者是fragment的view，后者是fragment的controller
            .addToBackStack(null) // -> 这里是将被replace的fragment添加到回退栈中，点击back可以返回 <-
            .commit()
    }
}
