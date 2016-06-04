package ozner.wifi.mxchip;

import android.content.Context;

import ozner.device.OznerDevice;


/**
 * Created by xzyxd on 2015/11/1.
 */
public abstract class MXChipDevice extends OznerDevice {
    public MXChipDevice(Context context, String Address, String Model, String Setting) {
        super(context, Address, Model, Setting);
    }

    public abstract String getSecureCode();
}
