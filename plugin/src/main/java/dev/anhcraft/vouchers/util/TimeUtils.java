package dev.anhcraft.vouchers.util;

public class TimeUtils {
    public static String format(long timeSec) {
        long hours = timeSec / 3600;
        long minutes = (timeSec % 3600) / 60;
        long seconds = timeSec % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
