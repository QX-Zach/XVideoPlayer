package ozner.util;

import android.util.Log;

public class dbg {

    static IDbgMessage mMessageListener = null;
    static String tag = "oznerDBG";

    public static void i(String msg) {
        Log.i(tag, msg);
        if (mMessageListener != null) {
            mMessageListener.OnMessage(msg);
        }
    }

    public static void i(String msg, Object... args) {
        String m = String.format(msg, args);
        Log.i(tag, m);
        if (mMessageListener != null) {
            mMessageListener.OnMessage(m);
        }
    }

    public static void e(String msg) {
        Log.e(tag, msg);
        if (mMessageListener != null) {
            mMessageListener.OnMessage(msg);
        }
    }

    public static void e(String msg, Object... args) {
        String m = String.format(msg, args);
        Log.e(tag, m);
        if (mMessageListener != null) {
            mMessageListener.OnMessage(m);
        }
    }

    public static void d(String msg) {
        Log.d(tag, msg);
        if (mMessageListener != null) {
            mMessageListener.OnMessage(msg);
        }
    }

    public static void setMessageListener(IDbgMessage messageListener) {
        mMessageListener = messageListener;
    }

    public static void d(String msg, Object... args) {
        String m = String.format(msg, args);

        Log.d(tag, String.format(msg, args));

        if (mMessageListener != null) {
            mMessageListener.OnMessage(m);
        }
    }

    public static void w(String msg) {
        Log.w(tag, msg);
    }

    public static void w(String msg, Object... args) {
        Log.w(tag, String.format(msg, args));
    }

    public interface IDbgMessage {
        void OnMessage(String message);
    }

}
