package ozner.wifi.mxchip;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mxchip.jmdns.JmdnsAPI;
import com.mxchip.jmdns.JmdnsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ozner.device.BaseDeviceIO;
import ozner.device.IOManager;
import ozner.device.OznerDeviceManager;


/**
 * Created by xzyxd on 2015/10/31.
 */
public class MXChipIOManager extends IOManager {
    /**
     * 找到设备广播,附加设备的MAC地址
     */
    public final static String ACTION_SCANNER_FOUND = "com.ozner.mxchip.scanner.found";

    HashMap<String, String> listenDeviceList = new HashMap<>();

    final MQTTProxyImp mqttProxyImp = new MQTTProxyImp();
    MQTTProxy proxy;

    MXChipScanImp mxChipScanImp;

    public MXChipIOManager(Context context) {
        super(context);
        proxy = new MQTTProxy(context);
        proxy.registerListener(mqttProxyImp);
        mxChipScanImp = new MXChipScanImp(context);
    }


    public MXChipIO createMXChipDevice(String address, String type) throws ClassCastException {
        synchronized (listenDeviceList) {
            if (!listenDeviceList.containsKey(address))
                listenDeviceList.put(address, type);
        }

        if (proxy.isConnected()) {
            MXChipIO io = new MXChipIO(context(), proxy, address, type);
            doAvailable(io);
            return io;
        } else
            return null;

    }

    public void startScan() {
        mxChipScanImp.startScan();
    }

    public void stopScan() {
        mxChipScanImp.stopScan();
    }

    class MXChipScanImp implements JmdnsListener {
        JmdnsAPI mdnsApi = null;

        public MXChipScanImp(Context context) {
            mdnsApi = new JmdnsAPI(context);
        }

        public static final String Extra_Address = "Address";
        public static final String Extra_Model = "getType";

        public void startScan() {
            mdnsApi.startMdnsService("_easylink._tcp.local.", this);

        }

        public void stopScan() {
            mdnsApi.stopMdnsService();
        }


        @Override
        public void onJmdnsFind(JSONArray jsonArray) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String name = object.getString("deviceName");
                    int p = name.indexOf("#");
                    if (p > 0) {
                        name = name.substring(p + 1);
                        String deviceMAC = object.getString("deviceMac");
                        if (deviceMAC == null) return;
                        if (deviceMAC.isEmpty()) return;

                        MXChipIO io = OznerDeviceManager.Instance().ioManagerList().mxChipIOManager().
                                createMXChipDevice(deviceMAC, "FOG_HAOZE_AIR");
                        io.name = name;

                        Intent intent = new Intent(ACTION_SCANNER_FOUND);
                        intent.putExtra(Extra_Address, deviceMAC);
                        intent.putExtra(Extra_Model, "FOG_HAOZE_AIR");
                        context().sendBroadcast(intent);
                        if (proxy.isConnected())
                            doAvailable(io);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    final static int delayedAvailableMessage = 0x1000;

    @Override
    protected void doUnavailable(BaseDeviceIO io) {
        super.doUnavailable(io);
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == delayedAvailableMessage) {
                    String address = msg.obj.toString();
                    synchronized (listenDeviceList) {
                        if (listenDeviceList.containsKey(address)) {
                            String type = listenDeviceList.get(address);
                            if (OznerDeviceManager.Instance().hashDevice(address)) {
                                MXChipIO io = createMXChipDevice(address, type);
                                if (io != null) {
                                    doAvailable(io);
                                }
                            }
                        }
                    }
                }
            }
        };

        if (!OznerDeviceManager.Instance().hashDevice(io.getAddress())) {
            listenDeviceList.remove(io.getAddress());
        } else {
            if (proxy.isConnected()) {
                Message m = new Message();
                m.what = delayedAvailableMessage;
                m.obj = io.getAddress();
                handler.sendMessageDelayed(m, 5000); //如果IO被关闭了,MQTT还是连接中的情况下,重新激活IO
            }
        }
        super.doUnavailable(io);
    }


    @Override
    public void Start(String user, String token) {
        proxy.start();
    }

    @Override
    public void Stop() {
        proxy.stop();
    }

    @Override
    protected void doChangeRunningMode() {
        super.doChangeRunningMode();
    }

    class MQTTProxyImp implements MQTTProxy.MQTTListener {

        @Override
        public void onConnected(MQTTProxy proxy) {
            synchronized (listenDeviceList) {
                for (String address : listenDeviceList.keySet()) {
                    MXChipIO io = new MXChipIO(context(), proxy, address, listenDeviceList.get(address));
                    doAvailable(io);
                }
            }
        }

        @Override
        public void onDisconnected(MQTTProxy proxy) {
            synchronized (listenDeviceList) {
                for (String address : listenDeviceList.keySet()) {
                    BaseDeviceIO io = getAvailableDevice(address);
                    if (io != null)
                        doUnavailable(io);
                }
            }
        }

        @Override
        public void onPublish(MQTTProxy proxy, String topic, byte[] data) {

        }
    }

}
