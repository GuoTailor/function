package com.gyh.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.HashMap;

/**
 * Created by gyh on 2019/10/23.
 */
public class StaticConf {
    private static StaticConf instance = null;
    private static final String redisUrl = "redis://:****@****:6379/1";
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
        javaType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        syncCommands.get("key");
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

    public RedisClient getRedisClient() {
        if (redisClient == null) {
            newInstance();
        }
        return redisClient;
    }

    public StatefulRedisConnection<String, String> getConnection() {
        if (connection == null) {
            newInstance();
        }
        return connection;
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

}
