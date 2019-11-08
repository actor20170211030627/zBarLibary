# zBarLibary
### 1.修改自: [zBarLibary](https://github.com/actor20170211030627/zBarLibary "zBarLibary"), 感谢

做了如下修改:
<ol>
	<li>zxing 3.4.0在低版本手机解析二维码时会报错, 做了修改</li>
	<li>在<code>zBarLibary</code>作者的基础上新增功能: 自定义界面</li>
	<li>最新修改日期: 2019-10-16 (1.4.0)</li>
	<li>效果见原作者一样的效果</li>
</ol>

### 2.minSdkVersion [![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16)
    如果您项目的minSdkVersion小于16, 可能会报错: Manifest merger failed with multiple errors, see logs

### ## 7.How to
To get a Git project into your build:

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:
<pre>
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
</pre>

**Step 2.** Add the dependency, the last version:
[![](https://jitpack.io/v/actor20170211030627/zbarlibary.svg)](https://jitpack.io/#actor20170211030627/zbarlibary)

    dependencies {
            implementation 'com.github.actor20170211030627:zbarlibary:last_version'
    }



#### 关于包的大小问题
为了确保全平台的兼容，默认库中携带了arm64-v8a，armeabi，armeabi-v7a，mips，mips64，x86，x86_64，的so文件，可能会导致安装包体积大，和其他第三方SDK冲突的问题，可以使用以下代码解决大小和冲突
<pre>
android {
    ...
    defaultConfig {
        ...
        ndk {
            //指定要ndk需要兼容的架构
            abiFilters "armeabi", "armeabi-v7a", "x86", "arm64-v8a", "mips", "mips64", "x86_64"
        }
    }
}
</pre>


### 使用方法
## 1.识别二维码（条形码）

### 1.1.Activity <I>'示例'</I>
<pre>
public class TestCustomScanActivity extends QRActivity {

    private VerticalSeekBar vsb_zoom;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        
        //布局中必须有一个 id 为 'camera_preview' 的 CameraPreview
        setContentView(R.layout.activity_test_custom_scan);

        vsb_zoom = findViewById(R.id.vsb_zoom);
        findViewById(R.id.iv_album).setOnClickListener(this);
        findViewById(R.id.iv_flash).setOnClickListener(this);


        //配置扫描(有默认值, 都可以不配置)
        isFingerZoom = true;//是否能手势缩放, 默认true
        isPlaySound    = true;//扫描完成后, 是否要播放声音, 默认true
        isShowVibrator = false;//振动提醒, 默认false
        isNeedCrop     = true;//是否从相册选择后裁剪图片, 默认true
        isAutoLight    = false;//是否自动灯光, 默认false
        dingPath = R.raw.test;//扫描成功后, 播放的音乐文件, 默认R.raw.qrcode
        OPEN_ALBUM_TEXT = "选择要识别的图片";//打开相册的文字
        //设置扫码类型, 默认全部（二维码，条形码，全部，自定义，默认为二维码）
        Symbol.scanType = QrConfig.TYPE_ALL;
        //设置扫描的码的具体类型, 此项只有在扫码类型为TYPE_CUSTOM时才有效
        Symbol.scanFormat = QrConfig.BARCODE_EAN13;
        //是否只识别框中内容(默认为false: 全屏识别)
        Symbol.is_only_scan_center = false;
        //是否开启自动缩放, 默认false (实验性功能，不建议使用)
        Symbol.is_auto_zoom = false;
        //是否开启双引擎识别, 默认true (仅对识别二维码有效，并且开启后只识别框内功能将失效)
        Symbol.doubleEngine = true;
        //是否连续扫描二维码, 默认true
        Symbol.looperScan = true;
        //连续扫描间隔时间, 默认1s
        Symbol.looperWaitTime = 1000;


        //设置SeekBar颜色
        setSeekBarColor(vsb_zoom, getResources().getColor(R.color.red_E42E30));
        //设置监听
        vsb_zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setZoom(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onScanSuccess(ScanResult result) {
        Log.e(TAG, "onScanSuccess: " + result);
        Toast.makeText(getApplicationContext(), "内容：" + result.getContent()
                + "  类型：" + result.getType(), Toast.LENGTH_SHORT).show();
//        onBackPressed();//退出本页
    }

    //从相册选择图片识别后, 获取到的图片为空(可不用重写此方法)
    @Override
    protected void selectImageIsEmpty() {
        super.selectImageIsEmpty();
        Toast.makeText(getApplicationContext(), "选择图片识别, 获取图片失败！", Toast.LENGTH_SHORT).show();
    }

    //从相册选择图片识别后, 识别本地图片失败(可不用重写此方法)
    @Override
    protected void recognitionLocalFailure(String imagePath) {
        super.recognitionLocalFailure(imagePath);
        Toast.makeText(getApplicationContext(), "识别失败！, path: ".concat(imagePath), Toast.LENGTH_SHORT).show();
    }

    //从相册选择图片识别后, 识别本地图片异常(可不用重写此方法)
    @Override
    protected void recognitionLocalException(Exception e) {
        super.recognitionLocalException(e);
        Toast.makeText(getApplicationContext(), "识别异常！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_album://相册
                selectImageFromAlbum();
                break;
            case R.id.iv_flash://闪光灯
                setFlash();
                break;
        }
    }
</pre>

### 1.2. <code>activity_my.xml</code>布局文件 <I>'示例'</I>
	<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
	    xmlns:app="http://schemas.android.com/apk/res-auto"
	    xmlns:tools="http://schemas.android.com/tools"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    tools:context="com.actor.zbarlibaryTest.TestCustomScanActivity">
	
	
	    <!-- 二维码识别界面, 必须要有 -->
	    <cn.bertsir.zbar.CameraPreview
	        android:id="@+id/camera_preview"
	        android:layout_width="match_parent"
	        android:layout_height="match_parent" />
	
	    <!--扫描界面(4个角 & 扫描View: 网格、雷达、线条), 可自定义参照修改-->
	    <include layout="@layout/view_scan" />
	
	    <!--标题栏-->
	    <FrameLayout
	        android:layout_width="match_parent"
	        android:layout_height="40dp"
	        android:background="#ff5f00"
	        android:fitsSystemWindows="true">
	
	        <!--返回键-->
	        <ImageView
	            android:layout_width="30dp"
	            android:layout_height="30dp"
	            android:layout_gravity="center_vertical"
	            android:layout_marginStart="5dp"
	            android:layout_marginLeft="5dp"
	            android:padding="6dp"
	            android:src="@drawable/scanner_back_img" />
	
	        <!--标题-->
	        <TextView
	            android:layout_width="match_parent"
	            android:layout_height="wrap_content"
	            android:layout_gravity="center"
	            android:gravity="center"
	            android:text="扫描二维码"
	            android:textColor="@android:color/white"
	            android:textSize="20sp" />
	    </FrameLayout>
	
	    <LinearLayout
	        android:layout_width="wrap_content"
	        android:layout_height="wrap_content"
	        android:layout_gravity="center"
	        android:layout_marginTop="140dp"
	        android:gravity="center"
	        android:orientation="vertical">
	
	        <TextView
	            android:layout_width="match_parent"
	            android:layout_height="wrap_content"
	            android:layout_marginTop="16dp"
	            android:gravity="center_horizontal"
	            android:text="扫一扫"
	            android:textColor="#b3ffffff"
	            android:textSize="18sp" />
	    </LinearLayout>
	
	    <!--闪光灯-->
	    <ImageView
	        android:id="@+id/iv_flash"
	        android:layout_width="30dp"
	        android:layout_height="30dp"
	        android:layout_gravity="bottom|right"
	        android:layout_marginRight="10dp"
	        android:layout_marginBottom="70dp"
	        android:background="@drawable/circle_trans_black"
	        android:padding="5dp"
	        android:src="@drawable/scanner_light" />
	
	    <!--从相册选择-->
	    <ImageView
	        android:id="@+id/iv_album"
	        android:layout_width="30dp"
	        android:layout_height="30dp"
	        android:layout_gravity="bottom|right"
	        android:layout_marginRight="10dp"
	        android:layout_marginBottom="20dp"
	        android:background="@drawable/circle_trans_black"
	        android:padding="5dp"
	        android:src="@drawable/scanner_album" />
	
	    <LinearLayout
	        android:layout_width="wrap_content"
	        android:layout_height="wrap_content"
	        android:layout_gravity="right|center_vertical"
	        android:layout_marginRight="30dp"
	        android:orientation="vertical">
	
	        <!--垂直SeekBar, 用于缩放镜头-->
	        <cn.bertsir.zbar.view.VerticalSeekBar
	            android:id="@+id/vsb_zoom"
	            android:layout_width="wrap_content"
	            android:layout_height="200dp"
	            app:seekBarRotation="CW270" />
	    </LinearLayout>
	</FrameLayout>
</code>

### 1.3.<code>view_scan.xml</code>中自定义View属性介绍
	<!--扫描框的四个角-->
	<cn.bertsir.zbar.view.CornerView
        android:layout_width="20dp"
        android:layout_height="20dp"
        app:cvCornerColor="@color/colorAccent"	//角的颜色, 默认: colorAccent
        app:cvCornerGravity="leftTop"			//画左上角: ┏
        app:cvCornerWidth="5dp" />				//角的厚度, 默认10px(像素)

	<!--扫描View: 网格、雷达、线条-->
	<cn.bertsir.zbar.view.ScanLineView
	    android:id="@+id/iv_scan_line"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    app:slvGriddingDensity="40"				//网格密度, 每一行/列 分成多少个小格子, 默认40(样式是网格才有效)
	    app:slvScanColor="@color/colorAccent"	//扫描颜色, 默认: colorAccent
	    app:slvScanDurationMs="1800"			//扫描时间, 单位ms, 默认1800ms
	    app:slvScanStyle="gridding" />			//扫描样式, 包括:网格, 雷达, 网格+雷达, 线.    默认: 网格


## 2.更多功能请查看原作者
[zBarLibary](https://github.com/actor20170211030627/zBarLibary "zBarLibary"), 感谢
