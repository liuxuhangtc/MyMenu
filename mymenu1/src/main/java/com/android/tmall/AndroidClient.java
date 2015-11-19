package com.android.tmall;

import com.android.tmall.util.VolleyUtil;

/**
 * Created by Song on 2015/8/14.
 */
public class AndroidClient {
    public static final String VOLLEY_TAG = VolleyUtil.class.getSimpleName();
    public static final String BASE_URL = "http://192.168.43.126:8087/Menu/";


    public static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
