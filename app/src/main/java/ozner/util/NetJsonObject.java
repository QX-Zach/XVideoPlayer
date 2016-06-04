package ozner.util;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by C-sir@hotmail.com  on 2015/12/12.
 */
public class NetJsonObject implements Serializable {
    public NetJsonObject(){}
    public int state;
    public String value;
    public JSONObject getJSONObject(){
        try {
            return new JSONObject(value);
        }catch (Exception ex)
        {
            return null;
        }
    }
}
