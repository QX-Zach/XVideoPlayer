package ozner.AirPurifier;

import android.content.Context;

import ozner.device.BaseDeviceIO;
import ozner.device.DeviceNotReadyException;
import ozner.device.OznerDevice;

/**
 * Created by zhiyongxu on 15/11/16.
 */
public abstract class AirPurifier extends OznerDevice {

    public static final String ACTION_AIR_PURIFIER_SENSOR_CHANGED = "ozner.AirPurifier.Sensor.Changed";
    public static final String ACTION_AIR_PURIFIER_STATUS_CHANGED = "ozner.AirPurifier.Status.Changed";

    public AirPurifier(Context context, String Address, String Type, String Setting) {
        super(context, Address, Type, Setting);
    }

    /**
     * 设备型号
     *
     * @return 型号
     */
    public abstract String Model();

    @Override
    public boolean Bind(BaseDeviceIO deviceIO) throws DeviceNotReadyException {
        return super.Bind(deviceIO);
    }
}
