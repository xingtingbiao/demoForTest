package com.xingtb.Thread;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SimpleCache {

    private final Object lock = new Object();
    private volatile Map<String, Object> map = new HashMap<>();

    private static SimpleCache simpleCache = null;

    public Object get(String key) {
        if (null == map.get(key)) {
            System.out.println(key + " synchronized " + new Date());
            synchronized (lock) {
                map.computeIfAbsent(key, k -> this.retrieve(key));
            }
        }
        return map.get(key);
    }

    private Object retrieve(String key) {
        //this method is given, and should be called by the get() method when a key is absent
        return "value";
    }

    public static SimpleCache getSimpleCache() {
        if (null == simpleCache) {
            synchronized (SimpleCache.class) {
                if (null == simpleCache) {
                    simpleCache = new SimpleCache();
                }
            }
        }
        return simpleCache;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> System.out.println(getSimpleCache().get("a"))).start();
            new Thread(() -> System.out.println(getSimpleCache().get("b"))).start();
        }
    }
}
