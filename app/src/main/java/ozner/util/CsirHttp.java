package ozner.util;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by C-sir@hotmail.com  on 2015/12/4.
 */
public class CsirHttp {
    private final static int RetryCount = 1;

    public static JSONObject postGetJsonObject(String url, List<NameValuePair> params) {
        return null;
    }

    /*
    * 基础网络访问对象
    * */
    private static String postGetString(String url, List<NameValuePair> params, RequestConfig requestConfig) {
        try {
            //声明HttpClient对象
            CloseableHttpClient httpclient = HttpClients.createDefault();
//            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();//设置请求和传输超时时间
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);  //设置配置
            UrlEncodedFormEntity entity=new UrlEncodedFormEntity(params, HTTP.UTF_8);
            httpPost.setEntity(entity);
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            try {
                HttpEntity entity2 = response2.getEntity();
                if (entity2 != null) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity2.getContent(), "UTF-8"), 8 * 1024);
                        StringBuilder entityStringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            entityStringBuilder.append(line);
                        }
                        // 利用从HttpEntity中得到的String生成JsonObject
                        return entityStringBuilder.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                //消耗掉response
                EntityUtils.consume(entity2);
            } finally {
                response2.close();
                httpclient.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return null;
    }
    /*
    * 包含失败自动重试次数 网络请求获取String
    * */
    public static String postGetString(String url, List<NameValuePair> params, int timeout) {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();//设置请求和传输超时时间
        //Null 重试次数
        for (int i = 0; i < RetryCount; i++) {
            String value = postGetString(url, params, requestConfig);
            if (value != null) {
                return value;
            }
            Log.e("CsirNetWorkRetry:" + i, url);
        }
        return null;
    }

    /*
    *
    * */
    public static JSONObject postGetJsonObject(String url, List<NameValuePair> params, int timeout) {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();//设置请求和传输超时时间
        //Null 重试次数
        for (int i = 0; i < RetryCount; i++) {
            String value = postGetString(url, params, requestConfig);
            if (value != null) {
                try {
                    return new JSONObject(value);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            Log.e("CsirNetWorkRetry:" + i, url);
        }
        return null;
    }
}
