package cn.bertsir.zbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.soundcloud.android.crop.Crop;

import java.io.File;

import cn.bertsir.zbar.Qr.ScanResult;
import cn.bertsir.zbar.Qr.Symbol;
import cn.bertsir.zbar.utils.GetPathFromUri;
import cn.bertsir.zbar.utils.QRUtils;

/**
 * Description: 做了修改, 注意:
 * 1.子类的布局中必须有一个 id 为 'camera_preview' 的 CameraPreview
 * 2.都有默认值, 可以不用配置扫描选项
 *
 * Date       : 2019/11/8 on 10:40
 */
public abstract class QRActivity extends AppCompatActivity {

    private static final String        TAG = "QRActivity";
    private              CameraPreview cameraPreview;
    private              SoundPool     soundPool;
//    private              ScanView      sv;
    private              TextView      textDialog;
//    private              QrConfig      options;
    static final         int           REQUEST_IMAGE_GET = 1;
//    static final         int           REQUEST_PHOTO_CUT = 2;
//    public static final  int           RESULT_CANCELED   = 401;
    protected         float            AUTOLIGHTMIN      = 10F;
    private              Uri           uricropFile;
    private              String        cropTempPath      = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "cropQr.jpg";

    private              AlertDialog   progressDialog;
    private              float         oldDist           = 1f;

    //用于检测光线
    private SensorManager sensorManager;
    private Sensor        sensor;

    private boolean isInit = false;

    ////////////////////////////////////////////////////////////////
    //子类可修改的常量
    ////////////////////////////////////////////////////////////////
    protected boolean    isFingerZoom   = true;//是否能手势缩放, 默认true
    protected boolean    isPlaySound    = true;//扫描完成后, 是否要播放声音, 默认true
    protected boolean    isShowVibrator = false;//振动提醒, 默认false
    protected boolean    isNeedCrop     = true;//是否从相册选择后裁剪图片, 默认true
    protected boolean    isAutoLight    = false;//是否自动灯光, 默认false
    protected int        dingPath        = R.raw.qrcode;//扫描成功后, 播放的音乐文件, 默认R.raw.qrcode
    protected String OPEN_ALBUM_TEXT = "选择要识别的图片";//打开相册的文字

