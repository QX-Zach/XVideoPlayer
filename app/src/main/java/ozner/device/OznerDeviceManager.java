package ozner.device;

import android.content.Context;
import android.content.Intent;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ozner.XObject;
import ozner.util.Helper;
import ozner.util.SQLiteDB;
import ozner.util.dbg;

public class OznerDeviceManager extends XObject {

    /**
     * 新增一个配对设备广播
     */
    public final static String ACTION_OZNER_MANAGER_DEVICE_ADD = "com.ozner.manager.device.add";
    /**
     * 删除设备广播
     */
    public final static String ACTION_OZNER_MANAGER_DEVICE_REMOVE = "com.ozner.manager.device.remove";
    /**
     * 修改设备广播
     */
    public final static String ACTION_OZNER_MANAGER_DEVICE_CHANGE = "com.ozner.manager.device.change";

    public final static String ACTION_OZNER_MANAGER_OWNER_CHANGE = "com.ozner.manager.owner.change";

    static OznerDeviceManager instance;
    final HashMap<String, OznerDevice> devices = new HashMap<>();
    final DeviceManagerList mManagers;
    final IOManagerCallbackImp ioManagerCallbackImp = new IOManagerCallbackImp();

    SQLiteDB sqLiteDB;
    String owner = "";
    String token = "";
    IOManagerList ioManagerList;

    public OznerDeviceManager(Context context) throws InstantiationException {
        super(context);
        if (instance != null) {
            throw new InstantiationException();
        }
        instance = this;
        mManagers = new DeviceManagerList(context);
        sqLiteDB = new SQLiteDB(context);
        //导入老表
        importOldDB();
        ioManagerList = new IOManagerList(context);
        ioManagerList.setIoManagerCallback(ioManagerCallbackImp);

    }

    public static OznerDeviceManager Instance() {
        return instance;
    }

    public IOManagerList ioManagerList() {
        return ioManagerList;
    }


    /**
     * 获取用户对应的表名
     */
    private String getOwnerTableName() {

        if (Helper.StringIsNullOrEmpty(owner)) return null;
        return "A" + Helper.MD5(owner.trim());
    }

    private void importOldDB() {
        HashSet<String> ownerList = new HashSet<>();

        try {
            List<String[]> result = sqLiteDB.ExecSQL("SELECT DISTINCT name from OznerDevices", new String[0]);
            for (String[] list : result) {
                String owner = list[0];
                if (owner != null) {
                    owner = owner.trim();
                }

                if (Helper.StringIsNullOrEmpty(owner)) continue;

                if (!ownerList.contains(owner))
                    ownerList.add(owner);
            }
        } catch (Exception ignored) {
        }


        try {
            List<String[]> result = sqLiteDB.ExecSQL("SELECT DISTINCT name from CupSetting", new String[0]);
            for (String[] list : result) {
                String owner = list[0];
                if (owner != null)
                    owner = owner.trim();
                if (Helper.StringIsNullOrEmpty(owner)) continue;
                if (!ownerList.contains(owner))
                    ownerList.add(owner);
            }
        } catch (Exception ignored) {
        }

        for (String owner : ownerList) {
            String table = Helper.MD5(owner);
            String Sql = String.format("CREATE TABLE IF NOT EXISTS %s (Address VARCHAR PRIMARY KEY NOT NULL,Type Text NOT NULL,JSON TEXT)", table);
            sqLiteDB.execSQLNonQuery(Sql, new String[]{});
            try {
                String sql = String.format("INSERT INTO %s (Address,Type,JSON) SELECT Address,'CUP001',JSON from CupSetting where Owner=?", table);
                sqLiteDB.execSQLNonQuery(sql, new String[]{owner});
            } catch (Exception ignored) {

            }

            try {
                String sql = String.format("INSERT INTO %s (Address,Type,JSON) SELECT Address,Model,JSON from OznerDevices where Owner=?", table);
                sqLiteDB.execSQLNonQuery(sql, new String[]{owner});
            } catch (Exception ignored) {

            }
        }
        try {
            sqLiteDB.execSQLNonQuery("DROP TABLE CupSetting", new String[]{});
            sqLiteDB.execSQLNonQuery("DROP TABLE OznerDevices", new String[]{});
        } catch (Exception ignored) {

        }
    }

