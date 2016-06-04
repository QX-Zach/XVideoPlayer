package ozner.wifi.mxchip;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.fusesource.mqtt.client.Callback;

import ozner.device.BaseDeviceIO;
import ozner.device.DeviceNotReadyException;
import ozner.device.OperateCallback;
import ozner.util.Helper;

/**
 * Created by xzyxd on 2015/10/31.
 */
public class MXChipIO extends BaseDeviceIO {
    final MXChipIOImp mxChipIOImp = new MXChipIOImp();
    String address = "";
    MQTTProxy proxy;
    String out = null;
    String in = null;

    public MXChipIO(Context context, MQTTProxy proxy, String address,String Type) {
        super(context, Type);
        this.address = address;
        this.proxy = proxy;
        proxy.registerListener(mxChipIOImp);

    }
    public void startScan()
    {

    }


    /**
     * 设置MQTT设备的分类ID,每个庆科设备都有一个"分类ID/MAC"组成的MQTT订阅主题,在订阅消息前调用
     */
    public void setSecureCode(String secureCode) {
        in = secureCode + "/" + address.replace(":", "").toLowerCase() + "/in";
        out = secureCode + "/" + address.replace(":", "").toLowerCase() + "/out";
    }

    @Override
    public boolean send(byte[] bytes) {
        return mxChipIOImp.postSend(bytes, null);
    }


    @Override
    public boolean send(byte[] bytes, OperateCallback<Void> callback) {
        return mxChipIOImp.postSend(bytes, callback);
    }

    @Override
    public void close() {
        mxChipIOImp.close();
    }

    @Override
    public void open() throws DeviceNotReadyException {
        if (Helper.StringIsNullOrEmpty(out))
            throw new DeviceNotReadyException();
        mxChipIOImp.start();
    }

    /**
     * 设置一个循环发送runnable,来执行发送大数据包,比如挂件升级过程
     */
    public boolean post(MXChipRunnable runnable) {
        return mxChipIOImp.postRunable(runnable);
    }

    @Override
    public ConnectStatus connectStatus() {
        if (proxy.connected) {
            if (isReady())
                return ConnectStatus.Connected;
            else
                return ConnectStatus.Connecting;
        } else
            return ConnectStatus.Disconnect;
    }


    @Override
    public String getAddress() {
        return address;
    }


    public interface MXChipRunnable {
        void run();
    }

    class MXChipIOImp implements MQTTProxy.MQTTListener, Runnable {
        final static int MSG_SendData = 0x1000;
        final static int MSG_Runnable = 0x2000;
        final static int Timeout = 10000;
        Thread thread = null;
        Looper looper = null;
        MessageHandler handler = null;
        private boolean succeed = false;

        private boolean send(byte[] data) throws InterruptedException {
            if (data == null) return false;
            try {
                succeed = false;

                proxy.publish(in, data, new OperateCallback<Void>() {
                    @Override
                    public void onSuccess(Void var1) {
                        succeed = true;
                        setObject();
                    }

                    @Override
                    public void onFailure(Throwable var1) {
                        succeed = false;
                        setObject();
                    }
                });
                if (!succeed)
                    waitObject(Timeout);
                if (succeed) {
                    doSend(data);
                }
                return succeed;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void onConnected(MQTTProxy proxy) {

        }

        @Override
        public void onDisconnected(MQTTProxy proxy) {
            close();
        }

        @Override
        public void onPublish(MQTTProxy proxy, String topic, byte[] data) {
            if (topic.equals(out)) {
                doRecv(data);
            }
        }

        /**
         * 设置一个循环发送runnable,来执行发送大数据包,比如挂件升级过程
         */
        public boolean postRunable(MXChipRunnable runnable) {
            if (handler == null) return false;
            Message message = new Message();
            message.what = MSG_Runnable;
            message.obj = runnable;
            return handler.sendMessage(message);
        }

        public boolean postSend(byte[] data, OperateCallback<Void> callback) {
            if (Thread.currentThread().getId() == thread.getId()) {
                try {

                    if (send(data)) {
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                        return true;
                    } else {
                        if (callback != null) {
                            callback.onFailure(null);
                        }
                        return false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(e);
                    return false;
                }
            } else {
                if (handler != null) {
                    Message message = new Message();
                    message.what = MSG_SendData;
                    message.obj = new AsyncObject(data, callback);
                    return handler.sendMessage(message);
                } else
                    return false;
            }

        }

        public boolean isRuning() {
            return thread != null && thread.isAlive();
        }

        public void start() throws DeviceNotReadyException {
            if (isRuning()) {
                throw new DeviceNotReadyException();
            }
            thread = new Thread(this);
            thread.start();
        }

        public void close() {
            if (isRuning()) {

                proxy.unregisterListener(mxChipIOImp);
                setObject();
                if (looper != null)
                    looper.quit();
                thread.interrupt();
            }
        }

        @Override
        public void run() {
            try {
                doConnecting();
                if (!proxy.isConnected()) return;
                try {
                    succeed = false;
                    proxy.subscribe(out, new Callback<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            succeed = true;
                            setObject();
                        }
                        @Override
                        public void onFailure(Throwable throwable) {
                            succeed = false;
                            setObject();
                        }
                    });
                    waitObject(10000);
                    if (!succeed) return;

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                Looper.prepare();
                looper = Looper.myLooper();
                handler = new MessageHandler(looper);
                if (!doInit()) {
                    return;
                }
                doReady();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                proxy.unsubscribe(out);
                doDisconnected();
            }
        }

        class MessageHandler extends Handler {
            public MessageHandler(Looper looper) {
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                try {
                    if (msg.what == MSG_SendData) {
                        AsyncObject object = (AsyncObject) msg.obj;
                        try {
                            if (send(object.data)) {
                                if (object.callback != null)
                                    object.callback.onSuccess(null);
                            } else {
                                if (object.callback != null)
                                    object.callback.onFailure(null);
                            }
                        } catch (Exception e) {
                            if (object.callback != null)
                                object.callback.onFailure(e);
                            throw e;
                        }

                    } else if (msg.what == MSG_Runnable) {
                        MXChipRunnable runnable = (MXChipRunnable) msg.obj;
                        runnable.run();
                    }

                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.handleMessage(msg);
            }
        }

        class AsyncObject {
            public byte[] data;
            public OperateCallback<Void> callback;

            public AsyncObject(byte[] data, OperateCallback<Void> callback) {
                this.data = data;
                this.callback = callback;
            }

        }
    }
}