    protected Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
        activity = this;
//        options = (QrConfig) getIntent().getExtras().get(QrConfig.EXTRA_THIS_CONFIG);
//        initParm();
//        setContentView(R.layout.activity_qr);
//        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isInit) {
            initParm();
            initView();
            isInit = true;
        }
    }

    /**
     * 初始化参数
     */
    private void initParm() {
//        switch (options.getSCREEN_ORIENTATION()) {
//            case QrConfig.SCREEN_LANDSCAPE:
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                break;
//            case QrConfig.SCREEN_PORTRAIT:
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                break;
//            case QrConfig.SCREEN_SENSOR:
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//                break;
//            default:
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                break;
//        }
        //设置扫码类型, 默认全部（二维码，条形码，全部，自定义，默认为二维码）
        Symbol.scanType = QrConfig.TYPE_ALL/*options.getScan_type()*/;

        //设置扫描的码的具体类型, 此项只有在扫码类型为TYPE_CUSTOM时才有效
        Symbol.scanFormat = QrConfig.BARCODE_EAN13/*options.getCustombarcodeformat()*/;

        //是否只识别框中内容(默认为false: 全屏识别)
        Symbol.is_only_scan_center = false/*options.isOnly_center()*/;

        //是否开启自动缩放, 默认false (实验性功能，不建议使用)
        Symbol.is_auto_zoom = false/*options.isAuto_zoom()*/;

        //是否开启双引擎识别, 默认true (仅对识别二维码有效，并且开启后只识别框内功能将失效)
        Symbol.doubleEngine = true/*options.isDouble_engine()*/;

        //是否连续扫描二维码, 默认true
        Symbol.looperScan = true/*options.isLoop_scan()*/;

        //连续扫描间隔时间, 默认1s
        Symbol.looperWaitTime = 1000/*options.getLoop_wait_time()*/;

        Symbol.screenWidth = QRUtils.getInstance().getScreenWidth(this);
        Symbol.screenHeight = QRUtils.getInstance().getScreenHeight(this);

        int max = Math.max(Symbol.screenWidth, Symbol.screenHeight);
        //裁剪的宽
        Symbol.cropWidth = (int) (max * 0.8);//fl_scan.getWidth()
        //裁剪的高
        Symbol.cropHeight = (int) (max * 0.8);//fl_scan.getHeight()

        //自动灯光
        if (isAutoLight/*options.isAuto_light()*/) {
            getSensorManager();
        }
    }

    /**
     * 初始化布局
     */
    private void initView() {
        cameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
        //bi~
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(this, dingPath/*options.getDing_path()*/, 1);

//        sv = (ScanView) findViewById(R.id.sv);

        //设置扫描类型(条码/二维码)
//        sv.setType(options.getScan_view_type());//QrConfig.SCANVIEW_TYPE_QRCODE

        //4个角颜色
//        sv.setCornerColor(options.getCORNER_COLOR());

        //设置单次扫描时间
//        sv.setLineSpeed(options.getLine_speed());

        //设定扫描的颜色
//        sv.setLineColor(options.getLINE_COLOR());

        //扫描样式(网格, 雷达, 网格+雷达, 线)
//        sv.setScanLineStyle(options.getLine_style());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraPreview != null) {
            cameraPreview.setScanCallback(resultCallback);
            cameraPreview.start();
        }

        if (sensorManager != null) {
            //一般在Resume方法中注册
            /**
             * 第三个参数决定传感器信息更新速度
             * SensorManager.SENSOR_DELAY_NORMAL:一般
             * SENSOR_DELAY_FASTEST:最快
             * SENSOR_DELAY_GAME:比较快,适合游戏
             * SENSOR_DELAY_UI:慢
             */
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * 获取光线传感器
     */
    protected void getSensorManager() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
    }

    /**
     * 切换闪光灯
     */
    protected void setFlash() {
        if (cameraPreview != null) cameraPreview.setFlash();
    }

    /**
     * @param zoom 设置镜头缩放, 取值范围: [0, 1]
     */
    protected void setZoom(@FloatRange(from = 0, to = 1) float zoom) {
        if (cameraPreview != null) cameraPreview.setZoom(zoom);
    }

    /**
     * 设置 SeekBar 颜色
     */
    protected void setSeekBarColor(SeekBar seekBar, @ColorInt int color) {
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * 识别结果回调
     */
    private ScanCallback resultCallback = new ScanCallback() {
        @Override
        public void onScanResult(ScanResult result) {
            //播放声音
            if (isPlaySound/*options.isPlay_sound()*/) {
                soundPool.play(1, 1, 1, 0, 0, 1);
            }
            //震动提醒
            if (isShowVibrator/*options.isShow_vibrator()*/) {
                QRUtils.getInstance().getVibrator(getApplicationContext());
            }

            if (cameraPreview != null) {
                cameraPreview.setFlash(false);
            }
            /*QrManager.getInstance().getResultCallback().*/onScanSuccess(result);
            if (!Symbol.looperScan) {
                onBackPressed();
            }
        }
    };

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float light = event.values[0];
            if (light < AUTOLIGHTMIN) {//暂定值
                if (cameraPreview.isPreviewStart()) {
                    cameraPreview.setFlash(true);
                    sensorManager.unregisterListener(this, sensor);
                    sensor = null;
                    sensorManager = null;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_GET:
                    //是否从相册选择后裁剪图片
                    if (isNeedCrop/*options.isNeed_crop()*/) {
                        cropPhoto(data.getData());
                    } else {
                        recognitionLocation(data.getData());
                    }
                    break;
                case Crop.REQUEST_CROP://裁剪完成
                    recognitionLocation(uricropFile);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 从相册选择图片
     */
    protected void selectImageFromAlbum() {
        if (QRUtils.getInstance().isMIUI()) {//是否是小米设备,是的话用到弹窗选取入口的方法去选取
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(Intent.createChooser(intent, OPEN_ALBUM_TEXT/*options.getOpen_album_text()*/), REQUEST_IMAGE_GET);
        } else {//直接跳到系统相册去选取
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT < 19) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
            } else {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
            }
            startActivityForResult(Intent.createChooser(intent, OPEN_ALBUM_TEXT/*options.getOpen_album_text()*/), REQUEST_IMAGE_GET);
        }
    }

    /**
     * 识别本地图片
     *
     * @param uri
     */
    protected void recognitionLocation(Uri uri) {
        String imagePath = GetPathFromUri.getPath(this, uri);
        recognitionLocation(imagePath);
    }

    /**
     * 识别本地图片
     * @param imagePath 图片地址
     */
    protected void recognitionLocation(final String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            selectImageIsEmpty();
            return;
        }
        textDialog = showProgressDialog();
        textDialog.setText("请稍后...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //优先使用zbar识别一次二维码
                    final String qrcontent = QRUtils.getInstance().decodeQRcode(imagePath);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ScanResult scanResult = new ScanResult();
                            if (!TextUtils.isEmpty(qrcontent)) {
                                closeProgressDialog();
                                scanResult.setContent(qrcontent);
                                scanResult.setType(ScanResult.CODE_QR);
                                /*QrManager.getInstance().getResultCallback().*/onScanSuccess(scanResult);
                                QRUtils.getInstance().deleteTempFile(cropTempPath);//删除裁切的临时文件
                                onBackPressed();
                            } else {
                                //尝试用zxing再试一次识别二维码
                                final String qrcontent = QRUtils.getInstance().decodeQRcodeByZxing(imagePath);
                                if (!TextUtils.isEmpty(qrcontent)) {
                                    closeProgressDialog();
                                    scanResult.setContent(qrcontent);
                                    scanResult.setType(ScanResult.CODE_QR);
                                    /*QrManager.getInstance().getResultCallback().*/onScanSuccess(scanResult);
                                    QRUtils.getInstance().deleteTempFile(cropTempPath);//删除裁切的临时文件
                                    onBackPressed();
                                } else {
                                    //再试试是不是条形码
                                    try {
                                        String barcontent = QRUtils.getInstance().decodeBarcode(imagePath);
                                        if (!TextUtils.isEmpty(barcontent)) {
                                            closeProgressDialog();
                                            scanResult.setContent(barcontent);
                                            scanResult.setType(ScanResult.CODE_BAR);
                                            /*QrManager.getInstance().getResultCallback().*/onScanSuccess(scanResult);
                                            QRUtils.getInstance().deleteTempFile(cropTempPath);//删除裁切的临时文件
                                            onBackPressed();
                                        } else {
                                            recognitionLocalFailure(imagePath);
                                            closeProgressDialog();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        closeProgressDialog();
                                        recognitionLocalException(e);
                                    }

                                }

                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    closeProgressDialog();
                }
            }
        }).start();
    }

    /**
     * 裁切照片
     *
     * @param uri
     */
    protected void cropPhoto(Uri uri) {
        uricropFile = Uri.parse("file://" + "/" + cropTempPath);
        Crop.of(uri, uricropFile).asSquare().start(this);
    }


    protected TextView showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setCancelable(false);
        View view = View.inflate(this, R.layout.dialog_loading, null);
        builder.setView(view);
        ProgressBar pb_loading = view.findViewById(R.id.pb_loading);
        TextView tv_hint = (TextView) view.findViewById(R.id.tv_hint);
        if (Build.VERSION.SDK_INT >= 23) {
            pb_loading.setIndeterminateTintList(getColorStateList(R.color.dialog_pro_color));
        }
        progressDialog = builder.create();
        progressDialog.show();

        return tv_hint;
    }

    protected void closeProgressDialog() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 触摸事件, 缩放镜头
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFingerZoom) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = QRUtils.getInstance().getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 2) {
                        float newDist = QRUtils.getInstance().getFingerSpacing(event);
                        if (newDist > oldDist) {
                            cameraPreview.handleZoom(true);
                        } else if (newDist < oldDist) {
                            cameraPreview.handleZoom(false);
                        }
                        oldDist = newDist;
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    ////////////////////////////////////////////////////////////////
    //子类可重写的4个方法
    ////////////////////////////////////////////////////////////////
    /**
     * @param result 扫描成功
     */
    protected abstract void onScanSuccess(ScanResult result);

    /**
     * 从相册选择图片识别后, 获取到的图片为空(可不用重写此方法)
     * 如果没有选择图片识别的功能, 不用重写这个方法
     */
    protected void selectImageIsEmpty() {
        Log.e(TAG, "selectImageIsEmpty: 选择图片识别, 获取图片失败！");
//        Toast.makeText(getApplicationContext(), "选择图片识别, 获取图片失败！", Toast.LENGTH_SHORT).show();
    }

    /**
     * 从相册选择图片识别后, 识别本地图片失败(可不用重写此方法)
     */
    protected void recognitionLocalFailure(String imagePath) {
        String str = "识别失败！, path: ".concat(imagePath);
        Log.e(TAG, "recognitionLocalFailure: ".concat(str));
//        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param e 从相册选择图片识别后, 识别本地图片异常(可不用重写此方法)
     */
    protected void recognitionLocalException(Exception e) {
//        Toast.makeText(getApplicationContext(), "识别异常！", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraPreview != null) {
            cameraPreview.stop();
        }
        if (sensorManager != null) {
            //解除注册
            sensorManager.unregisterListener(sensorEventListener, sensor);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraPreview != null) {
            cameraPreview.setFlash(false);
            cameraPreview.stop();
        }
        soundPool.release();
    }
}
