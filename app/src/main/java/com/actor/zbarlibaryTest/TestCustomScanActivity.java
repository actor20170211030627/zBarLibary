package com.actor.zbarlibaryTest;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import cn.bertsir.zbar.QRActivity;
import cn.bertsir.zbar.Qr.ScanResult;
import cn.bertsir.zbar.Qr.Symbol;
import cn.bertsir.zbar.QrConfig;
import cn.bertsir.zbar.view.VerticalSeekBar;

/**
 * Description: 测试二维码扫描
 * Company    : 重庆市了赢科技有限公司 http://www.liaoin.com/
 * Author     : 李大发
 * Date       : 2019/11/8 on 11:27
 */
public class TestCustomScanActivity extends QRActivity implements View.OnClickListener {

    private VerticalSeekBar vsb_zoom;
//    private ImageView ivFlash;
    private ImageView ivLight;
    private static final String TAG = "TestCustomScanActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        //配置扫描, 要在setContentView()之前(有默认值, 都可以不配置)
        isFingerZoom = true;//是否能手势缩放, 默认true
        isPlaySound    = true;//扫描完成后, 是否要播放声音, 默认true
        isShowVibrator = false;//振动提醒, 默认false
        isNeedCrop     = true;//是否从相册选择后裁剪图片, 默认true
        isAutoLight    = false;//是否自动灯光, 默认false
        QrConfig.ding_path = R.raw.beep_di;//扫描成功后, 播放的音乐文件, 默认R.raw.beep_di
        OPEN_ALBUM_TEXT = "选择要识别的图片";//打开相册的文字
        //设置扫码类型, 默认全部（二维码，条形码，全部，自定义，默认为二维码）
        Symbol.scanType = QrConfig.TYPE_QRCODE;
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

        //布局中必须有一个 id 为 'camera_preview' 的 CameraPreview
        setContentView(R.layout.activity_test_custom_scan);

        vsb_zoom = findViewById(R.id.vsb_zoom);
//        ivFlash = findViewById(R.id.iv_flash);
        ivLight = findViewById(R.id.iv_light);
        findViewById(R.id.iv_album).setOnClickListener(this);
//        ivFlash.setOnClickListener(this);
        ivLight.setOnClickListener(this);


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
    protected void onResume() {
        super.onResume();
        startScan();
//        ivFlash.setSelected(isFlashOpen());//设置图片状态
        ivLight.setSelected(isFlashOpen());//设置图片状态
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
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
//            case R.id.iv_flash://闪光灯
            case R.id.iv_light://闪光灯
//                setFlash();
                boolean selected = v.isSelected();
                v.setSelected(!selected);
                setFlash(!selected);
                break;
        }
    }
}
