package ozner.wifi;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.aylanetworks.aaml.AylaDevice;
import com.aylanetworks.aaml.AylaHostScanResults;
import com.aylanetworks.aaml.AylaNetworks;
import com.aylanetworks.aaml.AylaSetup;
import com.aylanetworks.aaml.AylaSystemUtils;
import com.mxchip.jmdns.JmdnsAPI;
import com.mxchip.jmdns.JmdnsListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ozner.device.BaseDeviceIO;
import ozner.device.OznerDeviceManager;
import ozner.util.Helper;
import ozner.util.HttpUtil;
import ozner.wifi.ayla.AylaIO;
import ozner.wifi.mxchip.MXChipIO;
import ozner.wifi.mxchip.Pair.ConfigurationDevice;
import ozner.wifi.mxchip.Pair.EasyLinkSender;
import ozner.wifi.mxchip.Pair.FTC;
import ozner.wifi.mxchip.Pair.FTC_Listener;

/**
 * Created by zhiyongxu on 16/5/12.
 */
public class WifiPair {

    public interface WifiPairCallback {
        /**
         * 开始查找AYLA设备
         */
        void onStartPairAyla();

        /**
         * 开始配对庆科设备
         */
        void onStartPariMxChip();


        /**
         * 开始发送WIFI设置信息，AYLA 找到设备时掉用
         */
        void onSendConfiguration();

        /**
         * 开始激活注册设备
         */
        void onActivateDevice();
        /**
         * 等待设备连接WIFI
         */
        void onWaitConnectWifi();

        /**
         * 配网完成
         *
         * @param io 配对好的设备IO接口
         */
        void onPairComplete(BaseDeviceIO io);

        /**
         * 配网失败
         *
         * @param e 失败的异常
         */
        void onPairFailure(Exception e);
    }

    /**
     * 已经有一个配网过程正在运行
     */
    public class PairRunningException extends Exception
    {}

    /**
     * 无线设备没打开或无连接
     */
    public static class WifiStatusException extends Exception {
    }

    public static class NullSSIDException extends Exception {
    }

    public static class TimeoutException extends Exception {
    }
    public static class UnknownException extends Exception {}

    public static class AylaException extends Exception{
        public AylaException(String message)
        {
            super(message);
        }

    }
    private WifiPairCallback callback=null;
    private String ssid=null;
    private String password=null;
    private Context context;

    private int runPairCount=0;
    private Date startRunTime;
    private ThreadHandler runHandler=null;
    private int errorCount=0;
    Object waitObject = new Object();
    private void wait(int time) throws InterruptedException {
        synchronized (waitObject) {
            waitObject.wait(time);
        }
    }

    private void set() {
        synchronized (waitObject) {
            waitObject.notify();
        }
    }


    public WifiPair(Context context, WifiPairCallback callback)
            throws NullSSIDException
    {
        this.context=context;
        this.callback=callback;
    }

    private void doPairFailure(Exception ex)
    {
        callback.onPairFailure(ex);
        stop();
    }
    private void doComplete(BaseDeviceIO io)
    {
        callback.onPairComplete(io);
        stop();
    }


    class MXChipPairImp implements FTC_Listener, Runnable, JmdnsListener {
        /**
         * 默认1分钟配网超时
         */
        final static int ConfigurationTimeout = 30000;
        final static int MDNSTimeout = 60000;
        String deviceMAC = "";



        ConfigurationDevice device = null;


        @Override
        public void onFTCfinished(String jsonString) {
            device = ConfigurationDevice.loadByFTCJson(jsonString);
            set();
        }

        @Override
        public void isSmallMTU(int MTU) {

        }

        private String getToken() {
            return String.valueOf(System.currentTimeMillis());
        }

        private String ActiveDevice() throws IOException {
            com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
            String url = "http://" + device.localIP + ":" + device.localPort + "/dev-activate";
            jsonObject.put("login_id", device.loginId);
            jsonObject.put("dev_passwd", device.devPasswd);
            jsonObject.put("user_token", getToken());
            String retString = HttpUtil.postJSON(url, jsonObject.toJSONString(), "US-ASCII");
            com.alibaba.fastjson.JSONObject ret = (com.alibaba.fastjson.JSONObject) JSON.parse(retString);
            return ret.getString("device_id");
        }

