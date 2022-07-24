/**
 * CrimeListFragment - crime列表页面controller
 * @author Xilai Jiang
 * @version 1.1
 */
package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Method
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    /**
     * 主Activity回调接口，需要主Activity实现
     * 因为只有MainActivity能调用supportFragmentManager，而点击事件是在Fragment中被处理的，所以需要这个接口给MainActivity回调
     */
    interface Callbacks{
        fun onCrimeSelected(crimeId: UUID)
    }


    private var callbacks : Callbacks? = null  // Callback实例，用于存储context，因为context也是一个Callbacks实例

    private lateinit var crimeRecyclerView: RecyclerView // 滚动列表
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList()) // 数据适配器，用空数组初始化

    private val crimeListViewModel: CrimeListViewModel by lazy { // Crime数据的Model，使用JetPack的ViewModel实现
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java) // 使用androidx.lifecycle库下的ViewModelProviders来加载一个ViewModel
    }

    /**
     * Fragment生命周期函数onCreateView
     * inflater用于引入fragment布局，这个布局作为函数的返回值
     * container传入inflate方法
     * savedInstanceState用于恢复状态
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false) // 引入view

        crimeRecyclerView =
            view.findViewById(R.id.crime_recycler_view) as RecyclerView // 初始化recyclerView -> 组装layoutmanager和adapter
        crimeRecyclerView.layoutManager = LinearLayoutManager(context) // 设置LayoutManager，传入托管Activity，
        crimeRecyclerView.adapter = adapter // 设置adapter 里面元素默认为空
        return view // 将组装好的view返回
    }

    /**
     * Fragment生命周期函数onViewCreated，视图创建好后回调
     * 在这里创建对ViewModel中的crimeListLiveData的监听器
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 这里用到crimeListViewModel时，才会执行by lazy中的代码创建ViewModel
        crimeListViewModel.crimeListLiveData.observe( // 监听crimeListViewModel中的crimeListLiveData
            viewLifecycleOwner, // Fragment的lifecycle owner
            Observer { crimes -> // crimeListLiveData发生变化后，取到LiveData里面的数据
                crimes?.let {
                    Log.d("GarryTag", "Refresh")
                    Log.i(TAG, "Got crimes ${crimes.size}") // 在控制台里输出一下取到数据的数量
                    updateUI(crimes) // 在UI线程中，更新UI
                }
            })
    }

    /**
     * UI更新函数，立即执行
     * 接受一个新的List<Crime>
     */
    private fun updateUI(crimes: List<Crime>) {
        adapter = CrimeAdapter(crimes) // 创建新的CrimeAdapter
        crimeRecyclerView.adapter = adapter // 并赋给RecyclerView
    }

    /**
     * RecyclerView必备私有内部类 - ViewHolder 列表每一项的View的Holder
     */
    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var crime: Crime  // 首先创建ViewHolder要Hold的那个view的数据model

        // 然后在成员中存放View中的组件并初始化View，注意要通过itemView来访问传入的view页面中的所有DOM

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title) // 每个View都含有一个titleTextView，显示
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date) // 一个dateTextview
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved) // 一个图像，表示这个crime是否已经解决

        init {
            // 构造器中为整个itemView创建一个点击监听器
            itemView.setOnClickListener(this)
        }

        /**
         * view绑定 - 给定一个model，绑定到view上
         * 这个会在adapter中的onBindViewHolder中调用
         */
        fun bind(crime: Crime) {
            this.crime = crime // 把model赋值给自己
            titleTextView.text = this.crime.title // 各种控件赋值
            dateTextView.text = this.crime.date.toString()
            solvedImageView.visibility = if (crime.isSolved) { // 如果已经解决了，就显示出来
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) { // 实现View.OnClickListener()接口，需要实现的点击事件方法
            callbacks?.onCrimeSelected(crime.id) //调用MainActivity的onCrimeSelector()方法，传入UUID启动新的Fragment
        }
    }

    /**
     * RecyclerView的数据适配器，控制着哪些数据要显示在界面上，为这些数据创建ViewHolder并绑定数据
     * ViewHolder接受一个列表作为要显示在RecyclerView中的项目库
     */
    private inner class CrimeAdapter(var crimes: List<Crime>)
        : RecyclerView.Adapter<CrimeHolder>() { // 泛型中传入ViewHolder的子类

        /**
         * 在onCreateViewHolder中创建View，并传进ViewHolder(view)
         * @param parent ViewGroup
         * @param viewType Int
         * @return CrimeHolder
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) // 如果调用了这个方法，说明RecyclerView正在创建一个List
                : CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false) // 我们inflate每一项的layout
            return CrimeHolder(view) // 用这个view创建一个ViewHolder
        }

        /**
         * onBindViewHolder 将即将显示的数据与view相绑定
         * @param holder CrimeHolder
         * @param position Int
         */
        override fun onBindViewHolder(holder: CrimeHolder, position: Int) { // 如果调用了这个方法，说明RecyclerView正在渲染这个项
            val crime = crimes[position]  // 拿到准备渲染的crime的model
            holder.bind(crime) // 将model绑定在ViewHolder上
        }

        /**
         * 返回数据的数量
         * @return Int
         */
        override fun getItemCount() = crimes.size // 找大小 不解释
    }

    /**
     * 伴生类，用于别的类实现来创建这个Fragment，调用区域在别的类中，操作区域在本类中
     */
    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    /**
     * Fragment生命周期：吸附时的回调
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks? // 把MainActivity赋给callbacks
    }

    /**
     * Fragment生命周期：脱离时的回调
     */

    override fun onDetach() {
        super.onDetach()
        callbacks = null // callbacks置为空
    }

    /**
     * 创建给Fragment创建AppBar的选项菜单时重写
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    /**
     * Fragment生命周期函数
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // 开启选项菜单
    }

    /**
     * 当某个选项被选中时的回调
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.new_crime -> {
                val crime = Crime() // 创建一个新的crime
                crimeListViewModel.addCrime(crime) // 加到数据库里（Observer检测到后，updateUI）
                callbacks?.onCrimeSelected(crime.id) // MainActivity更换Fragment，传入新创建的crime的UUID
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}