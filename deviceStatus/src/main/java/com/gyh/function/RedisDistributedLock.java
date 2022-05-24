package com.gyh.function;

import com.aliyun.fc.runtime.FunctionComputeLogger;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * Created by gyh on 2019/11/27.
 */
public class RedisDistributedLock {
    private RedisCommands<String, String> redisCommands;

    public RedisDistributedLock() {
        this.redisCommands = StaticConf.getInstance().getSyncCommands();
    }

    public void tryLock(String lockKey, String lockValue, int lockTimeout, FunctionComputeLogger log) {
        while (!lock(lockKey, lockValue, lockTimeout, log)) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean lock(String lockKey, String lockValue, int lockTimeout, FunctionComputeLogger log) {
        try {
            String lock = redisCommands.get(lockKey);
            if (lock != null) {
                return false;
            } else {
                //返回 null, key 已经存在了, 说明有其他客户端持有锁
                String setValue = redisCommands.set(lockKey, lockValue, SetArgs.Builder.nx().ex(lockTimeout));
                return setValue != null;
            }
        } catch (Exception e) {
            log.error("获取锁错误, error = " + e.getMessage());
            return false;
        }
    }

    public boolean tryUnlock(String lockKey, String lockValue, FunctionComputeLogger log) {
        try {
            // String scriptTemplate = "if redis.call('get', '%s') == '%s' then return redis.call('del', '%s') else return 0 end";
            // String script = String.format(scriptTemplate, lockKey, lockValue, lockKey);
            // long result = redisCommands.eval(script, ScriptOutputType.INTEGER);
            String scriptTemplate = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            long result = redisCommands.eval(scriptTemplate, ScriptOutputType.INTEGER, new String[]{lockKey}, lockValue);
            return result == 1;
        } catch (Exception e) {
            log.error("释放锁错误, error = " + e.getMessage());
            return Boolean.FALSE;
        }
    }


}

