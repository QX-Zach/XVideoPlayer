package com.ozner.xvidoeplayer;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by ozner_67 on 2016/4/20.
 */
public class VideoBaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "900026637", false);
    }
}
