# CriminalIntent for Android
An app that records criminal events that happened in an office.

#### 简介
CriminalIntent是一款办公室陋习的记录App。它允许办公室职员去发现办公室中同事的陋习，用相机记录下来，写上陋习的详情，并通知通讯录里的这位同事。此外，还可以通过列表访问所有的陋习。

#### 技术栈
**整体架构：MVVM** 应用临时数据存储以LiveData的形式存储在ViewModel中，可供Activity或Fragment中的View观察。持久数据存储在Room数据库中，通过数据仓储类向ViewModel提供接口。

**布局：** 项目全局采用ConstraintLayout进行页面布局。ConstraintLayout中支持使用拖拽方式摆放组件的位置和设置其与其他组件的依赖关系。Google官方也极力推荐使用ConstraintLayout进行页面布局设计。

**页面路由：** 项目采用了单Activity + 多Fragment的设计思想。因此，页面路由采用了在托管Activity中设置SupportFragmentManager的方式。由SupportFragmentManager负责Fragment的加载，替换及返回栈的管理。

**项目列表：** 项目中的项目列表由RecyclerView实现。RecyclerView的数据源是ViewModel中的crime列表LiveData。当这个数据库中的crimes信息发生变化时，也同步更新RecyclerView。

**相机拍摄：** 相机拍摄功能是依靠Android系统中的支持处理MediaStore.ACTION_IMAGE_CAPTURE的应用来处理的，通常是支持照相功能的App。此外，保存照片的功能通过ContentProvider实现。
> 进入CrimeFragment后，会触发Observer对Fragment持有的Crime进行更新，此时就会拿到crime实体中存储的文件名，通过系统的context.fileDir和文件名创建一个File，最后用FileProvider的getUriForFile方法获取Uri，把这个Uri在启动相机的时候一起传给相机，相机会把照片保存进去。然后Fragment这边收到回调以后，刷一下ImageView让它显示地址中的最新照片即可。

**日期选择器：** 这里使用自己实现的DataPickerFragment。这个Fragment是一个全局单例类，它继承DialogFragment使得日期选择器可以浮动在页面智商。调用其show函数即可显示出来，show的参数1是Fragment管理器，传入FragmentManager即可。最后在Fragment的onDateSelected回调中拿到选择的日期，显示在UI中。

**信息发送：** 通过隐式Intent来完成陋习嫌疑人的信息告知。告知的方式非常多，可以通过短信、QQ、邮箱等很多APP完成。因此，消息发送的action是Intent.ACTION_SEND。在Intent中通过putExtra植入要传递消息，然后通过Intent.createChooser(intent, str)来封装intent和其上面显示的文字来创建一个Chooser页面，这个页面会显示所有支持发送的方式。startActivity(chooserIntent)即可。

**联系人选择：** 和相机一样通过隐式Intent触发联系人选择。Intent的action为`Intent.ACTION_PICK`，Intent第二参数设置为ContactsContract.Contacts.CONTENT_URI。构建好Intent后通过startActivityForResult发射出去。选择联系人后，已选联系人的数据会以URI的形式返回到Fragment的onActivityResult中。用ContentResolver解析取到的URI，并指定查询的列和相关条件，用cursor遍历结果。`cursor.getString(0)`第一行数据。

#### 项目架构

- database // Room数据库模块
    - CrimeDao // Dao数据库接口模块
    - CrimeDatabase // Room数据库模块
    - CrimeTypeConverters // Room类型转换器
- Crime // Entity实体类
- CrimeListViewModel // CrimeListFragment的VM
- CrimeDetailViewModel // CrimeFragment的VM
- CrimeFragment // Crime显示页面
- CrimeListFragment // Crime列表显示页面
- CrimeRepository  // 数据仓储类
- DatePickerFragment // 日期选择器DialogFragment实现
- MainActivity // Fragment托管Activity
- PictureUtils // 图片处理工具类
