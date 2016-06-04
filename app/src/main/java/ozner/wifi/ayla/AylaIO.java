package ozner.wifi.ayla;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aylanetworks.aaml.AylaDatapoint;
import com.aylanetworks.aaml.AylaDevice;
import com.aylanetworks.aaml.AylaNetworks;
import com.aylanetworks.aaml.AylaProperty;
import com.aylanetworks.aaml.AylaSystemUtils;

import java.util.ArrayList;
import java.util.HashMap;

import ozner.device.BaseDeviceIO;
import ozner.device.DeviceNotReadyException;
import ozner.device.OperateCallback;
import ozner.wifi.ThreadHandler;

/**
 * Created by zhiyongxu on 16/4/27.
 */
public class AylaIO extends BaseDeviceIO {
    AylaDevice aylaDevice;
    ConnectStatus connectStatus=ConnectStatus.Disconnect;
    String address;
    HashMap<String,AylaProperty> properties=new HashMap<>();
    private final ThreadHandler notifyHandler=new ThreadHandler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public AylaIO(Context context, final AylaDevice device) {
        super(context, device.model);
        String mac = device.mac.toUpperCase();
        address = mac.substring(0, 2) + ":" +
                mac.substring(2, 4) + ":" +
                mac.substring(4, 6) + ":" +
                mac.substring(6, 8) + ":" +
                mac.substring(8, 10) + ":" +
                mac.substring(10, 12);

        aylaDevice=device;
        device.getProperties(new ThreadHandler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == AylaNetworks.AML_ERROR_OK) {

                    String jsonResults = msg.obj.toString();
                    AylaProperty[] ps = AylaSystemUtils.gson.fromJson(jsonResults, AylaProperty[].class);
                    synchronized (properties)
                    {
                        for (AylaProperty property : ps)
                        {
                            if (!properties.containsKey(property.name))
                                properties.put(property.name,property);
                        }

                    }

                    doConnected();
                    doInit();

                    doReady();
                }
                super.handleMessage(msg);
            }
        });

        doConnecting();
    }

    @Override
    protected void doConnected() {
        connectStatus=ConnectStatus.Connected;
        super.doConnected();
    }

    @Override
    protected void doDisconnected() {
        connectStatus=ConnectStatus.Disconnect;
        super.doDisconnected();
    }
    @Override
    protected void doConnecting() {
        connectStatus=ConnectStatus.Connecting;
        super.doConnecting();
    }


    private AylaProperty getAylaProperty(String name)
    {
        synchronized (properties)
        {
            return properties.get(name);
        }
    }


    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public boolean send(byte[] bytes) {
        return send(bytes,null);
    }
    class ParamHandler extends Handler
    {
        OperateCallback<Void> callback;
        public ParamHandler(OperateCallback<Void> callback)
        {
            this.callback=callback;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what==AylaNetworks.AML_ERROR_OK)
            {
                callback.onSuccess(null);
            }else
            {
                callback.onFailure(new Exception(msg.obj.toString()));
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public boolean send(byte[] bytes, OperateCallback<Void> callback) {
        String json = new String(bytes);
        JSONObject object = JSON.parseObject(json);
        AylaProperty property = getAylaProperty(object.getString("name"));
        if (property != null) {
            AylaDatapoint datapoint = new AylaDatapoint();

            if (TextUtils.equals(property.baseType(), "integer")) {
                datapoint.nValue(object.getInteger("value"));
            }else
            if (TextUtils.equals(property.baseType(), "boolean")) {
                datapoint.nValue(new Byte(object.getBoolean("value") ? (byte) 1 : (byte) 0));
            }else
            if (TextUtils.equals(property.baseType(), "string")) {
                datapoint.sValue(object.getString("value"));
            }else
            if (TextUtils.equals(property.baseType(), "float")) {
                datapoint.nValue(object.getFloat("value"));
            }else
            if (TextUtils.equals(property.baseType(), "decimal")) {
                datapoint.nValue(object.getInteger("value"));
            }else
                datapoint.sValue(object.getString("value"));
            property.createDatapoint(new ParamHandler(callback),datapoint);
            return true;
        }
        else
            return false;
    }

    public void updateProperty()
    {
        aylaDevice.getProperties(new ThreadHandler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == AylaNetworks.AML_ERROR_OK) {
                    String jsonResults = msg.obj.toString();
                    AylaProperty[] ps = AylaSystemUtils.gson.fromJson(jsonResults, AylaProperty[].class);

                    ArrayList<AylaProperty> ap=new ArrayList<AylaProperty>();
                    synchronized (properties)
                    {
                        for (AylaProperty p : ps)
                        {
                            AylaProperty property=properties.get(p.name);
                            if (property!=null)
                            {
                                if (property.value!=null) {
                                    if (!property.value.equals(p.value)) {
                                        ap.add(p);
                                    }
                                }
                            }
                            properties.put(p.name,p);
                        }
                    }
                    JSONObject json=new JSONObject();
                    if (ap.size()>0) {
                        for (AylaProperty p : ap) {
                            json.put(p.name, p.value);
                        }
                    }
                    doRecv(json.toJSONString().getBytes());
                }
                super.handleMessage(msg);
            }
        });
    }
    public String getProperty(String name)
    {
        AylaProperty property = getAylaProperty(name);
        if (property!=null)
        {
            return property.datapoint.value();
        }else
            return null;
    }
    @Override
    public void close() {

    }

    @Override
    public void open() throws DeviceNotReadyException {

    }

    @Override
    public ConnectStatus connectStatus() {
        return connectStatus;
    }

}
