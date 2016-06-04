package ozner.device;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;

import ozner.wifi.ayla.AylaIOManager;
import ozner.wifi.mxchip.MXChipIOManager;

/**
 * Created by xzyxd on 2015/11/2.
 */
public class IOManagerList extends IOManager {
    MXChipIOManager mxChipIOManager;
    AylaIOManager aylaIOManager;

    public IOManagerList(Context context) {
        super(context);
        mxChipIOManager = new MXChipIOManager(context);
        aylaIOManager = new AylaIOManager(context);
    }

    public MXChipIOManager mxChipIOManager() {
        return mxChipIOManager;
    }

    public AylaIOManager aylaIOManager() {
        return aylaIOManager;
    }


    @Override
    public void Start(String user, String token) {
        mxChipIOManager.Start(user, token);
        aylaIOManager.Start(user, token);
    }

    @Override
    public void Stop() {
        mxChipIOManager.Stop();
        aylaIOManager.Stop();
    }

    @Override
    public void closeAll() {
        mxChipIOManager.closeAll();
    }

    @Override
    public void setIoManagerCallback(IOManagerCallback ioManagerCallback) {
        mxChipIOManager.setIoManagerCallback(ioManagerCallback);
        aylaIOManager.setIoManagerCallback(ioManagerCallback);

    }

    @Override
    public BaseDeviceIO getAvailableDevice(String address) {
        BaseDeviceIO io = null;

        if ((io = mxChipIOManager.getAvailableDevice(address)) != null) {
            return io;
        }
        if ((io = aylaIOManager.getAvailableDevice(address)) != null) {
            return io;
        }

        return io;
    }

    @Override
    public BaseDeviceIO[] getAvailableDevices() {
        ArrayList<BaseDeviceIO> list = new ArrayList<>();
        Collections.addAll(list, mxChipIOManager.getAvailableDevices());
        Collections.addAll(list, aylaIOManager.getAvailableDevices());

        return list.toArray(new BaseDeviceIO[list.size()]);
    }

}
