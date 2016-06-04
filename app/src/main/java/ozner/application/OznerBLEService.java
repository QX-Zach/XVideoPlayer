package ozner.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import ozner.XObject;
import ozner.device.OznerDeviceManager;

import java.util.List;

public class OznerBLEService extends Service implements ActivityLifecycleCallbacks {
    public static final String ACTION_ServiceInit = "ozner.service.init";
    static OznerDeviceManager mManager;
    OznerBLEBinder binder = new OznerBLEBinder();
    Handler handler = new Handler();
    PowerManager powerManager;

    public OznerBLEService() {
    }

    public void checkBackgroundMode(boolean now) {
        ActivityManager activityManager = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    if (now) {
                        XObject.setRunningMode(getApplicationContext(), XObject.RunningMode.Background);
                    } else {
                        delayedCheck();
                    }
                } else {
                    if (powerManager.isScreenOn()) //当屏幕是灭的时延时10秒在判断,如果还是熄灭的进入后台模式
                    {
                        XObject.setRunningMode(getApplicationContext(), XObject.RunningMode.Foreground);
                    } else {
                        if (now) {
                            XObject.setRunningMode(getApplicationContext(),
                                    powerManager.isScreenOn() ?
                                            XObject.RunningMode.Foreground : XObject.RunningMode.Background);
                        } else {
                            delayedCheck();
                        }

                        return;
                    }

                }
            }
        }


    }

    private void delayedCheck() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkBackgroundMode(true);
            }
        }, 10000);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {

            mManager = new OznerDeviceManager(getApplicationContext());
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        this.getApplication().registerActivityLifecycleCallbacks(this);
        XObject.setRunningMode(getApplicationContext(), XObject.RunningMode.Foreground);
        //BluetoothWorkThread work=new BluetoothWorkThread(getApplicationContext());
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.getApplication().unregisterActivityLifecycleCallbacks(this);
        return super.onUnbind(intent);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        checkBackgroundMode(false);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        checkBackgroundMode(false);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        checkBackgroundMode(false);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        checkBackgroundMode(false);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        checkBackgroundMode(false);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        checkBackgroundMode(false);
    }

    public class OznerBLEBinder extends Binder {
        /**
         * 获取设备管理器
         *
         * @return
         */
        public OznerDeviceManager getDeviceManager() {
            return mManager;
        }


    }

}
