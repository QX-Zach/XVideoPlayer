package ozner.AirPurifier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import ozner.util.ByteUtil;

/**
 * Created by zhiyongxu on 15/11/10.
 */
public class PowerTimer {
    public final static int Monday = 0x01;
    public final static int Tuesday = 0x02;
    public final static int Wednesday = 0x04;
    public final static int Thursday = 0x08;
    public final static int Friday = 0x10;
    public final static int Saturday = 0x20;
    public final static int Sunday = 0x40;

    /**
     * 开机时间,分钟单位,当日0时起的分钟差值,默认7点
     */
    public short PowerOnTime = 7 * 60;

    /**
     * 关机时间,分钟单位,当日0时起的分钟差值,默认18点
     */
    public short PowerOffTime = 18 * 60;


    /**
     * 周期,如用户选择 周一,周二,周五时该项数值为
     * Monday | Tuesday | Friday
     */
    public byte Week = Monday | Tuesday | Wednesday | Thursday | Friday | Saturday | Sunday;

    /**
     * 是否允许自动开关机
     */
    public boolean Enable = false;

    public void fromByBytes(byte[] bytes) {
        if ((bytes == null) || (bytes.length < 6)) return;
        Enable=bytes[0]!=0;
        PowerOnTime = ByteUtil.getShort(bytes, 1);
        PowerOffTime = ByteUtil.getShort(bytes, 3);
        Week = bytes[5];
    }

    public byte[] ToBytes() {
        byte[] bytes = new byte[6];
        bytes[0]=(byte)(Enable?1:0);
        ByteUtil.putShort(bytes, PowerOnTime, 1);
        ByteUtil.putShort(bytes, PowerOffTime, 3);
        bytes[5] = Week;
        return bytes;
    }

    public String ToJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Enable", Enable);
        jsonObject.put("PowerOnTime", PowerOnTime);
        jsonObject.put("PowerOffTime", PowerOffTime);
        jsonObject.put("Week", Week);
        return jsonObject.toJSONString();
    }

    public void fromJSON(String json) {
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            Enable = jsonObject.getBoolean("Enable");
            PowerOnTime = jsonObject.getShort("PowerOnTime");
            PowerOffTime = jsonObject.getShort("PowerOffTime");
            Week = jsonObject.getByte("Week");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