        private String Authorize() throws IOException {
            com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
            String url = "http://" + device.localIP + ":" + device.localPort + "/dev-authorize";
            jsonObject.put("login_id", device.loginId);
            jsonObject.put("dev_passwd", device.devPasswd);
            jsonObject.put("user_token", getToken());
            String retString = HttpUtil.postJSON(url, jsonObject.toJSONString(), "US-ASCII");
            com.alibaba.fastjson.JSONObject ret = (com.alibaba.fastjson.JSONObject) JSON.parse(retString);
            return ret.getString("device_id");
        }

        @Override
        public void run() {
            try {

                JmdnsAPI mdnsApi = new JmdnsAPI(context);
                device = null;
                deviceMAC = null;
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo();

                if (info.getSupplicantState() != SupplicantState.COMPLETED) {
                    doPairFailure(new WifiStatusException());
                    return;
                }
                callback.onStartPariMxChip();

                FTC ftc = new FTC(context, this);
                try {
                    ftc.startListen();
                    EasyLinkSender sender = new EasyLinkSender();
                    sender.setSettings(ssid.trim(), password, info.getIpAddress());
                    Date t = new Date();

                    while (device == null) {
                        sender.send_easylink_v3();
                        Thread.sleep(100);
                        sender.send_easylink_v2();
                        Thread.sleep(100);
                        Date now = new Date();
                        if ((now.getTime() - t.getTime()) > ConfigurationTimeout) {
                            break;
                        }
                    }
                    sender.close();
                } finally {
                    ftc.stop();
                }

                if (device == null) {
                    runNext();
                    return;
                }

                if ((device.activated) && (!Helper.StringIsNullOrEmpty(device.activeDeviceID))) {
                    int p = device.activeDeviceID.indexOf("/");
                    if (p > 0) {
                        String tmp = device.activeDeviceID.substring(p + 1).toUpperCase();
                        if (tmp.length() == 12) {
                            String mac = tmp.substring(0, 2) + ":" +
                                    tmp.substring(2, 4) + ":" +
                                    tmp.substring(4, 6) + ":" +
                                    tmp.substring(6, 8) + ":" +
                                    tmp.substring(8, 10) + ":" +
                                    tmp.substring(10, 12);
                            MXChipIO io = OznerDeviceManager.Instance().ioManagerList().mxChipIOManager().
                                    createMXChipDevice(mac, device.Type);
                            if (io != null) {
                                doComplete(io);
                            } else
                                doPairFailure(new UnknownException());
                            return;
                        }
                    }
                }
                callback.onWaitConnectWifi();

                //Thread.sleep(2000);
                mdnsApi.startMdnsService("_easylink._tcp.local.", this);
                WifiPair.this.wait(MDNSTimeout);
                mdnsApi.stopMdnsService();

                if (Helper.StringIsNullOrEmpty(deviceMAC)) {
                    doPairFailure(new TimeoutException());
                    return;
                }
                callback.onActivateDevice();

                String deviceId = ActiveDevice();
                if (Helper.StringIsNullOrEmpty(deviceId)) {
                    doPairFailure(new UnknownException());
                }
                //Authorize();
//                if (Helper.StringIsNullOrEmpty(deviceId)) {
//                    doPairFailure(null);
//                }

                MXChipIO io = OznerDeviceManager.Instance().ioManagerList().mxChipIOManager().
                        createMXChipDevice(deviceMAC, device.Type);
                io.name=device.name;
                if (io != null) {
                    doComplete(io);
                } else
                    doPairFailure(null);
            } catch (Exception e) {
                doPairFailure(e);
            } finally {

            }
        }

