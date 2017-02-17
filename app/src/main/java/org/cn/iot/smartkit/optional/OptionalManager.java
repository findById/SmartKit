package org.cn.iot.smartkit.optional;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class OptionalManager {

    private static Context mContext;

    public static void init(Context ctx) {
        mContext = ctx;
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        if (TextUtils.isEmpty(defaultValue)) {
            defaultValue = "";
        }
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString(key, defaultValue);
    }

    public static void put(String key, Object value) {
        if (value instanceof Boolean) {
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(key, Boolean.valueOf(String.valueOf(value))).commit();
        } else if (value instanceof Integer) {
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(key, Integer.valueOf(String.valueOf(value))).commit();
        } else {
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(key, String.valueOf(value)).commit();
        }

    }

}
