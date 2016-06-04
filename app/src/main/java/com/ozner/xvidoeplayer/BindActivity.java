package com.ozner.xvidoeplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import ozner.device.BaseDeviceIO;
import ozner.device.OznerDevice;
import ozner.device.OznerDeviceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class BindActivity extends AppCompatActivity {
    BindActivityIMP bindActivityIMP = new BindActivityIMP();
    DeviceAdapter adapter;
    ListView listView;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.load();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new DeviceAdapter();
        listView.setAdapter(adapter);

        findViewById(R.id.addButton).setOnClickListener(bindActivityIMP);
        findViewById(R.id.selectAllButton).setOnClickListener(bindActivityIMP);
        findViewById(R.id.removeButton).setOnClickListener(bindActivityIMP);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OznerDevice.ACTION_DEVICE_UPDATE);
        intentFilter.addAction(BaseDeviceIO.ACTION_DEVICE_CONNECTED);
        intentFilter.addAction(BaseDeviceIO.ACTION_DEVICE_DISCONNECTED);


        adapter.load();
    }

    class DeviceAdapter extends BaseAdapter {
        ArrayList<OznerDevice> list = new ArrayList<>();
        HashSet<String> checked = new HashSet<>();

        public DeviceAdapter() {
            load();
        }

        private void load() {
            list.clear();
            try {
                for (OznerDevice device : OznerDeviceManager.Instance().getDevices()) {
                    list.add(device);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void remove() {
            for (String key : checked) {
                OznerDeviceManager.Instance().remove(OznerDeviceManager.Instance().getDevice(key));
            }
            checked.clear();
            load();
        }

        public void selectAll() {
            checked.clear();
            for (OznerDevice device : list) {
                checked.add(device.Address());
            }
            this.notifyDataSetInvalidated();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(BindActivity.this);
            View view = inflater.inflate(R.layout.list_item_video, null);
            OznerDevice device = list.get(position);
            view.setTag(device);

            TextView tv = (TextView) view.findViewById(R.id.videoPath);
            tv.setText(list.get(position).toString());
            CheckBox cb = (CheckBox) view.findViewById(R.id.videoCheck);
            cb.setChecked(checked.contains(list.get(position)));
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        checked.remove(buttonView.getTag());
                    } else {
                        if (!checked.contains(buttonView.getTag())) {
                            checked.add(buttonView.getTag().toString());
                        }
                    }
                }
            });
            cb.setTag(device.Address());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v.findViewById(R.id.videoCheck);
                    cb.setChecked(!cb.isChecked());
                }
            });
            return view;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.load();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                Intent intent = new Intent(BindActivity.this, WifiConfigurationActivity.class);
                startActivityForResult(intent, 1);
                break;
            case KeyEvent.KEYCODE_2:
                adapter.selectAll();
                break;
            case KeyEvent.KEYCODE_3:
                adapter.remove();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    class BindActivityIMP implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.addButton: {
                    Intent intent = new Intent(BindActivity.this, WifiConfigurationActivity.class);
                    startActivityForResult(intent, 1);
                    break;
                }
                case R.id.selectAllButton: {
                    adapter.selectAll();
                    break;
                }
                case R.id.removeButton: {
                    adapter.remove();
                    break;
                }
            }
        }

    }
}
