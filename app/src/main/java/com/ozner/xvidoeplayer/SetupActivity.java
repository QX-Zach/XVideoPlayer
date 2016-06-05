package com.ozner.xvidoeplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ozner.util.GetPathFromUri4kitkat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class SetupActivity extends AppCompatActivity {
    SetupActivityImp setupActivityImp = new SetupActivityImp();
    VideoAdapter videoAdapter;
    ListView listView;

    class VideoAdapter extends BaseAdapter {
        ArrayList<String> list = new ArrayList<>();
        HashSet<String> checked = new HashSet<>();

        public VideoAdapter() {
            load();
        }

        boolean contains(String path) {
            return list.contains(path);
        }

        private void save() {
            SharedPreferences sharedPreferences = getSharedPreferences("Video", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            TreeSet<String> set = new TreeSet<>(list);
            editor.putStringSet("videos", set);
            editor.commit();
        }

        private void load() {
            SharedPreferences sharedPreferences = getSharedPreferences("Video", Context.MODE_PRIVATE);
            Set<String> videos = sharedPreferences.getStringSet("videos", null);
            if (videos != null) {
                checked.clear();
                list.clear();
                for (String p : videos) {
                    list.add(p);
                }
            }
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
                list.remove(key);
            }
            checked.clear();
            save();
            this.notifyDataSetInvalidated();
        }

        public void selectAll() {
            checked.clear();
            for (String key : list) {
                checked.add(key);
            }
            this.notifyDataSetInvalidated();
        }

        public void add(String path) {
            if (!contains(path))
                list.add(path);
            save();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(SetupActivity.this);
            View view = inflater.inflate(R.layout.list_item_video, null);
            TextView tv = (TextView) view.findViewById(R.id.videoPath);
            tv.setText(list.get(position));
            CheckBox cb = (CheckBox) view.findViewById(R.id.videoCheck);
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
            String path = list.get(position);
            cb.setTag(path);
            cb.setChecked(checked.contains(path));
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoAdapter = new VideoAdapter();
        setContentView(R.layout.activity_setup);
        findViewById(R.id.addButton).setOnClickListener(setupActivityImp);
        findViewById(R.id.selectAllButton).setOnClickListener(setupActivityImp);
        findViewById(R.id.removeButton).setOnClickListener(setupActivityImp);
        findViewById(R.id.bindButton).setOnClickListener(setupActivityImp);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(videoAdapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                }else {
                    Toast.makeText(SetupActivity.this, "没有找到相应文件，请重试！", Toast.LENGTH_SHORT).show();
                }
                break;
            case KeyEvent.KEYCODE_2:
                videoAdapter.selectAll();
                break;
            case KeyEvent.KEYCODE_3:
                videoAdapter.remove();
                break;
            case KeyEvent.KEYCODE_4:
                startActivity(new Intent(SetupActivity.this, BindActivity.class));
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class SetupActivityImp implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.addButton: {
                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, 1);
                    }else {
                        Toast.makeText(SetupActivity.this, "没有找到相应文件，请重试！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case R.id.selectAllButton: {
                    videoAdapter.selectAll();
                    break;
                }

                case R.id.removeButton: {
                    videoAdapter.remove();
                    break;
                }
                case R.id.bindButton: {
                    startActivity(new Intent(SetupActivity.this, BindActivity.class));
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode == RESULT_OK) && (requestCode == 1)) {
            Uri uri = data.getData();
            try {
                String path = GetPathFromUri4kitkat.getPath(this, uri);
                videoAdapter.add(path);
                videoAdapter.notifyDataSetInvalidated();
            } catch (Exception e) {
                Log.e("XPlayer", e.toString());
            }
        }
    }
}
