package ozner.AirPurifier;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import ozner.device.OperateCallback;
import ozner.device.OperateCallbackProxy;

/**
 * Created by zhiyongxu on 15/12/10.
 */
public class A2DP {
    boolean support = false;
    String mac = "";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothA2dp bluetoothA2dp;

    public A2DP(Context context) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.A2DP) {
                    bluetoothA2dp = (BluetoothA2dp) proxy;
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.A2DP) {
                    bluetoothA2dp = null;
                }
            }
        }, BluetoothProfile.A2DP);
    }

    public boolean support() {
        return support;
    }

    public String MAC() {
        return mac;
    }


    public boolean enable() {
        if (bluetoothA2dp == null) return false;

        for (BluetoothDevice device : bluetoothA2dp.getConnectedDevices()) {
            if (device.getAddress().equals(mac)) {
                return true;
            }
        }
        return false;
    }

    public interface A2DPCallback {
        void OpenA2DP(OperateCallback<Void> cb);
    }

    A2DPCallback callback;

    public void setCallback(A2DPCallback cb) {
        this.callback = cb;
    }

    public void setEnable(boolean enable, OperateCallback<Void> cb) {
        if (!support) return;
        if (enable) {


            callback.OpenA2DP(new OperateCallbackProxy<Void>(cb) {
                                  @Override
                                  public void onSuccess(Void var1) {
                                      BluetoothDevice device = bluetoothAdapter.getRemoteDevice(MAC());
                                      if (device == null) {
                                          callback.onFailure(null);
                                      } else {
                                          try {
                                              bluetoothA2dp.getClass().getMethod("connect", BluetoothDevice.class).invoke(bluetoothA2dp, device);
                                              callback.onSuccess(null);
                                          } catch (Exception e) {
                                              e.printStackTrace();
                                          }
                                      }
                                      super.onSuccess(var1);
                                  }

                                  @Override
                                  public void onFailure(Throwable var1) {
                                      super.onFailure(var1);
                                  }
                              }

            );
        }else
        {

        }
    }

    public void load(byte[] bytes) {
        support = bytes[1] != 0;
        mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2]);
    }
}
