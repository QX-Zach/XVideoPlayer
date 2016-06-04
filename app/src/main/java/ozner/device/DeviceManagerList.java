package ozner.device;

import android.content.Context;

import ozner.AirPurifier.AirPurifierManager;
import ozner.WaterPurifier.WaterPurifierManager;
import ozner.device.BaseDeviceManager;

import java.util.ArrayList;

/**
 * Created by xzyxd on 2015/11/2.
 */
public class DeviceManagerList extends ArrayList<BaseDeviceManager> {

    WaterPurifierManager waterPurifierManager;
    AirPurifierManager airPurifierManager;
    public DeviceManagerList(Context context) {
        waterPurifierManager = new WaterPurifierManager(context);
        airPurifierManager = new AirPurifierManager(context);
        add(waterPurifierManager);
        add(airPurifierManager);
    }

    public WaterPurifierManager waterPurifierManager() {
        return waterPurifierManager;
    }
    public AirPurifierManager airPurifierManager() {
        return airPurifierManager;
    }

}
