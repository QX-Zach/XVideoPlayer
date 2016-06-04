package ozner.wifi.mxchip.Pair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by zhiyongxu on 15/10/18.
 */
public class ConfigurationDevice {
    public String name;
    public String ap;
    public String localIP;
    public String firmware;
    public String loginId = "admin";
    public String devPasswd = "12345678";
    public int localPort = 8000;
    public boolean activated;
    public boolean connected;
    /**
     * 激活设备的ID
     */
    public String activeDeviceID;

    public String Type;

    public static ConfigurationDevice loadByJSON(String jsonString) {
        JSONObject jsonObject = (JSONObject) JSON.parse(jsonString);
        ConfigurationDevice device = new ConfigurationDevice();
        device.name = jsonObject.getString("name");
        device.ap = jsonObject.getString("ap");
        device.localIP = jsonObject.getString("localIP");
        device.firmware = jsonObject.getString("firmware");
        device.loginId = jsonObject.getString("loginId");
        device.devPasswd = jsonObject.getString("devPasswd");
        device.activated = jsonObject.getBoolean("activated");
        device.connected = jsonObject.getBoolean("connectStatus");
        device.localPort = jsonObject.getIntValue("localPort");
        device.Type = jsonObject.getString("Type");
        return device;
    }

    public static ConfigurationDevice loadByFTCJson(String jsonString) {
        if ((jsonString == null) || (jsonString.equals(""))) {
            return null;
        }
        try {
            JSONObject json = JSON.parseObject(jsonString);
            if (json == null) return null;

            ConfigurationDevice device = new ConfigurationDevice();
            device.name = json.getString("N");
            device.Type = json.getString("FW").replace("@", "");
            JSONArray array = json.getJSONArray("C");
            for (int i = 0; i < array.size(); i++) {

                JSONObject object = array.getObject(i, JSONObject.class);
                if (object.getString("N").equals("WLAN")) {
                    JSONArray jsonArray = object.getJSONArray("C");
                    for (int x = 0; x < jsonArray.size(); x++) {
                        JSONObject sub = jsonArray.getObject(x, JSONObject.class);
                        String name = sub.getString("N");
                        if (name.equals("Wi-Fi")) {
                            device.ap = sub.getString("C");
                            continue;
                        }
                        if (name.equals("IP address")) {
                            device.localIP = sub.getString("C");
                            continue;
                        }
                    }
                    continue;
                }


                if (object.getString("N").equals("Cloud info")) {
                    JSONArray jsonArray = object.getJSONArray("C");
                    for (int x = 0; x < jsonArray.size(); x++) {
                        JSONObject sub = jsonArray.getObject(x, JSONObject.class);
                        String name = sub.getString("N");
                        if (name.equals("activated")) {
                            device.activated = sub.getBoolean("C");
                            continue;
                        }
                        if (name.equals("connectStatus")) {
                            device.connected = sub.getBoolean("C");
                            continue;
                        }
                        if (name.equals("rom version")) {
                            device.firmware = sub.getString("C");
                            continue;
                        }
                        if (name.equals("device_id")) {
                            device.activeDeviceID = sub.getString("C");
                        }
                        if (name.equals("Cloud settings")) {
                            JSONArray setting = sub.getJSONArray("C");
                            for (int y = 0; y < setting.size(); y++) {
                                JSONObject sj = setting.getObject(y, JSONObject.class);
                                String sn = sj.getString("N");
                                if (sn.equals("login_id")) {
                                    device.loginId = sj.getString("C");
                                    continue;
                                }
                                if (sn.equals("devPasswd")) {
                                    device.devPasswd = sj.getString("C");
                                    continue;
                                }


                            }
                            continue;
                        }

                    }

                    continue;
                }


            }
            return device;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("ap", ap);
        jsonObject.put("localIP", localIP);
        jsonObject.put("firmware", firmware);
        jsonObject.put("loginId", loginId);
        jsonObject.put("devPasswd", devPasswd);
        jsonObject.put("activated", activated);
        jsonObject.put("connectStatus", connected);
        jsonObject.put("localPort", localPort);
        jsonObject.put("Type", Type);
        return jsonObject.toJSONString();
    }

}
