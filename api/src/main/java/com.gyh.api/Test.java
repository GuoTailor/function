package com.gyh.api;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gyh on 2019/9/21.
 */
public class Test {

    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
        Set<Integer> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
        set.add(1);
        set.add(2);
        set.add(3);
        set.add(4);

        ThreadManager.getInstance().execute(() -> {
            for (int i = 0; i < 3; i++) {
                int n = new Random().nextInt();
                System.out.println("添加" + n);
                set.add(n);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Iterator<Integer> it = set.iterator();
        while (it.hasNext()) {
            System.out.println("移除" + it.next());
            it.remove();
            Thread.sleep(1000);
        }
        set.forEach(System.out::println);
        ThreadManager.getInstance().shutdown();
    }
}
