package ozner.WaterPurifier;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;

import ozner.XObject;
import ozner.device.BaseDeviceIO;
import ozner.device.OperateCallback;
import ozner.util.ByteUtil;
import ozner.util.Helper;
import ozner.wifi.ayla.AylaIO;


/**
 * Created by xzyxd on 2015/11/2.
 */
public class WaterPurifier_Ayla extends WaterPurifier {

    final static String Property_Power = "Power";
    final static String Property_Heating = "Heating";
    final static String Property_Cooling = "Cooling";
    final static String Property_Sterilization = "Sterilization";
    final static String Property_Status = "Sterilization";

    private int TDS1 = 0;
    private int TDS2 = 0;

    final WaterPurifierImp waterPurifierImp = new WaterPurifierImp();


    public WaterPurifier_Ayla(Context context, String Address, String Model, String Setting) {
        super(context, Address, Model, Setting);
    }


    @Override
    public Class<?> getIOType() {
        return AylaIO.class;
    }

    @Override
    protected void doSetDeviceIO(BaseDeviceIO oldIO, BaseDeviceIO newIO) {
        if (oldIO != null) {
            oldIO.setOnTransmissionsCallback(null);
            oldIO.unRegisterStatusCallback(waterPurifierImp);
            oldIO.setOnInitCallback(null);
        }
        if (newIO != null) {
            newIO.setOnTransmissionsCallback(waterPurifierImp);
            newIO.registerStatusCallback(waterPurifierImp);
            newIO.setOnInitCallback(waterPurifierImp);
        } else {
            cancelTimer();
        }
        super.doSetDeviceIO(oldIO, newIO);
    }

    private String getProperty(String name) {
        if (IO() == null) return "";
        return ((AylaIO) IO()).getProperty(name);
    }

    @Override
    protected int getTDS1() {
        return TDS1;
    }

    @Override
    protected int getTDS2() {
        return TDS2;
    }

    @Override
    protected boolean getPower() {
        String value = getProperty(Property_Power);
        if (!Helper.StringIsNullOrEmpty(value))
            return (Integer.parseInt(value) != 0 ? true : false);
        return false;
    }

    @Override
    protected void setPower(boolean Power, OperateCallback<Void> cb) {
        if (IO() == null) {
            if (cb != null)
                cb.onFailure(null);
            return;
        }
        JSONObject object = new JSONObject();
        object.put("name", Property_Power);
        object.put("value", new Integer(Power ? 1 : 0));
        IO().send(object.toJSONString().getBytes(), cb);
    }

    @Override
    protected boolean getHot() {
        String value = getProperty(Property_Heating);
        if (!Helper.StringIsNullOrEmpty(value))
            return (Integer.parseInt(value) != 0 ? true : false);
        return false;
    }

    @Override
    protected void setHot(boolean Hot, OperateCallback<Void> cb) {
        if (IO() == null) {
            if (cb != null)
                cb.onFailure(null);
            return;
        }
        JSONObject object = new JSONObject();
        object.put("name", Property_Heating);
        object.put("value", new Integer(Hot ? 1 : 0));
        IO().send(object.toJSONString().getBytes(), cb);
    }

    @Override
    protected boolean getCool() {
        String value = getProperty(Property_Cooling);
        if (!Helper.StringIsNullOrEmpty(value))
            return (Integer.parseInt(value) != 0 ? true : false);
        return false;
    }

    @Override
    protected void setCool(boolean Cool, OperateCallback<Void> cb) {
        if (IO() == null) {
            if (cb != null)
                cb.onFailure(null);
            return;
        }
        JSONObject object = new JSONObject();
        object.put("name", Property_Cooling);
        object.put("value", new Integer(Cool ? 1 : 0));
        IO().send(object.toJSONString().getBytes(), cb);
    }

    @Override
    protected boolean getSterilization() {
        String value = getProperty(Property_Sterilization);
        if (!Helper.StringIsNullOrEmpty(value))
            return (Integer.parseInt(value) != 0 ? true : false);
        return false;
    }

    @Override
    protected void setSterilization(boolean Sterilization, OperateCallback<Void> cb) {
        if (IO() == null) {
            if (cb != null)
                cb.onFailure(null);
            return;
        }
        JSONObject object = new JSONObject();
        object.put("name", Property_Sterilization);
        object.put("value", new Integer(Sterilization ? 1 : 0));
        IO().send(object.toJSONString().getBytes(), cb);
    }

    int requestCount = 0;

    private void updateStatus(OperateCallback<Void> cb) {
        if (IO() == null) {
            if (cb != null)
                cb.onFailure(null);
        } else {
            requestCount++;
            if (requestCount > 3) {
                setOffline(true);
            }
            ((AylaIO) IO()).updateProperty();
        }
    }

    @Override
    protected void doTime() {
        updateStatus(null);
        super.doTime();
    }

    private void loadAylaStatus(String value) {
        try {
            byte[] bytes = Hex.decodeHex(value.toCharArray());
            //mac 0,5
            //model 6,10;
            try {
                info.Model = new String(bytes, 6, 10, "US-ASCII").trim();
                info.MainBoard = new String(bytes, 76, 12, "US-ASCII").trim();
                info.ControlBoard = new String(bytes, 88, 12, "US-ASCII").trim();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            TDS2 = ByteUtil.getShort(bytes, 104);
            TDS1 = ByteUtil.getShort(bytes, 106);

        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doChangeRunningMode() {
        if (getRunningMode() == XObject.RunningMode.Foreground) {
            updateStatus(null);
            if (IO() != null) {
                if (IO().connectStatus() == BaseDeviceIO.ConnectStatus.Connected) {
                    startTimer();
                }
            }
        } else {
            cancelTimer();
        }

        super.doChangeRunningMode();
    }

    class WaterPurifierImp implements
            BaseDeviceIO.OnTransmissionsCallback,
            BaseDeviceIO.StatusCallback,
            BaseDeviceIO.OnInitCallback {

        @Override
        public void onConnected(BaseDeviceIO io) {

        }

        @Override
        public void onDisconnected(BaseDeviceIO io) {

        }

        @Override
        public void onReady(BaseDeviceIO io) {
            setOffline(false);
            if (getRunningMode() == XObject.RunningMode.Foreground) {
                startTimer();
            }
        }


        @Override
        public void onIOSend(byte[] bytes) {


        }


        @Override
        public void onIORecv(byte[] bytes) {
            if (isOffline) {
                setOffline(false);
            }
            requestCount = 0;

            if (bytes != null) {
                String str = new String(bytes);
                JSONObject json = JSON.parseObject(str);
                if (json.size() > 0) {
                    for (String key : json.keySet()) {
                        if (key.equals(Property_Status)) {
                            loadAylaStatus(json.getString(key));
                            break;
                        }
                    }
                    doUpdate();
                }
            }
        }

        @Override
        public boolean onIOInit() {
            try {
                isOffline = false;
                info.MainBoard = getProperty("version");
                loadAylaStatus(getProperty("Status"));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


    }


}
