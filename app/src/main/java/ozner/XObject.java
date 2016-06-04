package ozner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import ozner.util.dbg;

/**
 * Created by xzyxd on 2015/10/30.
 * 基本对象
 */
public abstract class XObject {
    private static final String ACTION_RUNNING_MODE_CHANGE = "ozner.XObject.RunningModeChange";
    private static RunningMode runningMode = RunningMode.Foreground;
    private final StatusMonitor statusMonitor = new StatusMonitor();
    private final Object waitObject = new Object();
    private Context context;

    public XObject(Context context) {
        this.context = context;
        context.registerReceiver(statusMonitor, new IntentFilter(ACTION_RUNNING_MODE_CHANGE));
    }

    /**
     * 设置运行模式
     */
    public static void setRunningMode(Context context, RunningMode runningMode) {
        if (ozner.XObject.runningMode != runningMode) {
            ozner.XObject.runningMode = runningMode;
            context.sendBroadcast(new Intent(ACTION_RUNNING_MODE_CHANGE));
            dbg.i("设置运行模式:" + runningMode.toString());
        }

    }

    public static RunningMode getRunningMode() {
        return ozner.XObject.runningMode;
    }

    public final Context context() {
        return context;
    }

    @Override
    protected void finalize() throws Throwable {
        context.unregisterReceiver(statusMonitor);
        super.finalize();
    }

    protected void doChangeRunningMode() {

    }

    protected void waitObject(int time) throws InterruptedException {
        synchronized (waitObject) {
            waitObject.wait(time);
        }
    }

    protected void setObject() {
        synchronized (waitObject) {
            waitObject.notify();
        }
    }

    public enum RunningMode {Background, Foreground}

    private class StatusMonitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_RUNNING_MODE_CHANGE)) {
                doChangeRunningMode();
            }
        }
    }


}
