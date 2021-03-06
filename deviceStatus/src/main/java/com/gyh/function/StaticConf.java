package com.gyh.function;

import com.aliyun.fc.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.gyh.api.DeviceStatus;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by gyh on 2019/10/23.
 */
public class StaticConf {
    private static final ConcurrentHashMap<String, DeviceStatus> deviceStatuses = new ConcurrentHashMap<>();
    private static final String redisUrl = "redis://:****@****:6379/1";
    public static final String online = "online";
    public static final String offline = "offline";
    private static StaticConf instance = null;
    private ObjectMapper mapper;
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    private MapType javaType;

    private StaticConf() {
        mapper = new ObjectMapper();
        redisClient = RedisClient.create(redisUrl);
        connection = redisClient.connect();
        syncCommands = connection.sync();
        syncCommands.get("key");
        javaType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
    }

    public static StaticConf getInstance() {
        if (instance == null) {
            synchronized (StaticConf.class) {
                if (instance == null) {
                    instance = new StaticConf();
                }
            }
        }
        return instance;
    }

    private void newInstance() {
        synchronized (StaticConf.class) {
            if (mapper == null) {
                mapper = new ObjectMapper();
            }
            if (redisClient == null) {
                redisClient = RedisClient.create(redisUrl);
            }
            if (connection == null) {
                connection = redisClient.connect();
            }
            if (syncCommands == null) {
                syncCommands = connection.sync();
            }
            if (javaType == null) {
                javaType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
            }
        }
    }

    public ObjectMapper getMapper() {
        if (mapper == null) {
            newInstance();
        }
        return mapper;
    }

    public RedisCommands<String, String> getSyncCommands() {
        if (syncCommands == null) {
            newInstance();
        }
        return syncCommands;
    }

    public MapType getJavaType() {
        if (javaType == null) {
            newInstance();
        }
        return javaType;
    }

    /**
     * @return true??????????????????false???????????????
     */
    public Boolean addDeviceStatus(DeviceStatus deviceStatus, long timeout, Context context) {
        DeviceStatus oldStatus = deviceStatuses.get(deviceStatus.deviceName);
        if ((oldStatus != null && !oldStatus.content.equals(deviceStatus.content))          // ???????????????????????????
                && (Math.abs(deviceStatus.time - oldStatus.time) <= timeout)                // ?????????????????????????????????
                && (online.equals(deviceStatus.content) || offline.equals(deviceStatus.content))// ??????????????????????????????
                && (online.equals(oldStatus.content) || offline.equals(oldStatus.content))) {   // ??????????????????????????????

            deviceStatuses.remove(deviceStatus.deviceName);     // ????????????????????????
            context.getLogger().info("???????????? " + deviceStatus.deviceName
                    + " new " + deviceStatus.content + " " + deviceStatus.time
                    + " old " + oldStatus.content + " " + oldStatus.time
                    + "  " + (deviceStatus.time - oldStatus.time));
            return false;
        } else {
            deviceStatuses.put(deviceStatus.deviceName, deviceStatus);
            return true;
        }
    }

    public boolean remove(DeviceStatus deviceStatus) {
        return deviceStatuses.remove(deviceStatus.deviceName) != null;
    }

    public void forEachRemove(String deviceName, Consumer<DeviceStatus> operation) {
        Iterator<Map.Entry<String, DeviceStatus>> it = deviceStatuses.entrySet().iterator();
        while (it.hasNext()) {
            DeviceStatus ds = it.next().getValue();
            if (deviceName.equals(ds.deviceName)) {
                it.remove();
                operation.accept(ds);
            }
        }
    }
}
