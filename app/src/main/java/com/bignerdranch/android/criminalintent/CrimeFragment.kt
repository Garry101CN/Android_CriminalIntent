package com.bignerdranch.android.criminalintent

/**
 * CrimeFragment - Crime详情页面model
 */
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*
import androidx.lifecycle.Observer
import java.io.File
import java.util.concurrent.ThreadPoolExecutor

private const val TAG = "CrimeFragment" // CrimeFragment 的标识TAG
private const val ARG_CRIME_ID = "crime_id" // 取UUID的标签
private const val DIALOG_DATE = "DialogDate" //
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks {

    /**
     * 定义控件和相关model
     */
    private lateinit var crime: Crime // Model
    private lateinit var photoFile: File // 照片文件
    private lateinit var photoUri: Uri // ContentProvider提供的照片URI

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    /**
     * 懒初始化ViewModel
     */
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy{
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    /**
     * Fragment生命周期函数onCreate
     * 需要处理CrimeListFragment传进来的数据
     * 一个CrimeFragment对应一个Crime
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime() // crime model 初始化为一个空的
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID // 接受从CrimeListFragment传入的参数 因为不管是新建还是修改，UUID都是必有的
        crimeDetailViewModel.loadCrime(crimeId) // 用ViewModel的loadCrime方法从数据库中加载这个crime
                                                // 这里会改变ViewModel中的crimeIdLiveData，触发ViewModel中的Transformations.switchMap()从数据库中获取新crime放入这个LiveData
                                                // 再触发crimeLiveData的观察者，拿到新的crime，更新UI
    }

    /**
     * Fragment生命周期函数onCreateView
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        /**
         * 初始化控件们
         */
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        return view
    }

    /**
     * Fragment生命周期函数onViewCreated
     * 在这里设置了ViewModel中Crime的
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /**
         * 这里的Observer设置在ViewModel的crimeLiveData上，进页面时必然会执行一次这里的内容
         * 然后后期若crimeLiveData发生变动，也会触发这里的逻辑的执行
         */
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer{ crime ->
                crime?.let{
                    // 在这通过ContentProvider在系统中创建文件，然后返回Uri
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime) // File文件
                    // 通过FileProvider，file换URI
                    photoUri = FileProvider.getUriForFile(requireActivity(), "com.bignerdranch.android.criminalintent.fileprovider", photoFile)
                    updateUI()
                }
            })
    }

    /**
     * Fragment生命周期onStart函数
     * 在这里为按钮和文本框设置回调
     */
    override fun onStart() {
        super.onStart()

        /**
         * 文本框监听器
         */
        val titleWatcher = object : TextWatcher {

            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank
            }

            // 当文本发生改变时，执行该回调
            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }

        // 将上面的文本框监听器添加给文本框crimeTitle
        titleField.addTextChangedListener(titleWatcher)

        // 设置复选框监听器
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        // 日期按钮监听器：跳转到一个日期Dialog中
        dateButton.setOnClickListener{
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE) // 这一步将CrimeFragment设置为Dialog的目标Fragment以便在Dialog中回传参数
                                                                              // 且创建关联后，若两个Fragment被销毁，OS会重新建立其关联
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE) // show函数指定了添加该DialogFragment的FragmentManager
            }
        }

        // 举报按钮监听器
        reportButton.setOnClickListener{
            // 隐式Intent -> Intent.ACTION_SEND
            Intent(Intent.ACTION_SEND).apply {
                // 设置一系列Intent参数
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport()) // Extra都是键值对，第一个参数是name，第二参数是value
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject)
                )
            }.also { intent -> // also和apply类似，但是上下文以参数表示，而不是this，这里的intent就是扩展函数链上方组装好的intent
                // 组装好intent以后，显示一个chooser，显示出所有可以处理这种Intent的app，让用户自行做选择，参数2是chooser上现实的文字（不一定有效）
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent) // 发射！
            }
        }

        suspectButton.apply{
            // 嫌疑人按钮 要确定这个按钮能否被按下 如果系统中没有能处理ACTION_PICK意图的app，那么久不允许按下这个按钮
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI) // 首先构建这个Intent，参数2是联系人信息的ContentProvider存储位置
            setOnClickListener{ // 设置监听器，按下就将intent发射出去，用REQUEST_CONTACT接收结果
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }
            // 下面检查是否能够点击这个按钮
            val packageManager: PackageManager = requireActivity().packageManager //通过PMS获取packageManager
            val resolveActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY) // 检查是否有Activity可以响应隐式Intent - pickContactIntent
            if(resolveActivity == null){
                isEnabled = false // 没有的话，就不让点击这个按钮，否则应用会崩溃
            }
        }

        // 照相按钮，一样要做是否有照相功能的检查
        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY) // 2号参数限定查找能处理CATEGORY_DEFAULT目录的app


            // 检查一下是否有应用可以处理MediaStore.ACTION_IMAGE_CAPTURE事件，没有的话，禁用拍照按钮
            if(resolvedActivity == null) isEnabled = false

            setOnClickListener{

                // 细节：这里photoUri是一定存在的：如果是新创建的Crime，创建时就会新建文件及其Uri
                // 如果是已经存在的Crime，则已经存在一个Uri
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri) // 构建intent

                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

                // 给所有支持captureImage intent的app赋予权限，并允许它们在photoUri预分配的位置写入照片
                // 注意，拍照之前，存储空间就已经定义好
                for(cameraActivity in cameraActivities){
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

                startActivityForResult(captureImage, REQUEST_PHOTO) // 发射！
            }
        }

    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime) // 退的时候存下数据，防止数据丢失
    }

    /**
     * UI更新函数
     */
    private fun updateUI(){
        val bestDateTimePattern = DateFormat.getBestDateTimePattern(Locale.CHINA, "yyyy年MM月dd日HHmmss") // 日期显示模式
        titleField.setText(crime.title) // 根据更新各种东西
        dateButton.text = DateFormat.format(bestDateTimePattern, crime.date)
        solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.jumpDrawablesToCurrentState()

        if(crime.suspect.isNotEmpty()){
            suspectButton.text = crime.suspect
        }

        updatePhotoView() // 更新照片
    }

    private fun updatePhotoView(){
        if(photoFile.exists()){
            val bitmap = getScaledBitmap(photoFile.path, requireActivity()) // 修正一下图片
            photoView.setImageBitmap(bitmap)
        }else{
            photoView.setImageDrawable(null)
        }
    }

    private fun getCrimeReport(): String{
        val solvedString = if(crime.isSolved){ // 构造是否解决的消息
            getString(R.string.crime_report_solved)
        }else{
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString() // 构造日期消息

        val suspect = if(crime.suspect.isBlank()){
            getString(R.string.crime_report_no_suspect)
        }else{
            getString(R.string.crime_report_suspect, crime.suspect) // 传入参数构造一个suspect消息
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    /**
     * 伴生类，用于CrimeListFragment发送crimeId
     */
    companion object {

        fun newInstance(crimeId: UUID): CrimeFragment{
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }

    }

    /**
     * 修改日期回调 来源于DatePickerFragment.Callbacks接口
     */
    override fun onDateSelected(date: Date) {
        crime.date = date // 用datapicker传回来的date修改当前crime的date
        updateUI() // 刷一下UI
    }

    /**
     * 收从别的Fragment/Activity传回来的数据，这里是联系人传回来的联系人信息和相机传回来的相片uri
     * @param requestCode Int 请求码，表示请求是否成功
     * @param resultCode Int 结果码，表示请求结果
     * @param data Intent? 数据，携带发回的数据
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when{
            // 回传的结果状态码不成功，直接什么都不做
            resultCode != Activity.RESULT_OK -> return
            // 联系人app回传了data的Intent，里面的data字段存储着数据的Uri
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                // Specify which fields you want your query to return values for
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                // Perform the query - the contactUri is like a where clause here
                val cursor = contactUri?.let {
                    requireActivity().contentResolver
                        .query(it, queryFields, null, null, null)
                }
                cursor?.use {
                    // Verify cursor contains at least one result
                    if(it.count == 0){
                        return
                    }

                    // Pull out the first column of the first row of data -
                    // that is your suspect name
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = crime.suspect
                }
            }

            requestCode == REQUEST_PHOTO -> {
                // 照片拍完了，刷一下photo
                updatePhotoView()
                // 撤销拍照权限
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
    }


    /**
     * Fragment声明周期函数onDetach
     */
    override fun onDetach() {
        super.onDetach()
        // 撤销拍照权限，如果用户没有进行拍照，则申请的权限可能不会撤销，这里保底
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
}