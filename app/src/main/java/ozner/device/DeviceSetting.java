package ozner.device;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author zhiyongxu
 *         设备设置基类
 * @category Device
 */
public class DeviceSetting {
    HashMap<String, Object> mValues = new HashMap<String, Object>();

    /**
     * 获取一个设置
     *
     * @param key    设置KEY字符串
     * @param defaut 没有找到时的返回
     * @return
     */
    public Object get(String key, Object defaut) {
        if (mValues.containsKey(key))
            return mValues.get(key);
        else
            return defaut;
    }

    /**
     * 存储一个设置值
     *
     * @param key   设置KEY
     * @param value 值
     */
    public void put(String key, Object value) {
        mValues.put(key, value);
    }

    /**
     * 判断设置是否存在
     *
     * @param key 设置KEY
     * @return TRUE存在
     */
    public boolean has(String key) {
        return mValues.containsKey(key);
    }

    /**
     * 通过JSON加载设置
     *
     * @param json
     */
    public void load(String json) {
        if (json == null)
            return;
        if (json.isEmpty())
            return;
        mValues.clear();
        try {
            JSONObject object = new JSONObject(json);
            Iterator<String> list = object.keys();
            while (list.hasNext()) {
                String key = list.next();
                mValues.put(key, object.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回设备名称
     *
     * @return 设备名称
     */
    public String name() {
        return get("name", "").toString();
    }

    /**
     * 设置设备名称
     *
     * @param value
     */
    public void name(String value) {
        put("name", value);
    }

    /**
     * 返回设备配置JSON
     */
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject(mValues);

        return jsonObject.toString();
    }
}
