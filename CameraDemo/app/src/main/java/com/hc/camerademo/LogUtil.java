package com.hc.camerademo;

import android.util.Log;

public class LogUtil {

    public static void d (String tag,String text) {
        Log.d(tag,text);
    }

    public static void e (String tag,String text) {
        Log.e(tag,text);
    }
}
