package xh.xshare;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.annotation.ColorRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.functions.Func1;
import xh.xshare.AppUpdate.DownloadIntentService;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by G1494458 on 2017/01/11.
 */

public class ShareUtil {

    private static final String TAG = "ShareUtil";

    public static final String TIME_FORMAT_STANDARD = "yyyy-MM-dd HH:mm:ss";
    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static final int TYPE_PAGE_LOGIN = 0;
    public static final int TYPE_PAGE_ANNUAL_VACATION = 1;
    public static final int TYPE_PAGE_ATTENDANCE_CARD = 2;
    public static final int TYPE_PAGE_BONUS = 3;
    public static final int TYPE_PAGE_LEAVE = 4;
    public static final int TYPE_PAGE_PAYCHECK = 5;
    public static final int TYPE_PAGE_PERSONAL_TAX = 6;
    public static final int TYPE_PAGE_SALARY = 7;
    public static final int TYPE_PAGE_FIRST_LOGIN = 8;
    public static final int TYPE_PAGE_FORGET_PWD = 9;
    public static final int TYPE_PAGE_MODIFY_PWD = 10;
    public static final int TYPE_PAGE_HOME = 10;
    public static final int TYPE_PAGE_OVERTIME = 11;
    public static final int TYPE_PAGE_OVERTIME_DETAIL = 12;
    public static final String UAT = "uat";
    public static final String PRODUCTION = "pro";

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static int getColor(Context context, @ColorRes int color) {
        return context.getResources().getColor(color);
    }

    public static boolean isNetworkUsable(Context context) {
        ConnectivityManager conmgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conmgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = conmgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();
        return isWifiConn | isMobileConn;
    }

    public static void handleError(Context context, Throwable e, int page, String... param) {
        Log.e("test", "handleError: "+e.getMessage());
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            int errorCode = httpException.response().code();

            switch (errorCode) {
                case 404:
                    showToast(context, "错误代码404: " + context.getString(R.string.http_error_404));
                    break;
                case 400:
                    if (page == TYPE_PAGE_LOGIN
                            || page == TYPE_PAGE_FIRST_LOGIN
                            || page == TYPE_PAGE_FORGET_PWD
                            || page == TYPE_PAGE_MODIFY_PWD) {
                        String addtionError = "";
                        if (param.length > 0) {
                            addtionError = ", "+param[0];
                        }
                        showToast(context, getLoginPageError(httpException)+addtionError);
                    } else {
                        showToast(context, "错误代码400: " + context.getString(R.string.http_error_400));
                    }
                    break;
                case 401:
                    showToast(context, "错误代码401: " + context.getString(R.string.http_error_401));
                    break;
                case 407:
                    showToast(context, "错误代码407: " + context.getString(R.string.http_error_407));
                    break;
                case 500:
                    showToast(context, "错误代码500: " + context.getString(R.string.http_error_500));
                    break;
            }
        } else if (e instanceof SocketTimeoutException) {
            showToast(context, context.getString(R.string.http_error_time_out));
        } else if (e instanceof IOException) {
            showToast(context, "IO Exception: "+e.getMessage());
        } else {
            showToast(context, e.getMessage());
        }
    }

    private static String getLoginPageError(HttpException httpException) {
        String errorMsg = "";
        try {
            String body = httpException.response().errorBody().string();
            JsonReader reader = new JsonReader(new StringReader(body));
            reader.beginObject();
            String name = reader.nextName();
            if (name.equals("Message")) {
                errorMsg = reader.nextString();
            }
            reader.endObject();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return errorMsg;
    }

    public static String formatTime(long time, String f) {
        SimpleDateFormat format = new SimpleDateFormat(f, Locale.US);
        return format.format(time);
    }

    public static String formatTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT_STANDARD, Locale.US);
        return format.format(time);
    }

    public static String formatStringRes(Context context, int resId, Object...args) {
        Resources res = context.getResources();
        return String.format(res.getString(resId), args);
    }

    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }

    }

    public static String checkNull(String s) {
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    public static String formatStr(String num) {
        if (num == null)
            return "";
        String result = "";
        if (num.endsWith(".0") || num.endsWith("0.00")) {
            result = num.substring(0, num.indexOf("."));
        } else {
            result = num;
        }
        return result;
    }

    public static boolean isAlphaOrNumber(String name) {
        return name.matches("[a-zA-Z0-9]+");
    }

    /**
     * targetSdkVersion >= 23 (6.0)以上权限申请
     * @param activity
     * @param permissions
     * @param requestCode
     * @param callback
     */
    public static void requestMarshmallowPermission(final Activity activity, String[] permissions, final int requestCode, PermissionCallback callback) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //获取权限失败
                    callback.grantPermission(false, permission);
                } else {
                    ActivityCompat.requestPermissions(activity,
                            permissions,
                            requestCode);
                    //弹出权限申请对话框
                }
            } else {
                //获取权限成功
                callback.grantPermission(true, permission);
            }
        }
    }

    public static File getCacheDirectory(Context context) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            Log.w(TAG, "Can't define system cache directory! The app should be re-installed.");
        }
        return appCacheDir;
    }


    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w(TAG, "Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Log.i(TAG, "Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    public interface PermissionCallback {
        void grantPermission(boolean grant, String permission);
    }

}
