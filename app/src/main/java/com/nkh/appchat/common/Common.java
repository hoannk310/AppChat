package com.nkh.appchat.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.nkh.appchat.model.User;

public class Common {
    public static final String IS_FACE_ID = "face_id";
    public static final String KEY_ACCOUNT = "account";
    public static final String SHARED_PREFERENCE_NAME = "SettingGame";
    public static User current_user;

    public static void saveData(Context context, User myObject) {
        SharedPreferences mPrefs = context.getSharedPreferences("", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(myObject);
        prefsEditor.putString(KEY_ACCOUNT, json);
        prefsEditor.commit();
    }

    public static User loadData(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences("", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString(KEY_ACCOUNT, "");
        User obj = gson.fromJson(json, User.class);
        return obj;

    }

    public static boolean getSetting(Context context) {
        SharedPreferences sharedPreferences = context.
                getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean isVolume = sharedPreferences.getBoolean(IS_FACE_ID, false);
        return isVolume;
    }
}
