package ozner.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by C-sir@hotmail.com  on 2015/12/25.
 */
public class NetWeather {
    public String weatherform;
    public String city;//城市
    public String aqi; //空气质量指数
    public String co;//一氧化碳1小时平均值(ug/m³)
    public String no2; //二氧化氮1小时平均值(ug/m³)
    public String o3;//臭氧1小时平均值(ug/m³)
    public String pm10; //PM10 1小时平均值(ug/m³)
    public String pm25; //PM2.5 1小时平均值(ug/m³)
    public String qlty; //空气质量类别
    public String so2; //二氧化硫1小时平均值(ug/m³)
    public String hum; //相对湿度（%）
    public String tmp; //温度
    public boolean fromJSONObject(JSONObject jsonObject)
    {
        boolean success=false;
        boolean success2=false;
        try
        {
            weatherform=jsonObject.getString("weatherform");
            JSONObject jsonObjectdata=new JSONObject(jsonObject.getString("data"));
        JSONArray jsonArray=jsonObjectdata.getJSONArray("HeWeather data service 3.0");
        for(int i=0;i<jsonArray.length();i++)
        {
            JSONObject jsonObject1=jsonArray.getJSONObject(i);
            if(jsonObject1.get("now")!=null)
            {
                JSONObject jsonObject2=jsonObject1.getJSONObject("now");
                hum=jsonObject2.getString("hum");
                tmp=jsonObject2.getString("tmp");
                success=true;
            }
            if(jsonObject1.get("basic")!=null)
            {
                JSONObject jsonObject2=jsonObject1.getJSONObject("basic");
                city=jsonObject2.getString("city");
            }
            if(jsonObject1.get("aqi")!=null)
            {
                JSONObject jsonObject2=jsonObject1.getJSONObject("aqi").getJSONObject("city");
                aqi=jsonObject2.getString("aqi");
                co=jsonObject2.getString("co");
                no2=jsonObject2.getString("no2");
                o3=jsonObject2.getString("o3");
                pm10=jsonObject2.getString("pm10");
                pm25=jsonObject2.getString("pm25");
                so2=jsonObject2.getString("so2");
                qlty=jsonObject2.getString("qlty");
                success2=true;
            }
        }
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
        if(success&&success2)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
