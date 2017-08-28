package xh.xshare.VersionUpdate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import xh.xshare.BuildConfig;
import xh.xshare.R;


/**
 * Created by 邱漢璋 on 2016/12/29.
 */
class CheckUpdateTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "CheckUpdateTask";

    private ProgressDialog dialog;
    private Context mContext;
    private int mType;
    private boolean mShowProgressDialog;
    private static String url = VersionUpdateConstants.UPDATE_URL_UAT;

    CheckUpdateTask(Context context, int type, boolean showProgressDialog) {
        this.mContext = context;
        this.mType = type;
        this.mShowProgressDialog = showProgressDialog;
//        if (BuildConfig.ZDT_FLAVOR.equals(ZUtil.UAT)) {
//            url = VersionUpdateConstants.UPDATE_URL_UAT;
//            Log.d("CheckUpdateTask", "version: uat, url: "+url);
//        } else if (BuildConfig.ZDT_FLAVOR.equals(ZUtil.PRODUCTION)){
//            url = VersionUpdateConstants.UPDATE_URL_PRO;
//            Log.d("CheckUpdateTask", "version: pro, url: "+url);
//        }
    }

    protected void onPreExecute() {
        super.onPreExecute();
        if (mShowProgressDialog) {
            dialog = new ProgressDialog(mContext);
//            dialog.setMessage(mContext.getString(R.string.android_auto_update_dialog_checking));
//            dialog.show();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (!TextUtils.isEmpty(result)) {
            parseJson(result);
        }
    }

    private void parseJson(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            String status = obj.getJSONObject(VersionUpdateConstants.APK_UPDATE_META).getString(VersionUpdateConstants.APK_UPDATE_STATUS);
            if (!status.equals(VersionUpdateConstants.APK_UPDATE_OK))
                return;

            JSONObject data = obj.getJSONObject(VersionUpdateConstants.APK_UPDATE_DATA);
            String version = data.getString(VersionUpdateConstants.APK_UPDATE_VERSION).trim();
            String apkUrl = data.getString(VersionUpdateConstants.APK_DOWNLOAD_URL);

//            apkUrl = apkUrl.replace("https", "http");

            String versionName = AppUtils.getVersionName(mContext);
            Log.d("test", "remote version = "+version);
            Log.d("test", "local version = "+versionName);
            Log.d("test", "apk url = "+apkUrl);

            if (!version.equals(versionName)) {
                if (mType == VersionUpdateConstants.TYPE_NOTIFICATION) {
                    showNotification(mContext, "", apkUrl);
                } else if (mType == VersionUpdateConstants.TYPE_DIALOG) {
                    showDialog(mContext, "", apkUrl, version);
                    //showDialog(mContext, "", apkUrl);
                }
            }
//            else if (mShowProgressDialog) {
//                Toast.makeText(mContext, mContext.getString(R.string.android_auto_update_toast_no_new_update), Toast.LENGTH_SHORT).show();
//            }

        } catch (JSONException e) {
            Log.e(TAG, "parse json error");
        }
    }


    /**
     * Show dialog
     */
    private void showDialog(Context context, String content, String apkUrl, String version) {
        UpdateDialog.show(context, content, apkUrl, version);
    }

    /**
     * Show Notification
     */
    private void showNotification(Context context, String content, String apkUrl) {
        Intent myIntent = new Intent(context, DownloadService.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.putExtra(VersionUpdateConstants.APK_DOWNLOAD_URL, apkUrl);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int smallIcon = context.getApplicationInfo().icon;
        Notification notify = new NotificationCompat.Builder(context)
                .setTicker(context.getString(R.string.android_auto_update_notify_ticker))
                .setContentTitle(context.getString(R.string.android_auto_update_notify_content))
                .setContentText(content)
                .setSmallIcon(smallIcon)
                .setContentIntent(pendingIntent).build();

        notify.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notify);
    }

    @Override
    protected String doInBackground(Void... args) {
        return HttpUtils.get(url);
    }
}
