package ozner.device;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhiyongxu on 15/12/21.
 */
public abstract class AutoUpdateClass {
    Timer autoUpdateTimer = null;
    private long period;
    public long period()
    {
        return period;
    }

    public AutoUpdateClass(long period)
    {
        this.period=period;
    }

    public void start(long delay)
    {
        if (autoUpdateTimer != null)
            stop();
        autoUpdateTimer = new Timer();
        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                doTime();
            }
        }, delay, this.period);
    }
    protected abstract void doTime();

    public void stop()
    {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer.purge();
            autoUpdateTimer = null;
        }
    }

}
