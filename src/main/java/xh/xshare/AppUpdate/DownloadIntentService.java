package xh.xshare.AppUpdate;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xh.xshare.BuildConfig;
import xh.xshare.R;
import xh.xshare.ShareUtil;
import xh.xshare.net.NetworkClient;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DOWNLOAD_FILE = "xh.xshare.AppUpdate.action.download_file";
    private static final String ACTION_BAZ = "xh.xshare.AppUpdate.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "xh.xshare.AppUpdate.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "xh.xshare.AppUpdate.extra.PARAM2";

    private NetworkClient networkClient;
    private static final int NOTIFICATION_ID = 0;
    private static final int BUFFER_SIZE = 10 * 1024; // 8k ~ 32K

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    public DownloadIntentService() {
        super("DownloadIntentService");
        networkClient = new NetworkClient();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionDonwloadFile(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DownloadIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_FILE);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DownloadIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        String appName = getString(getApplicationInfo().labelRes);
        int icon = getApplicationInfo().icon;

        mBuilder.setContentTitle(appName).setSmallIcon(icon);
//        String urlStr = intent.getStringExtra(VersionUpdateConstants.APK_DOWNLOAD_URL);


        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD_FILE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionDownloadFile(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    private void handleActionDownloadFile(String param1, String param2) {
        networkClient.download()
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                InputStream is = response.body().byteStream();
                                byte[] dataBuffer = new byte[BUFFER_SIZE];
                                long bytesum = 0;
                                int byteread = 0;
                                int oldProgress = 0;
                                FileOutputStream out = null;
                                try {
                                    File dir = ShareUtil.getCacheDirectory(DownloadIntentService.this);
//                                    String apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
                                    File apkFile = new File(dir, "test");
                                    out = new FileOutputStream(apkFile);
                                    while ((byteread = is.read(dataBuffer)) != -1) {
                                        Log.d("test", "onResponse read: "+byteread);
                                        bytesum += byteread;
                                        out.write(dataBuffer, 0, byteread);

                                        int progress = (int) (bytesum * 100L / response.body().contentLength());
                                        // 如果进度与之前进度相等，则不更新，如果更新太频繁，否则会造成界面卡顿
                                        if (progress != oldProgress) {
                                            updateProgress(progress);
                                        }
                                        oldProgress = progress;
                                    }
                                    // 下载完成
                                    Log.d("test", "complete download");
                                    mNotifyManager.cancel(NOTIFICATION_ID);
                                    installAPk(apkFile);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (out != null) {
                                        try {
                                            out.close();
                                        } catch (IOException ignored) {

                                        }
                                    }
                                }
                                return null;
                            }
                        }.execute();

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("test", "onFailure: "+t.getLocalizedMessage());
                    }
                });
    }

    private void updateProgress(int progress) {
        //"正在下载:" + progress + "%"
        mBuilder.setContentText(this.getString(R.string.android_auto_update_download_progress, progress))
                .setProgress(100, progress, false);
        //setContentInent如果不设置在4.0+上没有问题，在4.0以下会报异常
        PendingIntent pendingintent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingintent);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void installAPk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //如果没有设置SDCard写权限，或者没有sdcard,apk文件保存在内存中，需要授予权限才能安装
        try {
            String[] command = {"chmod", "777", apkFile.toString()};
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
        } catch (IOException ignored) {
        }

        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = AppFileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    apkFile);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.d("test", "install above N");
        } else {
            uri = Uri.fromFile(apkFile);
            Log.d("test", "install below N");
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive");

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        System.exit(0);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
