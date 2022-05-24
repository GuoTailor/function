package com.gyh.api;

import java.util.Map;

/**
 * Created by gyh on 2019/9/25.
 */
public class DataDefine {
    public static String deviceName = "deviceName";
    public static String status = "status";
    public static String time = "time";

    public static String get(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    public static String pushStatus(String type, String deviceName, String status) {
        String res = HttpUtil.get("http://172.16.0.147/mebay/assist/push/message/"
                + deviceName + "?type=" + type + "&content=" + status);
        return deviceName + " " + type + " " + res;
    }
}
