package com.oden.syd_camera.utils;

import android.util.Log;

/**
 * Created by syd on 2017/6/16.
 */

public class LogUtils {
    static String className;
    static String methodName;
    static int lineNumber;

    private LogUtils()
    {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isDebug = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化
    private static final String TAG = "SydCamera";

    // 下面四个是默认tag的函数
    public static void i(String msg)
    {
        if (isDebug){
            getMethodNames(new Throwable().getStackTrace());
            Log.i(TAG + className, msg);
        }
    }

    public static void d(String msg)
    {
        if (isDebug){
            getMethodNames(new Throwable().getStackTrace());
            Log.d(TAG + className, msg);
        }
    }

    public static void w(String msg)
    {
        if (isDebug){
            getMethodNames(new Throwable().getStackTrace());
            Log.w(TAG + className, msg);
        }
    }

    public static void e(String msg)
    {
        if (isDebug){
            getMethodNames(new Throwable().getStackTrace());
            Log.e(TAG + className, msg);
        }
    }

    public static void v(String msg)
    {
        if (isDebug){
            getMethodNames(new Throwable().getStackTrace());
            Log.v(TAG + className, msg);
        }
    }

    // 下面是传入自定义tag的函数
    public static void i(String tag, String msg)
    {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg)
    {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void e(String tag, String msg)
    {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void v(String tag, String msg)
    {
        if (isDebug)
            Log.i(tag, msg);
    }


    private static void getMethodNames(StackTraceElement[] sElements) {
        className = " [" + sElements[1].getFileName() + "] ";
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }
}
