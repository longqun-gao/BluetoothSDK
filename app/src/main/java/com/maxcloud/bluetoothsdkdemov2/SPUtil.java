package com.maxcloud.bluetoothsdkdemov2;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 描    述：
 * 作    者：AleX.D.Tu
 * 时    间：2017/11/3
 * 版    权：迈斯云门禁网络科技有限公司
 */

public class SPUtil {
    static public void saveIpAddress(Context context, String ipAddr) {
        SharedPreferences sp = context.getSharedPreferences("Config", Application.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("IpAddress", ipAddr);
        editor.apply();
    }

    static public String getIpAddress(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Config", Application.MODE_PRIVATE);
        return sp.getString("IpAddress", "10.1.1.203");
    }

    static public void savePort(Context context, String port) {
        SharedPreferences sp = context.getSharedPreferences("Config", Application.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Port", port);
        editor.apply();
    }

    static public String getPort(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Config", Application.MODE_PRIVATE);
        return sp.getString("Port", "19990");
    }
}
