package com.android.tmall.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Menu 工具类
 */
public class TmallUtil {
    // 日志输出标记
    public static final String TAG = "Menu";

    // -- 简化 Toast code -----------------------
    public static void toast(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    // -- 选项存储 (记录用户的偏好)--------------------------------
    // 定义访问模式
    private static int MODE = Context.MODE_PRIVATE;
    // 定义SharedPreferences名称,该名称和在Android系统中保存的文件同名
    private static final String PREFERENCE_NAME = "MenuSettings";

    /**
     * 是否已成功登录
     *
     * @return
     */
    public static boolean isLogined(Context context) {
        boolean flag = false;
        // 读取选项存储中的用户信息(类似于浏览器中的 Cookie)
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCE_NAME, MODE);

        int id = preferences.getInt("id", 0);
        String username = preferences.getString("username", "");

        if (id > 0 && username.length() > 0) {
            flag = true; // 已登录
        }
        return flag;
    }

    /**
     * 登录成功,保存用户信息到 选项存储
     *
     * @param context
     * @param id
     * @param username
     * @param email
     * @param photopath
     */
    public static void savePreferences(Context context,
                                       int id, String username, String email, String photopath) {
        // 保存用户信息到选项存储
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCE_NAME, MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("id", id);
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("photopath", photopath);
        editor.commit();
    }

    /**
     * 返回当前已登录的用户名
     *
     * @param context
     * @return
     */
    public static String getUsername(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCE_NAME, MODE);
        return preferences.getString("username", "");
    }

    public static String getImgPath(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCE_NAME, MODE);
        return preferences.getString("photopath", "");
    }
}
