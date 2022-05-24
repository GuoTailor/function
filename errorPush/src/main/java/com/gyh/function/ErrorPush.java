package com.gyh.function;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FunctionInitializer;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.gyh.api.DataDefine;
import com.gyh.api.DeviceStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gyh on 2019/10/21.
 */
public class ErrorPush implements StreamRequestHandler, FunctionInitializer {
    private ObjectMapper mapper;
    private MapType javaType;
    private Context context;

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        Map<String, Object> map = mapper.readValue(input, javaType);
        context.getLogger().info(mapper.writeValueAsString(map));
        String devicesName = DataDefine.get(map, DataDefine.deviceName);
        String time = DataDefine.get(map, DataDefine.time);
        if (devicesName != null) {
            Map<String, Object> value = mapper.readValue(((Map) map.get("value")).get("ERROR_CODE").toString(), javaType);
            if (value != null) {
                List<DeviceStatus> list = new ArrayList<>(value.size() + 1);
                for (Map.Entry<String, Object> entry : value.entrySet()) {
                    DeviceStatus device = new DeviceStatus(devicesName, entry.getKey(), null);
                    list.add(device);
                    StatusCache.getInstance().addDeviceStatus(device);
                }
                try {
                    Thread.sleep(3_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (DeviceStatus device : list) {
                    if (StatusCache.getInstance().remove(device)) {
                        String log = DataDefine.pushStatus("error", device.deviceName, device.content);
                        context.getLogger().info(device.deviceName + " " + log);
                    }
                }
            }
        }
    }

    @Override
    public void initialize(Context context) throws IOException {
        this.context = context;
        mapper = new ObjectMapper();
        javaType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
    }

}
