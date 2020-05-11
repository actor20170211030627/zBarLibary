package com.actor.zbarlibaryTest.global;

import android.support.annotation.NonNull;

import com.actor.myandroidframework.application.ActorApplication;
import com.zhouyou.http.EasyHttp;
import com.zhy.http.okhttp.OkHttpUtils;

/**
 * description: 类的描述
 *
 * @author : 李大发
 * date       : 2020/5/11 on 22:24
 * @version 1.0
 */
public class MyApplication extends ActorApplication {

    @Override
    protected void configEasyHttp(EasyHttp easyHttp) {
        //配置张鸿洋的OkHttpUtils
        OkHttpUtils.initClient(EasyHttp.getOkHttpClient());
    }

    @NonNull
    @Override
    protected String getBaseUrl() {
        return null;
    }

    @Override
    protected void onUncaughtException(Thread thread, Throwable e) {

    }
}
