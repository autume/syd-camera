package com.oden.camera_test.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by syd
 */

public class MyApplication extends Application{
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}
