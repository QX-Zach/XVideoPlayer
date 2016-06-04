package com.ozner.xvidoeplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import ozner.AirPurifier.AirPurifier;
import ozner.AirPurifier.AirPurifier_MXChip;
import ozner.WaterPurifier.WaterPurifier;
import ozner.XObject;
import ozner.device.OznerDevice;
import ozner.device.OznerDeviceManager;
import ozner.util.ImageUtil;
import ozner.util.NetJsonObject;
import ozner.util.NetWeather;
import ozner.util.OznerDataHttp;
import ozner.util.dbg;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import static tv.danmaku.ijk.media.player.IMediaPlayer.*;

public class MainActivity extends AppCompatActivity {
    final int NextImg = 0x03;
    static OznerDeviceManager _Manager;
    IRenderView _renderView;
    IjkMediaPlayer _player;
    ArrayList<String> _playList = new ArrayList<>();
    ArrayList<String> _imgList = new ArrayList<>();
    PlayerImp _playerImp = new PlayerImp();
    RelativeLayout _videoLayout;
    ViewFlipper _messagePanel;
    boolean isFliping = true;
    long flipCount = 0;
    int currentIndex = 0;
    String outPM25 = "";
    int _playIndex = 0;
    int _imgIndex = 0;
    boolean isImgFliping = false;
    final int TDS_GOOD_VALUE = 100;
    final int TDS_BAD_VALUE = 200;
    private int delayTime = 10000;
    ImageUtil imageUtil;
    ViewFlipper imgFlip;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mac = intent.getStringExtra(OznerDevice.Extra_Address);
            View view = _messagePanel.findViewWithTag(mac);
            OznerDevice device = OznerDeviceManager.Instance().getDevice(mac);
            if (device != null) {
                updateViewText(view, device);
            }
        }
    };
    private static final int[] s_allAspectRatio = {
            IRenderView.AR_ASPECT_FIT_PARENT,
            IRenderView.AR_ASPECT_FILL_PARENT,
            IRenderView.AR_ASPECT_WRAP_CONTENT,
            // IRenderView.AR_MATCH_PARENT,
            IRenderView.AR_16_9_FIT_PARENT,
            IRenderView.AR_4_3_FIT_PARENT};

    boolean _holderReady = false;
    boolean _playerReady = false;
    IRenderView.IRenderCallback _renderCallback = new IRenderView.IRenderCallback() {
        @Override
        public void onSurfaceCreated(@NonNull IRenderView.ISurfaceHolder holder, int width, int height) {
            holder.bindToMediaPlayer(_player);
            _holderReady = true;
            if (_playerReady) {
                if (_player == null) {
//                    createRender();
                    createPlayer();
                }
                _player.start();
            }
        }

        @Override
        public void onSurfaceChanged(@NonNull IRenderView.ISurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void onSurfaceDestroyed(@NonNull IRenderView.ISurfaceHolder holder) {

        }
    };

    private View initFirstView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.message_view_info, null);
        return view;
    }

    private void updateViewText(View view, OznerDevice device) {
        try {
            if (device instanceof WaterPurifier) {
//            TextView tv = (TextView) view.findViewById(R.id.messageText);
//            tv.setText(((WaterPurifier) device).sensor().toString());
                TextView tv_pre = (TextView) view.findViewById(R.id.tv_pre);
                TextView tv_after = (TextView) view.findViewById(R.id.tv_after);
                LinearLayout llay_connect = (LinearLayout) view.findViewById(R.id.llay_connect);
                TextView tv_offline = (TextView) view.findViewById(R.id.tv_offline);
                TextView tv_state = (TextView) view.findViewById(R.id.tv_state);
                try {
                    WaterPurifier waterPurifier = (WaterPurifier) device;
                    if (!waterPurifier.isOffline()) {
                        tv_offline.setVisibility(View.GONE);
                        llay_connect.setVisibility(View.VISIBLE);
                        tv_state.setVisibility(View.VISIBLE);
                        if (waterPurifier.sensor().TDS1() != 65535) {
                            if (waterPurifier.sensor().TDS1() != 0) {
                                tv_after.setText(String.valueOf(waterPurifier.sensor().TDS1()));
                            } else {
                                tv_after.setText("1");
                            }
                        } else {
                            tv_after.setText(getString(R.string.noneValue));
                        }
                        if (waterPurifier.sensor().TDS2() != 65535) {
                            if (waterPurifier.sensor().TDS2() != 0) {
                                tv_pre.setText(String.valueOf(waterPurifier.sensor().TDS2()));
                            } else {
                                tv_pre.setText("1");
                            }
                        } else {
                            tv_pre.setText(getString(R.string.noneValue));
                        }
                        if (waterPurifier.sensor().TDS1() < TDS_GOOD_VALUE && waterPurifier.sensor().TDS1() >= 0) {
                            tv_state.setText("优");
//                        tv_state.setBackground(ContextCompat.getDrawable(this, R.drawable.good_bg));
                        } else if (waterPurifier.sensor().TDS1() < TDS_BAD_VALUE && waterPurifier.sensor().TDS1() > TDS_GOOD_VALUE) {
                            tv_state.setText("一般");
//                        tv_state.setBackground(ContextCompat.getDrawable(this, R.drawable.normal_bg));
                        } else if (waterPurifier.sensor().TDS1() > TDS_BAD_VALUE) {
                            if (waterPurifier.sensor().TDS1() != 65535) {
                                tv_state.setText("差");
                            } else {
                                tv_state.setVisibility(View.GONE);
                            }
//                        tv_state.setBackground(ContextCompat.getDrawable(this, R.drawable.bad_bg));
                        }
                    } else {
                        tv_offline.setVisibility(View.VISIBLE);
                        llay_connect.setVisibility(View.GONE);
                        tv_state.setVisibility(View.INVISIBLE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("tag", "净水器数据：" + ex.getMessage());
                }
            }
            if (device instanceof AirPurifier_MXChip) {
//            TextView tv = (TextView) view.findViewById(R.id.messageText);
                TextView tv_roomIn = (TextView) view.findViewById(R.id.tv_roomIn);
                TextView tv_roomOut = (TextView) view.findViewById(R.id.tv_roomOut);
                LinearLayout llay_airConn = (LinearLayout) view.findViewById(R.id.llay_airConn);
                TextView tv_state = (TextView) view.findViewById(R.id.tv_state);
                TextView tv_airOffline = (TextView) view.findViewById(R.id.tv_airOffline);
//            tv.setText(((AirPurifier_MXChip) device).sensor().toString());
                try {
                    AirPurifier_MXChip airPurifier_mxChip = (AirPurifier_MXChip) device;
                    if (!airPurifier_mxChip.isOffline()) {
                        tv_airOffline.setVisibility(View.GONE);
                        llay_airConn.setVisibility(View.VISIBLE);
                        if (airPurifier_mxChip.sensor().PM25() != 65535) {
                            tv_roomIn.setText(String.valueOf(airPurifier_mxChip.sensor().PM25()));
                        } else {
                            tv_roomIn.setText(getString(R.string.noneValue));
                        }
                        tv_roomOut.setText(outPM25);
                        tv_state.setVisibility(View.VISIBLE);
                        if (airPurifier_mxChip.sensor().PM25() < 75 && airPurifier_mxChip.sensor().PM25() > 0) {
                            tv_state.setText("优");
//                        tv_state.setBackground(ContextCompat.getDrawable(this, R.drawable.good_bg));
                        } else if (airPurifier_mxChip.sensor().PM25() >= 75 && airPurifier_mxChip.sensor().PM25() < 150) {
                            tv_state.setText("良");
//                        tv_state.setBackground(ContextCompat.getDrawable(this, R.drawable.normal_bg));
                        } else if (airPurifier_mxChip.sensor().PM25() >= 150) {
                            if (airPurifier_mxChip.sensor().PM25() != 65535) {
                                tv_state.setText("差");
                            } else {
                                tv_state.setVisibility(View.GONE);
                            }
//                        tv_state.setBackground(ContextCompat.getDrawable(this, R.drawable.bad_bg));
                        }
                    } else {
                        tv_airOffline.setVisibility(View.VISIBLE);
                        llay_airConn.setVisibility(View.GONE);
                        tv_state.setVisibility(View.INVISIBLE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("tag", "空净数据_ex：" + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private View initAirView(AirPurifier_MXChip airPurifier) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.message_view_air, null);
        view.setTag(airPurifier.Address());
        updateViewText(view, airPurifier);

        return view;
    }

    private View initWaterView(WaterPurifier waterPurifier) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.message_view_water, null);
        view.setTag(waterPurifier.Address());
        updateViewText(view, waterPurifier);
        return view;
    }

    private void loadDeviceList() {
        _messagePanel.removeAllViews();
        //_messageViewIndex=0;
        //_messageViewList.clear();
        _messagePanel.addView(initFirstView());
        for (OznerDevice device : OznerDeviceManager.Instance().getDevices()) {
            if (device instanceof WaterPurifier) {
                _messagePanel.addView(initWaterView((WaterPurifier) device));
            }
            if (device instanceof AirPurifier_MXChip) {
                _messagePanel.addView(initAirView((AirPurifier_MXChip) device));
            }
        }

    }

    private void loadVideoList() {
        SharedPreferences sharedPreferences = getSharedPreferences("Video", Context.MODE_PRIVATE);
        Set<String> videos = sharedPreferences.getStringSet("videos", null);
        _playList.clear();
        _imgList.clear();
        if (videos == null) return;
        for (String path : videos) {
            if (isImg(path)) {
                _imgList.add(path);
            } else {
                _playList.add(path);
            }
        }
        if (_imgList != null && _imgList.size() > 0) {
            if (_videoLayout.getChildCount() > 0) {
                _videoLayout.removeAllViews();
            }

            if (imgFlip != null && imgFlip.getChildCount() > 0) {
                imgFlip.removeAllViews();
            }
            for (String url : _imgList) {
                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageUtil.loadImage(imageView, "file://" + url);
                imgFlip.addView(imageView);
            }
            imgFlip.setAutoStart(false);
            beginShowImg();
        } else {
            _playIndex = 0;
            _playerImp.play();
        }


//        if (_playList != null && _playList.size() > 0) {
//            _playIndex = 0;
//            _playerImp.play();
//        } else if (_imgList != null && _imgList.size() > 0) {
//            if (_videoLayout.getChildCount() > 0) {
//                _videoLayout.removeAllViews();
//            }
//
//            if (imgFlip != null && imgFlip.getChildCount() > 0) {
//                imgFlip.removeAllViews();
//            }
//            for (String url : _imgList) {
//                ImageView imageView = new ImageView(this);
//                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//                imageUtil.loadImage(imageView, "file://" + url);
//                imgFlip.addView(imageView);
//            }
//            imgFlip.setAutoStart(false);
//            beginShowImg();
//        }
    }

    private boolean isImg(String url) {
        if (url.endsWith("png") || url.endsWith("jpg") || url.endsWith("jpeg")) {
            return true;
        }
        return false;
    }

    class PlayerImp implements OnPreparedListener, OnCompletionListener,
            OnErrorListener {
        public void play() {
            if (_playList.size() > 0) {
                try {
                    createRender();
                    createPlayer();
                    _player.setDataSource(MainActivity.this, Uri.parse(_playList.get(_playIndex)));
                    _player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("tag", "Play_ex:" + e.getMessage());
                }
            }
        }

        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            //播放完成，下一个节目循环
//            _playIndex++;
//            if (_playIndex >= _playList.size()) {
//                _playIndex = 0;
//            }
//            play();
            _playIndex++;
            if (_playIndex < _playList.size()) {
                play();
            } else {
                _playIndex = 0;
                if (_imgList.size() > 0) {
                    if (_player != null) {
//                    _player.setDisplay(null);
                        _player.stop();
                        _player.release();
                        _player = null;
                    }
                    beginShowImg();
                } else {
                    play();
                }
            }
        }

        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            _playerReady = true;
            _renderView.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
            _renderView.setVideoSampleAspectRatio(iMediaPlayer.getVideoSarNum(), iMediaPlayer.getVideoSarDen());
            _renderView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
            if (_holderReady)
                _player.start();
        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            Log.e("tag", "onError:播放错误");
            return false;
        }
    }

    void createRender() {
        if (_renderView != null) {
            _videoLayout.removeAllViews();
            _renderView = null;
        }
        _holderReady = false;
        _renderView = new SurfaceRenderView(this);
        _renderView.addRenderCallback(_renderCallback);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        _videoLayout.addView(_renderView.getView(), layoutParams);
    }


    void createPlayer() {
        if (_player != null) {
//            _player.setDisplay(null);
            _player.stop();
            _player.release();
            _player = null;
        }
        _player = new IjkMediaPlayer();

        //_player.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
        _player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        _player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        _player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);

        //_player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        _player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        _player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        _player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
        _player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);

        //_player.setSurface(_surfView.getHolder().getSurface());
        _player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        _player.setScreenOnWhilePlaying(true);

        _player.setOnPreparedListener(_playerImp);
        _player.setOnCompletionListener(_playerImp);
        _player.setOnErrorListener(_playerImp);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            startActivityForResult(new Intent(MainActivity.this, SetupActivity.class), 1);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        startActivityForResult(new Intent(MainActivity.this, SetupActivity.class), 1);
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadVideoList();
        loadDeviceList();
    }

//    class MessageTimeIMP extends TimerTask
//    {
//
//        @Override
//        public void run() {
//            Handler handler=new Handler(getMainLooper());
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    RelativeLayout layout=(RelativeLayout)findViewById(R.id.messagePanel);
//                    View lastView=layout.getChildAt(0);
//
//                    View showView=null;
//                    if (_messageViewIndex>=_messageViewList.size())
//                    {
//                        showView=_messageViewList.get(0);
//                        _messageViewIndex=0;
//                    }else
//                    {
//                        showView=_messageViewList.get(_messageViewIndex);
//                    }
//                    if (showView==lastView) return;
//
//
//                    layout.removeAllViews();
//
//                    _messageViewIndex++;
//                }
//            });
//
//
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            _Manager = new OznerDeviceManager(getApplicationContext());
            _Manager.setOwner("xzy", "xzy");
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
        imageUtil = new ImageUtil(this);
        imageUtil.setImageLoadingListener(new SimpleImageLoadingListener());
        _messagePanel = (ViewFlipper) findViewById(R.id.messagePanel);
        _videoLayout = (RelativeLayout) findViewById(R.id.video_panel);
        imgFlip = new ViewFlipper(this);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
//        _messagePanel.setAutoStart(true);
//        _messagePanel.setFlipInterval(3000);
//        _messagePanel.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_in));
//        _messagePanel.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_out));
        _messagePanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, SetupActivity.class), 1);
            }
        });

        loadVideoList();
        loadDeviceList();
        IntentFilter intentFilter = new IntentFilter(OznerDevice.ACTION_DEVICE_UPDATE);
        registerReceiver(broadcastReceiver, intentFilter);
        timer_pm25.schedule(new TimerTask() {
            @Override
            public void run() {
                getOutPM25();
            }
        }, 0, 300000);
        timer_flip.schedule(new TimerTask() {
            @Override
            public void run() {
                uiHandler.sendEmptyMessage(2);
            }
        }, 0, 1000);
    }

    Timer timer_pm25 = new Timer();
    Timer timer_flip = new Timer();

    public void getOutPM25() {
        OznerDataHttp.getWeather(this, new OznerDataHttp.OznerHttpCallback() {
            @Override
            public void HandleResult(NetJsonObject result) {
                Message message = new Message();
                message.what = 1;
                message.obj = result;
                uiHandler.sendMessage(message);
            }
        });
    }

    Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    try {
                        NetJsonObject netJsonObject = (NetJsonObject) msg.obj;
                        if (netJsonObject != null) {
//                            Log.e("tag", "PM2.5:" + netJsonObject.value);
                            if (netJsonObject.state > 0) {
                                NetWeather weather = new NetWeather();
                                weather.fromJSONObject(netJsonObject.getJSONObject());
                                if (weather != null) {
                                    outPM25 = weather.pm25;
                                }
                            } else {

                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case 2://循环view计时器
                    if (isFliping) {
                        flipCount++;
//                        Log.e("tag", "循环中：" + flipCount);
                        if (flipCount % 10 == 0) {//经过10秒
                            if (_messagePanel.getChildCount() - 1 == currentIndex++) {//当前view是最后一个
                                isFliping = false;
                                currentIndex = 0;
                                flipCount = 0;
                            } else {
                                _messagePanel.showNext();
                            }
//                            currentIndex++;
                        }
                    } else {
                        if (0 == flipCount) {
                            AlphaAnimation alpha_out = new AlphaAnimation(1, 0);
                            alpha_out.setDuration(1000);
                            alpha_out.setFillAfter(true);
                            _messagePanel.startAnimation(alpha_out);
//                            _messagePanel.setVisibility(View.GONE);
                        }
                        flipCount++;
//                        Log.e("tag", "暂停循环：" + flipCount);
                        if (flipCount % 3 == 0) {
                            isFliping = true;
                            flipCount = 0;
                            currentIndex = 0;
//                            _messagePanel.setVisibility(View.VISIBLE);
                            _messagePanel.showNext();
                            AlphaAnimation alpha_in = new AlphaAnimation(0, 1);
                            alpha_in.setDuration(1000);
                            alpha_in.setFillAfter(true);
                            _messagePanel.startAnimation(alpha_in);
                        }

                    }
                    break;
                case NextImg:
                    if (isImgFliping) {

                        if (++_imgIndex < _imgList.size()) {
                            imgFlip.showNext();
//                            _imgIndex++;
                            sendEmptyMessageDelayed(NextImg, delayTime);
                        } else {
                            stopShowImg();
                            if (_playList != null && _playList.size() > 0) {
                                _playerImp.play();
                            } else {
//                                beginShowImg();
                                reStartShowImg();
                            }
                        }
                    }
//                    Log.e("tag", "handel_img_index:" + _imgIndex);


//                    _imgIndex++;
//                    if (_imgIndex < _imgList.size()) {
////                        imgFlip.showNext();
////                        sendEmptyMessageDelayed(NextImg, delayTime);
//                    } else {
//                        _imgIndex = 0;
////                        isImgFliping = false;
////                        imgFlip.stopFlipping();
//                    }
//                    Log.e("tag", "handel_img_index:" + _imgIndex);
//                    if (isImgFliping) {
//                        imgFlip.showNext();
//                        sendEmptyMessageDelayed(NextImg, delayTime);
//                    }
                    break;
            }
        }
    };

    private void stopShowImg() {
        if (isImgFliping) {
            _imgIndex = 0;
            isImgFliping = false;
            imgFlip.stopFlipping();
        }
    }

    private void reStartShowImg() {
        if (!isImgFliping) {
            _imgIndex = 0;
            isImgFliping = true;
//            imgFlip.startFlipping();
            uiHandler.sendEmptyMessage(NextImg);
        }
    }

    private void beginShowImg() {
        if (_videoLayout != null) {
            _videoLayout.removeAllViews();
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        _videoLayout.addView(imgFlip, layoutParams);
        _imgIndex = 0;
        if (_imgList.size() > 1) {
            if (!isImgFliping) {
                isImgFliping = true;
//            _imgIndex = 0;
                AlphaAnimation a_in = new AlphaAnimation(0, 1);
                a_in.setDuration(1000);
                a_in.setFillAfter(true);
                imgFlip.setInAnimation(a_in);
                AlphaAnimation a_out = new AlphaAnimation(1, 0);
                a_out.setDuration(1000);
                a_out.setFillAfter(true);
                imgFlip.setOutAnimation(a_out);

                uiHandler.sendEmptyMessageDelayed(NextImg, delayTime);

            }
        }
    }

    @Override
    protected void onPause() {

        if (_player != null) {
            Log.e("tag", "onPause");

            _player.stop();
            _player.release();
            _player = null;
        }
        if (imgFlip != null) {
            stopShowImg();
        }
        XObject.setRunningMode(this, XObject.RunningMode.Background);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        XObject.setRunningMode(this, XObject.RunningMode.Foreground);
        loadVideoList();
        loadDeviceList();
        isFliping = true;
        flipCount = 0;
        currentIndex = 0;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
