package ozner.AirPurifier;

import ozner.util.ByteUtil;

import java.util.Date;

/**
 * Created by zhiyongxu on 15/11/11.
 */
public class FilterStatus {
    public Date lastTime=new Date(0);
    public Date stopTime=new Date(0);
    public int workTime =0;
    public int maxWorkTime = 0;
    public byte[] toBytes()
    {
        synchronized (this) {
            byte[] bytes = new byte[16];
            ByteUtil.putInt(bytes, (int) (lastTime.getTime() / 1000), 0);
            ByteUtil.putInt(bytes, workTime, 4);
            ByteUtil.putInt(bytes, (int) (stopTime.getTime() / 1000), 8);
            ByteUtil.putInt(bytes, maxWorkTime, 12);
            return bytes;
        }
    }
    public byte[] toBluetoothBytes()
    {
        synchronized (this) {
            byte[] bytes = new byte[16];
            ByteUtil.putInt(bytes, (int) (lastTime.getTime() / 1000), 0);
            ByteUtil.putInt(bytes, workTime, 4);
            ByteUtil.putInt(bytes, (int) (stopTime.getTime() / 1000), 8);
            ByteUtil.putInt(bytes, maxWorkTime, 12);
            return bytes;
        }
    }
    public void fromBytes(byte[] bytes)
    {
        synchronized (this) {
            if ((bytes != null) && (bytes.length == 16)) {
                lastTime = new Date(ByteUtil.getInt(bytes, 0) * 1000L);
                workTime = ByteUtil.getInt(bytes, 4);
                stopTime = new Date(ByteUtil.getInt(bytes, 8) * 1000L);
                maxWorkTime = ByteUtil.getInt(bytes, 12);

            }
        }
    }

}
