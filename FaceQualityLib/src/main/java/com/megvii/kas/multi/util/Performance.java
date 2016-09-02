package com.megvii.kas.multi.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tanjun on 16/8/26.
 */
public class Performance {
    private static Performance _instance;

    Map<String, Time> map;

    protected Performance() {
        map = new HashMap<String, Time>();
    }

    public synchronized void update(String name, long ms) {
        if(map.containsKey(name)) {
            Time time = map.get(name);
            time.update(ms);
        } else {
            Time time = new Time(ms);
            map.put(name, time);
        }
    }

    public synchronized long interval(String name) {
        if (!map.containsKey(name)) {
            return -1;
        }
        return map.get(name).interval;
    }

    public static Performance instance() {
        if (null == _instance) {
            _instance = new Performance();
        }
        return _instance;
    }

    static public class Time {
        public Time(long record) {
            this.record = record;
            this.interval = -1;
        }

        public void update(long time) {
            interval = time - record;
            record = time;
        }

        long record;
        long interval;
    }
}