        @Override
        public void onJmdnsFind(JSONArray jsonArray) {
            if (device == null) return;
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String name = object.getString("deviceName");
                    int p = name.indexOf("#");
                    if (p > 0) {
                        name = name.substring(p + 1);
                        if (device.name.indexOf(name) > 0) {
                            deviceMAC = object.getString("deviceMac");
                            device.localIP = object.getString("deviceIP");
                            set();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class AylaPairImp implements Runnable
    {
        private void doRegister(AylaDevice device)
        {
            //callback.onActivateDevice();

            device.registerNewDevice(new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==AylaNetworks.AML_ERROR_OK)
                    {
                        String jsonResults = (String)msg.obj;
                        AylaDevice device = AylaSystemUtils.gson.fromJson(jsonResults,  AylaDevice.class);
                        AylaIO io= OznerDeviceManager.Instance().ioManagerList().aylaIOManager().createAylaIO(device);
                        doComplete(io);
                    }


                    super.handleMessage(msg);
                }
            });
        }

        private void confirmNewDeviceToService()
        {
            //确认设备连接WIFI
            callback.onWaitConnectWifi();
            AylaSetup.confirmNewDeviceToServiceConnection(new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==AylaNetworks.AML_ERROR_OK)
                    {
                        String jsonResults = (String)msg.obj;
                        AylaDevice device = AylaSystemUtils.gson.fromJson(jsonResults,  AylaDevice.class);

                        String json= null;
                        try {
                            json = HttpUtil.get(String.format("http://%s/status.json",device.lanIp));
                            if (!Helper.StringIsNullOrEmpty(json))
                            {
                                com.alibaba.fastjson.JSONObject object= JSON.parseObject(json);
                                String mac=object.getString("mac").toUpperCase();

                                BaseDeviceIO io=OznerDeviceManager.Instance().ioManagerList().aylaIOManager().getAvailableDevice(mac);
                                if (io==null)
                                {
                                    doRegister(device);
                                }else
                                {
                                    device.mac=mac;
                                    device.model=object.getString("Model");
                                    doComplete(io);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        doRegister(device);

                    }else
                    {
                        if (errorCount<3)
                        {
                            errorCount++;
                            confirmNewDeviceToService();
                        }
                        else {
                            String error = "";
                            if (msg.obj == null) {
                                error = String.format("Ayla Code:%d", msg.what);
                            } else
                                error = msg.obj.toString();
                            doPairFailure(new AylaException(error));
                        }
                    }
                    super.handleMessage(msg);
                }
            });
        }

        private void connectNewDeviceToService()
        {
            Map<String, Object> callParams = new HashMap<String, Object>();
            callParams.put(AylaNetworks.AML_SETUP_LOCATION_LONGITUDE, 0.00d);
            callParams.put(AylaNetworks.AML_SETUP_LOCATION_LATITUDE, 0.00d);
            //AylaModule device = AylaSystemUtils.gson.fromJson(jsonResults, AylaModule.class);
            AylaSetup.lanSsid=ssid;
            AylaSetup.lanPassword=password;
            callback.onSendConfiguration();
            //配置AYLA 设备的WIFI信息
            AylaSetup.connectNewDeviceToService(new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==AylaNetworks.AML_ERROR_OK)
                    {
                        confirmNewDeviceToService();
                    }else
                    {
                        String error="";
                        if (msg.obj==null)
                        {
                            error=String.format("Ayla Code:%d",msg.what);
                        }else
                            error=msg.obj.toString();
                        doPairFailure(new AylaException(error));
                    }
                    super.handleMessage(msg);
                }

            }, callParams);

        }

        //开始连接AYLA AP
        private void connectDevice(AylaHostScanResults result) {

            AylaSetup.newDevice.hostScanResults = result;

            //连接设备
            AylaSetup.connectToNewDevice(new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    String jsonResults = (String) msg.obj;
                    if (msg.what == AylaNetworks.AML_ERROR_OK) {
                        connectNewDeviceToService();
                    }
                }
            });
        }



        @Override
        public void run() {
            callback.onStartPairAyla();
            AylaSetup.returnHostScanForNewDevices(new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == AylaNetworks.AML_ERROR_OK) {
                        String jsonResults = (String) msg.obj;
                        AylaHostScanResults[] scanResults = AylaSystemUtils.gson.fromJson(jsonResults, AylaHostScanResults[].class);
                        if (scanResults.length>0)
                        {
                            connectDevice(scanResults[0]);
                        }else
                        {
                            runNext();
                        }
                    }else
                    {
                        runNext();
                    }

                }
            });
        }

    }

    public void pair(String ssid,String password) throws PairRunningException
    {
        this.ssid=ssid;
        this.password=password;
        if (runHandler!=null)
        {
            throw new PairRunningException();
        }
        startRunTime=new Date();
        runHandler=new ThreadHandler();
        runNext();
    }
    private void runNext()
    {

        Date now = new Date();
        if ((now.getTime() - startRunTime.getTime()) > 2*60*1000) {
            doPairFailure(new TimeoutException());
            return;
        }
        if ((runPairCount % 2)==0)
        {
            runHandler.post(new AylaPairImp());
        }else
        {
            runHandler.post(new MXChipPairImp());
        }
        runPairCount++;

    }

    public void stop()
    {
        runHandler.close();
        runHandler=null;
    }



}
