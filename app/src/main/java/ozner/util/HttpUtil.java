package ozner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhiyongxu on 15/10/20.
 */
public class HttpUtil {
    public static String get(String url) throws IOException
    {
        URL my_url = new URL(url);
        HttpURLConnection connection = null;

        connection = (HttpURLConnection) my_url.openConnection();
        connection.setReadTimeout(1000 * 60);

        connection.setConnectTimeout(1000 * 60);

        connection.setDoOutput(false);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        try {
            //connection.connect();

            InputStream inputStream = connection.getInputStream();
            try {
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuffer strBuffer = new StringBuffer();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuffer.append(line);
                }
                return strBuffer.toString();
            } finally {
                inputStream.close();
            }

        } finally {
            connection.disconnect();
        }
    }

    public static String postJSON(String url, String json, String charset) throws IOException {
        URL my_url = new URL(url);
        HttpURLConnection connection = null;

        connection = (HttpURLConnection) my_url.openConnection();
        connection.setReadTimeout(1000 * 60);

        connection.setConnectTimeout(1000 * 60);

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Length", String.valueOf(json.length()));
        //connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json");
        try {
            //connection.connect();
            OutputStream outputStream = connection.getOutputStream();
            try {
                outputStream.write(json.getBytes());
            } finally {
                outputStream.flush();
                //outputStream.close();
            }

            InputStream inputStream = connection.getInputStream();
            try {
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuffer strBuffer = new StringBuffer();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuffer.append(line);
                }
                return strBuffer.toString();
            } finally {
                inputStream.close();
            }

        } finally {
            connection.disconnect();
        }

    }
}
