package com.startup.paynchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceConnector {
    public static final String PREF_NAME = "app_prefrences";
    public static final int MODE = Context.MODE_PRIVATE;

    public static final String ISCHATACTIVE      = "chat";
    public static final String ISAUDIOACTIVE     = "audio";
    public static final String ISVIDEOACTIVE     = "video";
    public static final String LOGINEDUSERID = "loginuserid";
    public static final String LOGINEDNAME = "loginedname";
    public static final String LOGINEDPHONE = "loginedphone";
    public static final String LOGINEDEMAIL = "loginedemail";
    public static final String callingdata="callingdata";
    public static final String ENQSTATUS = "enqstatus";
    public static final String ISINTROSHOWED = "introstatus";
    public static final String WALLETBAL = "wallet_bal";
    public static final String WEBHEADING = "webheading";
    public static final String WEBURL = "weburl";
    public static final String ORDERID = "order_id";

    public static void writeBoolean(Context context, String key, boolean value) {
        getEditor(context).putBoolean(key, value).commit();
    }

    public static boolean readBoolean(Context context, String key, boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    public static void writeInteger(Context context, String key, int value) {
        getEditor(context).putInt(key, value).commit();
    }

    public static int readInteger(Context context, String key, int defValue) {
        return getPreferences(context).getInt(key, defValue);
    }

    public static void writeString(Context context, String key, String value) {
        getEditor(context).putString(key, value).commit();
    }

    public static String readString(Context context, String key, String defValue) {
        return getPreferences(context).getString(key, defValue);
    }

    public static void writeFloat(Context context, String key, float value) {
        getEditor(context).putFloat(key, value).commit();
    }

    public static float readFloat(Context context, String key, float defValue) {
        return getPreferences(context).getFloat(key, defValue);
    }

    public static void writeLong(Context context, String key, long value) {
        getEditor(context).putLong(key, value).commit();
    }

    public static long readLong(Context context, String key, long defValue) {
        return getPreferences(context).getLong(key, defValue);
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, MODE);
    }

    public static Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    public static void cleanPrefrences(Context context) {
        getPreferences(context).edit().clear().commit();
    }
}
