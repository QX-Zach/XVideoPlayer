package ozner.WaterPurifier;


import ozner.util.ByteUtil;

/**
 * Created by xzyxd on 2015/11/2.
 */
class WaterPurifierStatusPacket {
    public final static int WaterPurifierError=0xffff;
    /**
     * 加热
     */
    public Boolean Hot = false;
    /**
     * 制冷
     */
    public Boolean Cool = false;
    /**
     * 电源开关
     */
    public Boolean Power = false;
    /**
     * 杀菌
     */
    public Boolean Sterilization = false;

    public int TDS1 = WaterPurifierError;
    public int TDS2 = WaterPurifierError;
    public WaterPurifierStatusPacket()
    {

    }
    public WaterPurifierStatusPacket(WaterPurifierStatusPacket status)
    {
        this.Hot=status.Hot;
        this.Cool=status.Cool;
        this.Power=status.Power;
        this.Sterilization=status.Sterilization;
    }


    public void fromBytes(byte[] bytes) {
        this.Hot = bytes[12] != 0;
        this.Cool = bytes[13] != 0;
        this.Power = bytes[14] != 0;
        this.Sterilization = bytes[15] != 0;
        this.TDS1 = ByteUtil.getShort(bytes, 16);
        this.TDS2 = ByteUtil.getShort(bytes, 18);
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (Hot ? 1 : 0);
        bytes[1] = (byte) (Cool ? 1 : 0);
        bytes[2] = (byte) (Power ? 1 : 0);
        bytes[3] = (byte) (Sterilization ? 1 : 0);
        return bytes;
    }

}
