package com.example.suxiongye.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.suxiongye.bean.Charging;
import com.example.suxiongye.charging.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suxiongye on 4/28/17.
 */
public class ChargingData {

    //用于处理
    private Handler handler;

    private final String host = "http://123.206.49.122:8091/chargings/api";

    public ChargingData(Handler handler) {
        this.handler = handler;
    }

    /**
     * 获取所有充电桩信息
     */
    public void getChargingData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                httpRequest(host);
            }
        }).start();
    }

    /**
     * 开启充电桩
     *
     * @param id
     */
    public void openCharging(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String request = host + "/open/" + id;
                httpRequest_string(request);
            }
        }).start();
    }

    /**
     * 关闭充电桩
     *
     * @param id
     */
    public void closeCharging(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String request = host + "/close/" + id;
                httpRequest_string(request);
            }
        }).start();
    }

    /**
     * 查看充电桩是否开启
     *
     * @param id
     */
    public void isOpenCharging(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String request = host + "/isopen/" + id;
                httpRequest_string(request);
            }
        }).start();
    }

    /**
     * 向服务器发送请求。获取充电桩信息
     *
     * @param request
     */
    private void httpRequest(String request) {
        try {
            URL url = new URL(request);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            Message message = new Message();
            if (responseCode == 200) {
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];
                int len = -1;
                while ((len = is.read(buff)) != -1) {
                    out.write(buff, 0, len);
                }
                is.close();
                String result = out.toString();
                out.close();
                Log.e("http", result);
                message = new Message();
                message.what = MainActivity.SHOW_CHARGING;
                message.obj = parser(result);
                handler.sendMessage(message);
            }
        } catch (Exception e) {
            Message message = new Message();
            message.what = MainActivity.SHOW_RESPONSE;
            message.obj = e.toString();
            handler.sendMessage(message);
            e.printStackTrace();
        }

    }

    /**
     * 向服务器发送请求，获取结果
     *
     * @param request
     */
    private void httpRequest_string(String request) {
        try {
            URL url = new URL(request);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            Message message = new Message();
            if (responseCode == 200) {
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];
                int len = -1;
                while ((len = is.read(buff)) != -1) {
                    out.write(buff, 0, len);
                }
                is.close();
                String result = out.toString();
                out.close();
                Log.e("http", result);
                message.what = MainActivity.SHOW_RESPONSE;
                message.obj = result;
                handler.sendMessage(message);
            }
        } catch (Exception e) {
            Message message = new Message();
            message.what = MainActivity.SHOW_RESPONSE;
            message.obj = e.toString();
            handler.sendMessage(message);
            e.printStackTrace();
        }
    }

    private List<Charging> parser(String str) {
        ArrayList<Charging> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(str);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jObject = jsonArray.getJSONObject(i);
                int id = jObject.getInt("id");
                String name = jObject.getString("name");
                Double latitude = jObject.getDouble("latitude");
                Double longitude = jObject.getDouble("longitude");
                String status = jObject.getString("status");
                String used = jObject.getString("used");
                Charging charging = new Charging();
                charging.setId(id);
                charging.setName(name);
                charging.setLatitude(latitude);
                charging.setLongitude(longitude);
                charging.setStatus(status);
                charging.setUsed(used);
                list.add(charging);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
