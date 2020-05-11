package com.actor.zbarlibaryTest.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actor.zbarlibaryTest.R;
import com.actor.zbarlibaryTest.service.CheckUpdateService;

import java.util.List;

import cn.bertsir.zbar.utils.PermissionConstants;
import cn.bertsir.zbar.utils.PermissionUtils;
import cn.bertsir.zbar.utils.QRUtils;

public class Main2Activity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "Main2Activity";

    private TextView tvVersion;
    private ImageView iv_qr;
    private EditText  et_qr_content;
    private CheckBox  cb_create_add_water;

    private Activity  activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        activity = this;

        iv_qr = findViewById(R.id.iv_qr);
        et_qr_content = findViewById(R.id.et_qr_content);
        cb_create_add_water = findViewById(R.id.cb_create_add_water);
        findViewById(R.id.bt_scan).setOnClickListener(this);
        findViewById(R.id.bt_make).setOnClickListener(this);
        tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText("最后更新时间: 2019-10-16 (1.4.0)");

        iv_qr.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String s = null;
                try {
                    s = QRUtils.getInstance().decodeQRcode(iv_qr);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "onLongClickException: " + e.toString());
                }
                Toast.makeText(getApplicationContext(), "内容：" + s, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        startService(new Intent(this, CheckUpdateService.class));//检查更新
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_scan://扫描二维码
                start();
                break;
            case R.id.bt_make://生成二维码
                Bitmap qrCode;
                if (cb_create_add_water.isChecked()) {//生成二维码添加logo
                    qrCode = QRUtils.getInstance().createQRCodeAddLogo(et_qr_content.getText().toString(),
                            BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
                } else {
                    qrCode = QRUtils.getInstance().createQRCode(et_qr_content.getText().toString());

                }
                iv_qr.setImageBitmap(qrCode);
                Toast.makeText(getApplicationContext(), "长按可识别", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void start() {
        PermissionUtils.permission(activity, PermissionConstants.CAMERA, PermissionConstants.STORAGE)
                .rationale(new PermissionUtils.OnRationaleListener() {
                    @Override
                    public void rationale(final ShouldRequest shouldRequest) {
                        shouldRequest.again(true);
                    }
                })
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        Intent intent = new Intent(activity, TestCustomScanActivity.class);
                        activity.startActivity(intent);
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever,
                                         List<String> permissionsDenied) {
                        Toast.makeText(activity,"摄像头权限被拒绝！", Toast.LENGTH_SHORT).show();

                    }
                }).request();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, CheckUpdateService.class));
    }
}
