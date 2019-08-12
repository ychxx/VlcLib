package org.videolan.libvlc.util;

import android.app.Activity;
import android.util.Log;

/**
 * Created by Lenovo on 2017/2/22.
 */

public final class VlcLog {
    private static boolean isLogShow = true;

    public static void d(Activity activity, String msg) {
        if (isLogShow) {
            d(activity.getClass().getName(), msg);
        }
    }

    public static void i(Activity activity, String msg) {
        if (isLogShow) {
            i(activity.getClass().getName(), msg);
        }
    }

    public static void w(Activity activity, String msg) {
        if (isLogShow) {
            w(activity.getClass().getName(), msg);
        }
    }

    public static void e(Activity activity, String msg) {
        if (isLogShow) {
            e(activity.getClass().getName(), msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isLogShow) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isLogShow) {
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (isLogShow) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isLogShow) {
            Log.w(tag, msg);
        }
    }
}