    protected String Owner() {
        return owner;
    }

    /**
     * 设置绑定的用户
     *
     * @param owner 用户ID
     */
    public void setOwner(String owner, String token) {
        if (owner != null)
            owner = owner.trim();
        if (Helper.StringIsNullOrEmpty(owner)) return;
        if (this.owner != null) {
            if (this.owner.equals(owner)) return;
        }
        this.owner = owner;
        this.token = token;
        synchronized (this) {
            devices.clear();
        }


        if (Helper.StringIsNullOrEmpty(owner))
            ioManagerList().Stop();

        ioManagerList().Start(owner,token);
        CloseAll();
        LoadDevices();

        String Sql = String.format("CREATE TABLE IF NOT EXISTS %s (Address VARCHAR PRIMARY KEY NOT NULL,Type Text NOT NULL,JSON TEXT)", getOwnerTableName());
        sqLiteDB.execSQLNonQuery(Sql, new String[]{});
        dbg.i("Set Owner:%s", owner);
        context().sendBroadcast(new Intent(ACTION_OZNER_MANAGER_OWNER_CHANGE));

    }

    protected void CloseAll() {
        ioManagerList.closeAll();
    }

    private void LoadDevices() {
        String sql = String.format("select Address,Type,JSON from %s", getOwnerTableName());
        List<String[]> list = sqLiteDB.ExecSQL(sql, new String[]{});
        synchronized (devices) {
            for (String[] v : list) {
                String Address = v[0];
                String Model = v[1];
                String Json = v[2];
                if (!devices.containsKey(Address)) {
                    for (BaseDeviceManager mgr : mManagers) {
                        OznerDevice device = mgr.loadDevice(Address, Model, Json);
                        if (device != null) {
                            devices.put(Address, device);
                            BaseDeviceIO io = ioManagerList.getAvailableDevice(Address);
                            if (io != null) {
                                try {
                                    device.Bind(io);
                                } catch (DeviceNotReadyException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 判断一个设备是否配对过
     *
     * @param address 设备MAC
     * @return True配对过
     */
    public boolean hashDevice(String address) {
        synchronized (devices) {
            return devices.containsKey(address);
        }
    }

    /**
     * 删除一个已经配对的设备
     */
    public void remove(OznerDevice device) {
        String sql = String.format("delete from %s where Address=?", getOwnerTableName());
        sqLiteDB.execSQLNonQuery(sql, new String[]{device.Address()});

        String address = device.Address();
        synchronized (devices) {
            if (devices.containsKey(address)) {
                devices.remove(address);
            }
        }
        Intent intent = new Intent(ACTION_OZNER_MANAGER_DEVICE_REMOVE);
        intent.putExtra("Address", address);
        context().sendBroadcast(intent);

//        if (device.IO() != null) {
//            device.IO().close();
//        }
        try {
            device.Bind(null);
        } catch (DeviceNotReadyException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取所有设备集合
     */
    public OznerDevice[] getDevices() {
        synchronized (devices) {
            return devices.values().toArray(new OznerDevice[devices.size()]);
        }
    }

//	/**
//	 * 通过蓝牙设备获取一个设备控制对象
//	 *
//	 */
//	public OznerDevice getDevice(OznerBluetoothDevice bluetooth)
//			throws NotSupportDeviceException {
//		String address = bluetooth.getAddress();
//		OznerDevice device = getDevice(address);
//		if (device == null) {
//			ArrayList<BaseDeviceManager> list = getManagers();
//			for (BaseDeviceManager mgr : list) {
//				device = mgr.getDevice(bluetooth);
//				if (device != null)
//					return device;
//			}
//		}
//		return device;
//	}

    /**
     * 通过MAC地址获取已经保存的设备
     */
    public OznerDevice getDevice(String address) {
        synchronized (devices) {
            if (devices.containsKey(address))
                return devices.get(address);
            else
                return null;
        }
    }

    /**
     * 通过一个IO接口来构造或者查找一个对应的设备
     *
     * @param io 接口实例
     * @return 返回NULL无对应的设备
     */
    public OznerDevice getDevice(BaseDeviceIO io) throws NotSupportDeviceException {
        synchronized (devices) {
            if (devices.containsKey(io.getAddress())) {
                return devices.get(io.getAddress());
            }
        }
        for (BaseDeviceManager mgr : mManagers) {
            if (mgr.isMyDevice(io.Type)) {
                OznerDevice device = mgr.loadDevice(io.getAddress(), io.Type, "");
                if (device != null) {
                    try {
                        device.Bind(io);
                        return device;
                    } catch (DeviceNotReadyException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        throw new NotSupportDeviceException();
    }

    public BaseDeviceIO[] getNotBindDevices() {
        ArrayList<BaseDeviceIO> list = new ArrayList<>();
        ArrayList<BaseDeviceIO> result = new ArrayList<>();
        Collections.addAll(list, ioManagerList.getAvailableDevices());

        synchronized (devices) {
            for (BaseDeviceIO io : list) {
                if (!devices.containsKey(io.getAddress()))
                    result.add(io);
            }
        }
        return result.toArray(new BaseDeviceIO[result.size()]);
    }

    public OznerDevice getDevice(String address, String type, String settings) {

        for (BaseDeviceManager mgr : mManagers) {
            if (mgr.isMyDevice(type)) {
                OznerDevice device = mgr.loadDevice(address, type, settings);
                if (device != null) {
                    return device;
                }
            }
        }

        return null;
    }

    /**
     * 判断设备是否处于可配对状态
     */
    public boolean checkisBindMode(BaseDeviceIO io) {
        for (BaseDeviceManager mgr : mManagers) {
            if (mgr.isMyDevice(io.getType())) {
                return mgr.checkIsBindMode(io);
            }
        }
        return false;
    }

    /**
     * 保存并绑定设备设置
     */
    public void save(OznerDevice device) {

        String Address = device.Address();
        boolean isNew;

        synchronized (devices) {

            if (Helper.StringIsNullOrEmpty(Owner())) return;

            if (!devices.containsKey(Address)) {
                devices.put(Address, device);
                isNew = false;
            } else
                isNew = true;
        }
        device.saveSettings();

        String sql = String.format("INSERT OR REPLACE INTO %s (Address,Type,JSON) VALUES (?,?,?);", getOwnerTableName());

        sqLiteDB.execSQLNonQuery(sql,
                new String[]{device.Address(), device.Type(),
                        device.Setting().toString()});

        Intent intent = new Intent();
        intent.putExtra("Address", Address);
        intent.setAction(isNew ? ACTION_OZNER_MANAGER_DEVICE_ADD
                : ACTION_OZNER_MANAGER_DEVICE_CHANGE);
        context().sendBroadcast(intent);
        device.updateSettings(); //刷新设置变更
    }

//    private ArrayList<BaseDeviceManager> getManagers() {
//        ArrayList<BaseDeviceManager> list = new ArrayList<>();
//        synchronized (this) {
//            list.addAll(mManagers);
//        }
//        return list;
//    }


    class IOManagerCallbackImp implements IOManager.IOManagerCallback {
        @Override
        public void onDeviceAvailable(IOManager manager, BaseDeviceIO io) {
            if (io != null) {
                OznerDevice device = getDevice(io.getAddress());

                if (device != null) {
                    try {
                        device.Bind(io);
                    } catch (DeviceNotReadyException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onDeviceUnavailable(IOManager manager, BaseDeviceIO io) {
            if (io != null) {
                OznerDevice device = getDevice(io.getAddress());
                if (device != null) {
                    try {
                        device.Bind(null);
                    } catch (DeviceNotReadyException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
//    /**
//     * 注册一个设备管理器
//     */
//    public void registerManager(BaseDeviceManager manager) {
//        synchronized (mManagers) {
//            if (!mManagers.contains(manager)) {
//                mManagers.add(manager);
//            }
//        }
//    }
//    /**
//     * 注销设备管理器
//     */
//    public void unregisterManager(BaseDeviceManager manager) {
//        synchronized (mManagers) {
//            if (mManagers.contains(manager))
//                mManagers.remove(manager);
//        }
//    }


}
