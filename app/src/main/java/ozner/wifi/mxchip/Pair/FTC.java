package ozner.wifi.mxchip.Pair;

import android.content.Context;
import android.util.Log;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhiyongxu on 15/11/4.
 */
public class FTC {

    private static ServiceThread service;
    private static boolean listening;
    private static ServerSocket server = null;
    Context context;
    FTC_Listener listener;
    private Thread listen;


    public FTC(Context context, FTC_Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public boolean startListen() {
        listening = true;
        if (null == server) {
            try {
                server = new ServerSocket();
                server.setReuseAddress(true);
                server.bind(new InetSocketAddress(8000));
                // server.setSoTimeout(0);
            } catch (Exception e1) {
                e1.printStackTrace();
                return false;
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                listen = new Thread(new MyService());
                listen.start();
            }
        }).start();
        return true;
    }

    public void stop() {
        listening = false;
        try {
            if (null != server) {
                server.close();
                server = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyService implements Runnable {
        // ���屣�����е�Socket
        public final List<Socket> socketList = new ArrayList<Socket>();
        private Thread t;

        public MyService() {

        }

        public void run() {
            while (listening == true) {
                Socket s = null;
                try {
                    s = server.accept();
                    if (s != null) {
                        Log.e("client", "connectStatus!!");
                        socketList.add(s);
                        service = new ServiceThread(s, listener);
                        t = new Thread(service);
                        t.start();
                    } else
                        System.out
                                .println("------------socket s = null--------------");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
