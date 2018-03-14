package net.lzzy.networkdisk.utils;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.lzzy.networkdisk.constants.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

public class AppUtils extends Application {
    private List<Activity> activities = new ArrayList<>();
    private static AppUtils context;

    public AppUtils() {
    }

    public static RefWatcher getRefWatcher(Context context) {
        AppUtils application = (AppUtils) context.getApplicationContext();
        return application.refWatcher;
    }


    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        File file = new File(Constants.FILE_DOWNLOAD_PATH);
        if (!file.exists())
            file.mkdirs();
        refWatcher = LeakCanary.install(this);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
//log打印级别，决定了log显示的详细程度
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
//log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);
//全局的读取超时时间
        builder.readTimeout(10000, TimeUnit.MILLISECONDS);
//全局的写入超时时间
        builder.writeTimeout(10000, TimeUnit.MILLISECONDS);
//全局的连接超时时间
        builder.connectTimeout(10000, TimeUnit.MILLISECONDS);
//使用sp保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));
//方法一：信任所有证书,不安全有风险
        HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);

        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
        HttpHeaders headers = new HttpHeaders();
        headers.put("header", "");    //header不支持中文，不允许有特殊字符
        HttpParams params = new HttpParams();
        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
        params.put("commonParamsKey2", "这里支持中文参数");
//-------------------------------------------------------------------------------------//

        OkGo.getInstance().init(this)                       //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(1);                         //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
        //.addCommonHeaders(headers);                     //全局公共头
//                .addCommonParams(params);                       //全局公共参数



    }

    public static Context getContext() {
        return context;
    }

    public void addActivity(Activity activity) {
        Log.i("sfsdfjsj45sf5fsd", "addActivity: " + activity);
        activities.add(activity);
    }

    public void allFinishActivity() {
        for (Activity a : activities)
            if (a != null && !a.isFinishing())
                a.finish();
        System.exit(0);
    }

    public void removeActivity(Activity activity) {
        if (activity != null)
            activities.remove(activity);
        Log.i("sfsdfjsj45sf5fsd", "removeActivity: " + activity);
    }

    public static String getVersionName() {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getVersionCode() {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 这是使用adb shell命令来获取mac地址的方式
     */
    public static String getMac() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    macSerial = macSerial.replace(":", "");//去:
                    break;
                }
            }
        } catch (IOException ex) {

            ex.printStackTrace();
        }
        return macSerial;
    }

    /**
     * 格式化单位
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "B";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "K";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "M";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "G";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);

        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "T";
    }

    public static String getFileSavePath() {
        String path = Constants.FILE_DOWNLOAD_PATH;
        SPUtils sp = new SPUtils(Constants.SP_APP_STATE);
        String getPath = sp.getString(Constants.SP_FILE_SAVE_PATH, null);
        if (getPath == null) {
            sp.putString(Constants.SP_FILE_SAVE_PATH, path);
        } else if (new File(getPath).exists()) {
            path = getPath;
        }
        return path;
    }

    public static String getTmpPath() {
        //String path = Constants.FILE_DOWNLOAD_PATH + "/tmp";
        String path = Constants.FILE_DOWNLOAD_PATH;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }


    public static long toLong(String s) {
        long res = 0;

        if (s.contains("T") || s.contains("TB")) {
            double num = Double.parseDouble(s.substring(0, s.indexOf("T")));
            res = (long) (num * 1024 * 1024 * 1024 * 1024);
            return res;
        }
        if (s.contains("G") || s.contains("GB")) {
            double num = Double.parseDouble(s.substring(0, s.indexOf("G")));
            res = (long) (num * 1024 * 1024 * 1024);
            return res;
        }
        if (s.contains("M") || s.contains("MB")) {
            double num = Double.parseDouble(s.substring(0, s.indexOf("M")));
            res = (long) (num * 1024 * 1024);
            return res;
        }
        if (s.contains("K") || s.contains("KB")) {
            double num = Double.parseDouble(s.substring(0, s.indexOf("K")));
            res = (long) (num * 1024);
            return res;
        }

        if (s.contains("B")) {
            res = (long) Double.parseDouble(s.substring(0, s.indexOf("B")));
            return res;
        }
        return res;
    }

    /**
     * 获得SD卡总大小 * * @return
     */
    public static String getSDTotalSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return Formatter.formatFileSize(getContext(), blockSize * totalBlocks);
    }

    /**
     * 获得sd卡剩余容量，即可用大小 * * @return
     */
    public static String getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        return Formatter.formatFileSize(getContext(), blockSize * availableBlocks);
    }

    /**
     * 获得机身内存总大小 * * @return
     */
    private String getRomTotalSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return Formatter.formatFileSize(getContext(), blockSize * totalBlocks);
    }

    /**
     * 获得机身可用内存 * * @return
     */
    private String getRomAvailableSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return Formatter.formatFileSize(getContext(), blockSize * availableBlocks);
    }

    public static File getThisApkFile(Activity activity) {
        List<PackageInfo> packageInfos = activity.getPackageManager().getInstalledPackages(0);
        for (PackageInfo p : packageInfos) {
            if (p.packageName.equals("net.lzzy.networkdisk")) {
                Log.i("dfsdf", "onFinish: "+p.applicationInfo.publicSourceDir);
                Log.i("dfsdf", "onFinish: "+p.applicationInfo.sourceDir);
                return new File(p.applicationInfo.publicSourceDir);
            }
        }
        return null;
    }

}
