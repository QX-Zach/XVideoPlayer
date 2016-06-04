package com.ozner.xvidoeplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;

import ozner.device.BaseDeviceIO;
import ozner.device.NotSupportDeviceException;
import ozner.device.OznerDevice;
import ozner.device.OznerDeviceManager;
import ozner.wifi.WifiPair;
import ozner.wifi.mxchip.MXChipIO;

import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class WifiConfigurationActivity extends Activity {
    final WifiPairImp wifiPairImp = new WifiPairImp();
    EditText wifi_ssid;
    EditText wifi_passwd;
    CircularProgressButton nextButton;
    CheckBox showpasswd;
    TextView status_text;
    WifiManager wifiManager;
    SharedPreferences wifiPreferences;
    Monitor monitor;
    Date time;
    BaseDeviceIO bindIO = null;
    asyHandle handle = new asyHandle();
    WifiPair wifiPair;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_configuration);
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiPreferences = this.getSharedPreferences("WifiPassword", Context.MODE_PRIVATE);
        monitor = new Monitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(monitor, filter);

        wifi_ssid = (EditText) this.findViewById(R.id.wifi_ssid);
        wifi_passwd = (EditText) this.findViewById(R.id.wifi_password);
        status_text = (TextView) this.findViewById(R.id.statusText);
        nextButton = (CircularProgressButton) this.findViewById(R.id.NextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStartButton();
            }
        });
        nextButton.setIndeterminateProgressMode(true);

        showpasswd = (CheckBox) this.findViewById(R.id.showPassword);
        showpasswd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifi_passwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else
                    wifi_passwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        loadWifi();
        try {
            wifiPair=new WifiPair(this,wifiPairImp);
        } catch (WifiPair.NullSSIDException e) {
            e.printStackTrace();
        }
//        MXChipIO io = OznerDeviceManager.Instance().ioManagerList().mxChipIOManager().
//                createMXChipDevice("C8:93:46:4F:84:03", "FOG_HAOZE_AIR");
//        io.name="Air";
//        try {
//            OznerDeviceManager.Instance().save(OznerDeviceManager.Instance().getDevice(io));
//        } catch (NotSupportDeviceException e) {
//            e.printStackTrace();
//        }

    }

    private void loadWifi() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                String ssid = wifiInfo.getSSID().replace("\"", "");
                wifi_ssid.setText(ssid);
                String pwd = wifiPreferences.getString("password." + ssid, "");
                wifi_passwd.setText(pwd);

            } else
                wifi_ssid.setText("");
        } else {
            wifi_ssid.setText("");
        }
    }

    private void loadDevice(MXChipIO io) {
        if (io == null) {
            findViewById(R.id.deviceInfoPanel).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) this.findViewById(R.id.name)).setText("名称:" + io.name);
            ((TextView) this.findViewById(R.id.type)).setText("类型:" + io.getType());
            ((TextView) this.findViewById(R.id.deviceId)).setText("设备MAC:" + io.getAddress());
            findViewById(R.id.deviceInfoPanel).setVisibility(View.VISIBLE);
            nextButton.setProgress(100);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(monitor);
    }

    private void setStatusText(final String text) {
        Message msg = new Message();
        msg.what = 0;
        msg.obj = text;
        handle.sendMessage(msg);
    }

    private void onClickStartButton() {
        if (nextButton.getProgress() == 100) {
            try {
                OznerDevice device = OznerDeviceManager.Instance().getDevice(bindIO);
                OznerDeviceManager.Instance().save(device);
                finish();
            } catch (NotSupportDeviceException e) {
                e.printStackTrace();
            }
        } else {
            try {
                String ssid = wifi_ssid.getText().toString().trim();
                if (ssid.isEmpty()) {
                    Toast toast = Toast.makeText(this, "没有设置SSID", Toast.LENGTH_LONG);
                    toast.show();
                }

                SharedPreferences.Editor editor = wifiPreferences.edit();
                try {
                    editor.putString("password." + ssid, wifi_passwd.getText().toString());
                } finally {
                    editor.commit();
                }
                time = new Date();
                wifiPair.pair(wifi_ssid.getText().toString().trim(),
                        wifi_passwd.getText().toString());
//                MXChipPair.Pair(this, wifi_ssid.getText().toString().trim(),
//                        wifi_passwd.getText().toString(), mxChipPairImp);
                nextButton.setProgress(0);

                nextButton.setProgress(10);

            } catch (Exception e) {
                e.printStackTrace();
                nextButton.setProgress(-1);
            }
        }
    }

    class Monitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                loadWifi();
            }
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                loadWifi();
            }
        }
    }

    class asyHandle extends Handler {


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    status_text.setText(msg.obj.toString());
                    break;
                case 1:
                    loadDevice((MXChipIO) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    class WifiPairImp implements WifiPair.WifiPairCallback{

        @Override
        public void onStartPairAyla() {
            setStatusText("正在查找设备信息");
            handle.post(new Runnable() {
                @Override
                public void run() {
                    nextButton.setProgress(50);
                }
            });
        }

        @Override
        public void onStartPariMxChip() {
            setStatusText("正在查找设备信息");
            handle.post(new Runnable() {
                @Override
                public void run() {
                    nextButton.setProgress(50);
                }
            });
        }

        @Override
        public void onSendConfiguration() {
            setStatusText("正在配置设备");
            handle.post(new Runnable() {
                @Override
                public void run() {
                    nextButton.setProgress(50);
                }
            });
        }

        @Override
        public void onActivateDevice() {
            setStatusText("正在激活设备");
            handle.post(new Runnable() {
                @Override
                public void run() {
                    nextButton.setProgress(80);
                }
            });
        }


        @Override
        public void onWaitConnectWifi() {
            setStatusText("等待设备重启");
            handle.post(new Runnable() {
                @Override
                public void run() {
                    nextButton.setProgress(70);
                }
            });

        }

        @Override
        public void onPairComplete(BaseDeviceIO io) {
            Message msg = new Message();
            msg.what = 1;
            msg.obj = io;
            handle.sendMessage(msg);
            handle.post(new Runnable() {
                @Override
                public void run() {
                    Date now = new Date();
                    String s = String.format("配网完成,耗时:%3.1f秒", (now.getTime() - time.getTime()) / 1000f);
                    setStatusText(s);
                    nextButton.setProgress(100);
                }
            });
            bindIO = io;
        }

        @Override
        public void onPairFailure(Exception e) {
            setStatusText("配网错误:" + (e == null ? "" : e.toString()));
            handle.post(new Runnable() {
                @Override
                public void run() {
                    nextButton.setProgress(-1);
                }
            });
        }
    }


}
