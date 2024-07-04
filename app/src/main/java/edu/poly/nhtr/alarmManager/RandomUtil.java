package edu.poly.nhtr.alarmManager;

import java.util.concurrent.atomic.AtomicInteger;

public class RandomUtil {
    private static final AtomicInteger seed = new AtomicInteger();

    public static int getRandomInt() {
        return seed.getAndIncrement() + (int) System.currentTimeMillis();
    }
}


