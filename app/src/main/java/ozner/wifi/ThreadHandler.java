package ozner.wifi;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by zhiyongxu on 16/5/12.
 */
public class ThreadHandler extends Handler {

    static class LoopObject {
        Looper looper;
    }

    static Looper newLooper() {
        final LoopObject obj = new LoopObject();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                obj.looper = Looper.myLooper();
                synchronized (obj) {
                    obj.notify();
                }
                Looper.loop();
            }
        });

        thread.start();
        synchronized (obj) {
            try {
                obj.wait();
                return obj.looper;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public ThreadHandler() {
        super(newLooper());
    }

    public void close() {
        this.getLooper().quit();

    }
}
