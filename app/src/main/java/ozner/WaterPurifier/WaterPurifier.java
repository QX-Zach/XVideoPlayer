package ozner.WaterPurifier;

import android.content.Context;

import com.ozner.xvidoeplayer.R;

import java.util.Timer;
import java.util.TimerTask;

import ozner.device.BaseDeviceIO;
import ozner.device.OperateCallback;
import ozner.device.OznerDevice;

/**
 * Created by xzyxd on 2015/11/2.
 */
public abstract class WaterPurifier extends OznerDevice {
    public static final String ACTION_WATER_PURIFIER_STATUS_CHANGE = "com.ozner.water.purifier.statusPacket.change";
    final Sensor sensor=new Sensor();
    final Status status=new Status();
    protected final WaterPurifierInfo info=new WaterPurifierInfo();
    boolean isOffline = true;
    Timer autoUpdateTimer = null;


    public WaterPurifier(Context context, String Address, String Type, String Setting) {
        super(context, Address, Type, Setting);
    }

    public class Sensor
    {
        public int TDS1()
        {
            return getTDS1();
        }
        public int TDS2()
        {
            return getTDS2();
        }
        @Override
        public String toString() {
            return String.format("TDS1:%d TDS2:%d", TDS1(),TDS2());
        }
    }
    public class Status
    {
        public boolean Power()
        {
            return getPower();
        }

        /**
         * 打开电源
         *
         * @param Power 开关
         * @param cb    状态回调
         */
        public void setPower(boolean Power, OperateCallback<Void> cb) {
            if (IO() == null) {
                cb.onFailure(null);
            }
            WaterPurifier.this.setPower(Power,cb);
        }
        public boolean Hot() {
            return getHot();
        }

        /**
         * 打开加热
         *
         * @param Hot 开关
         * @param cb  状态回调
         */
        public void setHot(boolean Hot, OperateCallback<Void> cb) {
            if (IO() == null) {
                cb.onFailure(null);
            }
            WaterPurifier.this.setHot(Hot,cb);
        }

        public boolean Cool() {
            return getCool();
        }

        /**
         * 打开制冷
         *
         * @param Cool 开关
         * @param cb   状态回调
         */
        public void setCool(boolean Cool, OperateCallback<Void> cb) {
            if (IO() == null) {
                cb.onFailure(null);
            }
            WaterPurifier.this.setCool(Cool,cb);
        }


        public boolean Sterilization() {
            return getSterilization();
        }

        /**
         * 打开杀菌
         *
         * @param Sterilization 开关
         * @param cb            状态回调
         */
        public void setSterilization(boolean Sterilization, OperateCallback<Void> cb) {
            if (IO() == null) {
                cb.onFailure(null);
            }
            WaterPurifier.this.setSterilization(Sterilization,cb);
        }

        @Override
        public String toString() {
            return String.format("Power:%s Hot:%s Cool:%s Sterilization:%s",
                    String.valueOf(Power()), String.valueOf(Hot()), String.valueOf(Cool()),
                    String.valueOf(Sterilization()));
        }
    }



    public Sensor sensor()
    {
        return sensor;
    }
    public Status status()
    {
        return status;
    }
    public WaterPurifierInfo info() {return info;}


    @Override
    public String toString() {
        if (isOffline())
        {
            return "offline";
        }else {
            return String.format("Status:%s\nSensor:%s",status.toString(),sensor.toString());
        }
    }

    @Override
    protected String getDefaultName() {
        return context().getString(R.string.water_purifier_name);
    }


    public boolean isOffline() {
        return isOffline;
    }


    @Override
    protected void doSetDeviceIO(BaseDeviceIO oldIO, BaseDeviceIO newIO) {
        if (newIO==null)
        {
            setOffline(true);
        }
    }


    protected void setOffline(boolean isOffline)
    {
        if (isOffline!=this.isOffline) {
            this.isOffline = isOffline;
            doUpdate();
        }
    }
    protected void doTime()
    {

    }

    protected void startTimer()
    {
        if (autoUpdateTimer != null)
            cancelTimer();
        autoUpdateTimer = new Timer();
        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                doTime();
            }
        }, 100, 5000);
    }

    protected void cancelTimer() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer.purge();
            autoUpdateTimer = null;
        }
    }

    protected abstract int getTDS1();
    protected abstract int getTDS2();

    protected abstract boolean getPower();
    protected abstract void setPower(boolean Power, OperateCallback<Void> cb);

    protected abstract boolean getHot();
    protected abstract void setHot(boolean Hot, OperateCallback<Void> cb);

    protected abstract boolean getCool();
    protected abstract void setCool(boolean Cool, OperateCallback<Void> cb);

    protected abstract boolean getSterilization();
    protected abstract void setSterilization(boolean Sterilization, OperateCallback<Void> cb);

}
