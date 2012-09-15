package com.orisider.oneclickgoogd;

import android.app.Application;
import android.content.Context;

public class GoogDrvShareApp extends Application {

    public static Context ctx;

    @Override
    public void onCreate() {
        super.onCreate();

        ctx = getApplicationContext();
    }
}
