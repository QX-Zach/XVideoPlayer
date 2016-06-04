package com.ozner.xvidoeplayer;

import android.app.Activity;

/**
 * Created by ozner_67 on 2016/4/19.
 */
public class DataRequest {
    public static String getWeather(final Activity activity,HttpCallback callback) {
        String url = "http://app.ozner.net:888/OznerServer/GetWeather";

        return null;
    }

    public interface HttpCallback {
        void onSuccess(String result);

        void onFail();
    }
}
