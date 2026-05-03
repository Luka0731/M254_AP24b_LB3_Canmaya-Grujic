package ch.concertticketwatcherengine.core.util;

import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    public static final long HOURS_IN_DAY = 24;
    public static final long DAYS_IN_YEAR = 365;

    public static void sleepHours(long hours) {
        try {
            TimeUnit.HOURS.sleep(hours);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.error("{ThreadUtil} Sleep interrupted: " + e.getMessage());
        }
    }

    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.error("{ThreadUtil} Sleep interrupted: " + e.getMessage());
        }
    }
}