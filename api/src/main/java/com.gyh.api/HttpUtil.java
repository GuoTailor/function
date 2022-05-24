package com.gyh.api;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by GYH on 2018/12/17.
 */
public class HttpUtil {
    private HttpUtil() {
    }

    /**
     * 发起https 请求
     *
     * @param params 请求参数
     * @return 请求结果
     */
    public static String requestData(String strUrl, String params) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");// POST
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            // 设置参数;设置参数后
            if (params != null) {
                conn.setDoOutput(true);
                OutputStream out = conn.getOutputStream();
                out.write(params.getBytes(StandardCharsets.UTF_8));
                out.flush();
                out.close();
            }
            conn.connect();
            //得到结果
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return parsRtn(conn.getInputStream());
            } else {
                System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String get(String urlStr) {
        String data = null;
        InputStream is;
        HttpURLConnection conn;//声明连接对象
        try {
            URL url = new URL(urlStr); //URL对象
            conn = (HttpURLConnection) url.openConnection(); //使用URL打开一个链接,下面设置这个连接
            conn.setRequestMethod("GET"); //使用get请求
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() == 200) {//返回200表示连接成功
                is = conn.getInputStream(); //获取输入流
                //System.out.println("get方法取回内容："+resultData);
                data = parsRtn(is);
                is.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Function  :   处理服务器的响应结果（将输入流转化成字符串）
     * Param     :   inputStream服务器的响应输入流
     */
    private static String parsRtn(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
    }

}
