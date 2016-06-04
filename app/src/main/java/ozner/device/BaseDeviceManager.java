package ozner.device;

import android.content.Context;

import ozner.XObject;
import ozner.device.BaseDeviceIO;
import ozner.device.OznerDevice;
import ozner.device.OznerDeviceManager;

/**
 * 设备管理基类
 *
 * @author zhiyongxu
 * @category Device
 */
public abstract class BaseDeviceManager extends XObject {

    public BaseDeviceManager(Context context) {
        super(context);
    }
    protected abstract OznerDevice createDevice(String address, String type, String settings);

    public OznerDevice loadDevice(String address, String type, String settings) {
        if (isMyDevice(type)) {
            OznerDevice device = OznerDeviceManager.Instance().getDevice(address);
            if (device == null) {
                return  createDevice(address,type,settings);
            }else
                return device;
        } else
            return null;
    }
    public boolean checkIsBindMode(BaseDeviceIO io)
    {
        return false;
    }
    public abstract boolean isMyDevice(String type);
}
