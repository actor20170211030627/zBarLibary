package cn.bertsir.zbar;

import android.app.Activity;
import android.content.Intent;

import cn.bertsir.zbar.Qr.ScanResult;

/**
 * Created by Bert on 2017/9/22.
 */

public class QrManager {

    private static QrManager instance;
    private        QrConfig  options;

    public OnScanResultCallback resultCallback;

    public synchronized static QrManager getInstance() {
        if(instance == null)
            instance = new QrManager();
        return instance;
    }

    public OnScanResultCallback getResultCallback() {
        return resultCallback;
    }


    public QrManager init(QrConfig options) {
        this.options = options;
        return this;
    }

    public void startScan(final Activity activity, OnScanResultCallback resultCall){

        if (options == null) {
            options = new QrConfig.Builder().create();
        }


        //edited 权限在外面页面自己判断, 这儿不判断
//        PermissionUtils.permission(activity, PermissionConstants.CAMERA, PermissionConstants.STORAGE)
//                .rationale(new PermissionUtils.OnRationaleListener() {
//                    @Override
//                    public void rationale(final ShouldRequest shouldRequest) {
//                        shouldRequest.again(true);
//                    }
//                })
//                .callback(new PermissionUtils.FullCallback() {
//                    @Override
//                    public void onGranted(List<String> permissionsGranted) {
                        Intent intent = new Intent(activity, QRActivity.class);
                        intent.putExtra(QrConfig.EXTRA_THIS_CONFIG, options);
                        activity.startActivity(intent);
//                    }
//
//                    @Override
//                    public void onDenied(List<String> permissionsDeniedForever,
//                                         List<String> permissionsDenied) {
//                        Toast.makeText(activity,"摄像头权限被拒绝！", Toast.LENGTH_SHORT).show();
//
//                    }
//                }).request();



        // 绑定图片接口回调函数事件
        resultCallback = resultCall;
    }



    public interface OnScanResultCallback {
        /**
         * 处理成功
         * 多选
         *
         * @param result
         */
        void onScanSuccess(ScanResult result);

    }
}
