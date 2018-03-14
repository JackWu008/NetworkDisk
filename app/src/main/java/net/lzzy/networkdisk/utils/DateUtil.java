package net.lzzy.networkdisk.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

/**
 * 时间相关工具类
 */
public class DateUtil {
    private DateUtil() {

    }

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static String format(long time) {
        return bartDateFormat.format(time);
    }

    @SuppressLint("SimpleDateFormat")
    public static long parse(String dateString) {
        try {
            return bartDateFormat.parse(dateString).getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


}
