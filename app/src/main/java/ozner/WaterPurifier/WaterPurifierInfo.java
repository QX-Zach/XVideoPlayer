package ozner.WaterPurifier;

import ozner.util.ByteUtil;

/**
 * Created by zhiyongxu on 15/11/20.
 */
public class WaterPurifierInfo {
    /**
     * 型号
     */
    public String Model="";
    /**
     * 机型
     */
    public String Type="";
    /**
     * 主板编号
     */
    public String MainBoard="";
    /**
     * 控制板编号
     */
    public String ControlBoard="";
    /**
     * 错误数量
     */
    public int ErrorCount=0;
    /**
     * 错误码
     */
    public int Error=0;

    public void fromBytes(byte[] bytes) {
        this.Model=new String(bytes,12,10);
        this.Type=new String(bytes,22,16);
        this.MainBoard=new String(bytes,38,22);
        this.ControlBoard=new String(bytes,60,22);
        this.ErrorCount=bytes[123];
        this.Error= ByteUtil.getInt(bytes,124);
    }


}
