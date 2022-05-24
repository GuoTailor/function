package com.gyh.api;

import java.util.Objects;

/**
 * Created by gyh on 2019/10/21.
 */
public class DeviceStatus {
    public String deviceName;
    public String content;
    public Long time;

    public DeviceStatus(){}

    public DeviceStatus(String deviceName, String content, Long time) {
        this.deviceName = deviceName;
        this.content = content;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceStatus that = (DeviceStatus) o;

        if (!Objects.equals(deviceName, that.deviceName)) return false;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        int result = deviceName != null ? deviceName.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
