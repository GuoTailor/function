package com.gyh.function;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FunctionInitializer;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.gyh.api.DataDefine;
import com.gyh.api.DeviceStatus;
import com.gyh.api.HttpUtil;
import com.gyh.api.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.UUID;

/**
 * Created by gyh on 2019/10/13.
 */
public class DeviceONOFF implements StreamRequestHandler, FunctionInitializer {
    private static final String DISTRIBUTED_LOCK = "DISTRIBUTED_LOCK";  // 分布式锁key，进程统一
    private static final String PROCESS_ID = ManagementFactory.getRuntimeMXBean().getName() + UUID.randomUUID();    // 分布式锁value，进程唯一
    private static final int LOCK_TIME_OUT = 2;  // 分布式锁超时时间,以秒为单位
    private static final long timeOut = 10_000;    // 防抖延时
    private StaticConf conf = null;
    private RedisDistributedLock redisDistributedLock;

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Map<String, Object> map = conf.getMapper().readValue(inputStream, conf.getJavaType());
        context.getLogger().info(conf.getMapper().writeValueAsString(map) + PROCESS_ID);
        //{"time":"2019-10-17 16:28:27.916","deviceName":"DTU201905060128","status":"online"}
        String devicesName = DataDefine.get(map, DataDefine.deviceName);
        String time = DataDefine.get(map, DataDefine.time);
        String status = DataDefine.get(map, DataDefine.status);
        if (devicesName != null && status != null) {
            Long newTimeLong = Util.encoderDate("yyyy-MM-dd HH:mm:ss.SSS", time);
            synchronized (DeviceONOFF.class) {
                try {
                    redisDistributedLock.tryLock(DISTRIBUTED_LOCK, PROCESS_ID, LOCK_TIME_OUT, context.getLogger());
                    String oldTime = conf.getSyncCommands().hget(devicesName, DataDefine.time);
                    if (oldTime == null) {
                        oldTime = time;
                    }
                    Long oldTimeLong = Util.encoderDate("yyyy-MM-dd HH:mm:ss.SSS", oldTime);
                    if (oldTimeLong == null || newTimeLong == null) {
                        context.getLogger().error("时间格式化失败 oldTime：" + conf.getSyncCommands().hget(devicesName, DataDefine.time) + " newTime" + time);
                        return;
                    }
                    if (newTimeLong - oldTimeLong < 0) {
                        context.getLogger().info(devicesName + " 过时 (" + status + ") newTime:" + time + " lodTime:" + oldTime);
                    } else {
                        context.getLogger().info(devicesName + " 设置 " + status + " newTime:" + time + " lodTime:" + oldTime);
                        conf.getSyncCommands().hset(devicesName, DataDefine.deviceName, devicesName);
                        conf.getSyncCommands().hset(devicesName, DataDefine.time, time);
                        conf.getSyncCommands().hset(devicesName, DataDefine.status, status);
                    }
                } finally {
                    redisDistributedLock.tryUnlock(DISTRIBUTED_LOCK, PROCESS_ID, context.getLogger());
                }
            }
            DeviceStatus device = new DeviceStatus(devicesName, status, newTimeLong);
            if (conf.addDeviceStatus(device, timeOut, context)) {   // 添加不能放在锁里面，不然同一时间只能添加一次
                try {
                    Thread.sleep(timeOut);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (DeviceONOFF.class) {
                conf.forEachRemove(device.deviceName, deviceStatus -> push(context, deviceStatus));
            }
        }
    }

    @Override
    public void initialize(Context context) {
        try {
            conf = StaticConf.getInstance();
            redisDistributedLock = new RedisDistributedLock();
        } catch (Exception e) {
            context.getLogger().error(e.getMessage());
            throw e;
        }
    }

    public void push(Context context, DeviceStatus device) {
        String res = HttpUtil.get("http://172.16.0.147/mebay/assist/device/updateStatus/" + device.deviceName);
        context.getLogger().info(device.deviceName + " push " + res);
        context.getLogger().info(DataDefine.pushStatus("status", device.deviceName, device.content) + " " + device.content);
    }

}
