package com.gyh.function;

import com.gyh.api.DeviceStatus;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gyh on 2019/10/21.
 */
public class StatusCache {
    private static StatusCache instance = null;
    private static final Set<DeviceStatus> deviceStatuses = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private StatusCache() {
    }

    public static StatusCache getInstance() {
        if (instance == null) {
            synchronized (StatusCache.class) {
                if (instance == null) {
                    instance = new StatusCache();
                }
            }
        }
        return instance;
    }

    public void addDeviceStatus(DeviceStatus deviceStatus) {
        deviceStatuses.add(deviceStatus);
    }

    public boolean remove(DeviceStatus deviceStatus) {
        return deviceStatuses.remove(deviceStatus);
    }
}
