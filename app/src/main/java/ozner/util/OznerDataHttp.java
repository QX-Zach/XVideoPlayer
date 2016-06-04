package ozner.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.ozner.xvidoeplayer.DataRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;

/**
 * Created by ozner_67 on 2016/4/21.
 */
public class OznerDataHttp {

    private static final String GetWeatherUrl = "http://app.ozner.net:888/OznerServer/GetWeather";

    public interface OznerHttpCallback {
        void HandleResult(NetJsonObject result);
    }


    public static void getWeather(Context context, OznerHttpCallback callback) {
        List<NameValuePair> httpParms = new ArrayList<>();
        new NormalAsyncTask(context, GetWeatherUrl, httpParms, callback).execute();
    }


    //通用异步请求任务
    private static class NormalAsyncTask extends AsyncTask<String, Integer, NetJsonObject> {
        private Context mContext;
        private OznerHttpCallback httpCallback;
        private String httpUrl;
        private List<NameValuePair> httpParms;

        public NormalAsyncTask(Context context, String httpUrl, List<NameValuePair> httpParms, OznerHttpCallback callback) {
            this.mContext = context;
            this.httpCallback = callback;
            this.httpUrl = httpUrl;
            this.httpParms = httpParms;
        }

        @Override
        protected NetJsonObject doInBackground(String... params) {
            return OznerWebServer(httpUrl, httpParms);
        }

        @Override
        protected void onPostExecute(NetJsonObject netJsonObject) {
            if (httpCallback != null) {
                httpCallback.HandleResult(netJsonObject);
            }
        }
    }

    /*
  * 对应服务器接口 获取JSONObject对象
  * */
    public static NetJsonObject OznerWebServer(String url, List<NameValuePair> params) {
        NetJsonObject result = new NetJsonObject();
        result.state = 0;
//        }
        String response = CsirHttp.postGetString(url, params, 10000);
        if (response != null) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                result.state = Integer.parseInt(jsonObject.get("state").toString());
                result.value = response;
                return result;
            } catch (Exception ex) {
                result.state = 0;
                return result;
            }
        }
        return result;
    }
}
