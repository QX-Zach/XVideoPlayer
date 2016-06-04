package ozner.device;

import android.content.Context;
import android.content.Intent;

import ozner.XObject;
import ozner.device.DeviceNotReadyException;
import ozner.device.OperateCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhiyongxu on 15/10/28.
 * IO基类
 */
public abstract class BaseDeviceIO extends XObject {

    /**
     * 开始连接设备事件
     */
    public final static String ACTION_DEVICE_CONNECTING = "ozner.device.connecting";
    /**
     * 设备连接并初始化完成事件
     */
    public final static String ACTION_DEVICE_CONNECTED = "ozner.device.connected";
    /**
     * 设备连接断开事件
     */
    public final static String ACTION_DEVICE_DISCONNECTED = "ozner.device.disconnected";


    public final static String Extra_Address = "Address";
    final ArrayList<StatusCallback> statusCallback = new ArrayList<>();
    String Type = "";
    boolean isReady = false;
    OnInitCallback onInitCallback = null;
    CheckTransmissionsCompleteCallback checkTransmissionsCompleteCallback = null;
    OnTransmissionsCallback onTransmissionsCallback = null;
    byte[] lastRecvPacket = null;

    public BaseDeviceIO(Context context, String Type) {
        super(context);
        this.Type = Type;
    }
    public String name;
    public String getType() {
        return this.Type;
    }

    public boolean isReady() {
        return isReady;
    }
    public void clearLastRecvPacket()
    {
        synchronized (this)
        {
            lastRecvPacket=null;
        }
    }

    /**
     * 获取最后一次收到的数据包
     */
    public byte[] getLastRecvPacket() {
        synchronized (this)
        {
            if (lastRecvPacket!=null)
            {
                return Arrays.copyOf(lastRecvPacket,lastRecvPacket.length);
            }else
                return null;
        }
    }
    /**
     * 异步方法
     *
     * @param bytes 发送的数据
     * @return 返回TRUE进入队列, FALSE加入操作队列失败
     */
    public abstract boolean send(byte[] bytes);

    /**
     * 异步方法,带回调返回
     *
     * @param bytes    发送的数据
     * @param callback 在发送成功以后调用onSuccess,失败或者异常时调用onFailure
     * @return 返回TRUE进入队列, FALSE加入操作队列失败
     */
    public abstract boolean send(byte[] bytes, OperateCallback<Void> callback);

    public abstract void close();

    public abstract void open() throws DeviceNotReadyException;

    public abstract ConnectStatus connectStatus();

    /**
     * 在后台模式调用完成doReady以后会调用doComplete来检查当前传输是否完成,没有继续等待,完成以后关闭连接
     */
    public void setCheckTransmissionsCompleteCallback(CheckTransmissionsCompleteCallback cb) {
        checkTransmissionsCompleteCallback = cb;
    }

    /**
     * 设置数据传输监听回调
     *
     * @param cb 完成状态回调
     */
    public void setOnTransmissionsCallback(OnTransmissionsCallback cb) {
        onTransmissionsCallback = cb;
    }

    /**
     * 在后台模式调用完成doReady以后会调用doComplete来检查当前传输是否完成,没有继续等待,完成以后关闭连接
     */
    protected boolean doCheckTransmissionsComplete() {
        return checkTransmissionsCompleteCallback == null || checkTransmissionsCompleteCallback.CheckTransmissionsComplete(this);
    }

    /**
     * 调用数据监听回调
     */
    protected void doSend(byte[] bytes) {
        if (onTransmissionsCallback != null) {
            try {
                onTransmissionsCallback.onIOSend(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 调用数据监听回调
     */
    protected void doRecv(byte[] bytes) {
        synchronized (this) {
            lastRecvPacket = bytes;
        }
        if (onTransmissionsCallback != null) {
            try {
                onTransmissionsCallback.onIORecv(bytes);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerStatusCallback(StatusCallback callback) {
        synchronized (statusCallback) {
            if (!statusCallback.contains(callback))
                statusCallback.add(callback);
        }
    }

    public void unRegisterStatusCallback(StatusCallback callback) {
        synchronized (statusCallback) {
            statusCallback.remove(callback);
        }
    }

    protected void doConnecting() {
        Intent intent = new Intent(ACTION_DEVICE_CONNECTING);
        intent.putExtra(Extra_Address, getAddress());
        context().sendBroadcast(intent);
    }

    protected void doConnected() {
        isReady = false;
        synchronized (statusCallback) {
            for (StatusCallback cb : statusCallback)
                cb.onConnected(this);
        }

        Intent intent = new Intent(ACTION_DEVICE_CONNECTING);
        intent.putExtra(Extra_Address, getAddress());
        context().sendBroadcast(intent);
    }

    protected void doDisconnected() {
        isReady = false;
        synchronized (statusCallback) {
            List<StatusCallback> list = new ArrayList<>(statusCallback);
            for (StatusCallback cb : list)
                cb.onDisconnected(this);
        }
        Intent intent = new Intent(ACTION_DEVICE_DISCONNECTED);
        intent.putExtra(Extra_Address, getAddress());
        context().sendBroadcast(intent);
    }

    protected void doReady() {
        isReady = true;
        synchronized (statusCallback) {
            for (StatusCallback cb : statusCallback)
                cb.onReady(this);
        }

        Intent intent = new Intent(ACTION_DEVICE_CONNECTED);
        intent.putExtra(Extra_Address, getAddress());
        context().sendBroadcast(intent);
    }

    /**
     * 蓝牙连接初始化完成时的回调
     */
    public void setOnInitCallback(OnInitCallback onInitCallback) {
        this.onInitCallback = onInitCallback;
    }

    protected boolean doInit() {
        return onInitCallback == null || onInitCallback.onIOInit();
    }


    public abstract String getAddress();

    public enum ConnectStatus {Connecting, Connected, Disconnect}


    public interface StatusCallback {
        void onConnected(ozner.device.BaseDeviceIO io);

        void onDisconnected(ozner.device.BaseDeviceIO io);

        void onReady(ozner.device.BaseDeviceIO io);
    }

    /**
     * 传送检查回调
     */
    public interface CheckTransmissionsCompleteCallback {
        /**
         * 在后台模式调用完成doReady以后会调用doComplete来检查当前传输是否完成,没有继续等待,完成以后关闭连接
         *
         * @return true 传送完成,可以断开连接
         */
        boolean CheckTransmissionsComplete(ozner.device.BaseDeviceIO io);
    }

    /**
     * 数据传输监听回调
     */
    public interface OnTransmissionsCallback {
        /**
         * 接口发送数据时调用该回调
         *
         * @param bytes 已发送的数据
         */
        void onIOSend(byte[] bytes);

        /**
         * 接口收到数据时调用
         *
         * @param bytes 收到的数据
         */
        void onIORecv(byte[] bytes);
    }


    public interface OnInitCallback {
        /**
         * 连接完成以后,通过回调来继续初始化操作
         *
         * @return 返回FALSE初始化失败
         */
        boolean onIOInit();
    }




}
