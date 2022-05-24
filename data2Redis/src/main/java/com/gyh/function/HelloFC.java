package com.gyh.function;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FunctionInitializer;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.gyh.api.DataDefine;
import com.gyh.api.HttpUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by gyh on 2019/9/22.
 */
public class HelloFC implements StreamRequestHandler, FunctionInitializer {
    private StaticConf conf = null;

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Map<String, Object> map = conf.getMapper().readValue(inputStream, conf.getJavaType());
        //context.getLogger().info(conf.getMapper().writeValueAsString(map));
        //outputStream.write("hello world\n".getBytes());
        final String devicesName = DataDefine.get(map, DataDefine.deviceName);
        if (devicesName != null) {
            Map<String, Object> items = (Map) map.get("items");
            if (items != null) {
                for (Map.Entry<String, Object> entry : items.entrySet()) {
                    Map<String, Object> value = (Map) entry.getValue();
                    if (value != null) {
                        Object v = value.get("value");
                        String oldV = null;
                        if ("S50".equals(entry.getKey())) {
                            oldV = conf.getSyncCommands().hget(devicesName, "S50");
                            context.getLogger().info(devicesName + " oldV " + oldV);
                        }
                        conf.getSyncCommands().hset(devicesName, entry.getKey(), conf.getMapper().writeValueAsString(v));
                        if ("S50".equals(entry.getKey())) {
                            if (oldV != null && !oldV.equals(v.toString())) {   // 一定要toString，v是Integer类型的，比较是同不过
                                updateStatus(context, devicesName, v.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void initialize(Context context) throws IOException {
        try {
            conf = StaticConf.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            context.getLogger().info(e.getMessage());
        }
    }

    public void updateStatus(Context context, String deviceName, String value) {
        String res = HttpUtil.get("http://172.16.0.147/mebay/assist/device/updateStatus/" + deviceName);
        context.getLogger().info(deviceName + " push " + res);
        res = DataDefine.pushStatus("status", deviceName, value);
        context.getLogger().info(value + " " + res);
    }

}
