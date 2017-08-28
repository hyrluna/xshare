package xh.xshare.VersionUpdate;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

public class UpdateChecker {


    public static void checkForDialog(Context context) {
        if (context != null) {
            AsyncTask<Void, Void, String> task = new CheckUpdateTask(context, VersionUpdateConstants.TYPE_DIALOG, true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                task.execute();
            //new CheckUpdateTask(context, VersionUpdateConstants.TYPE_DIALOG, true).execute();
        } else {
            Log.e(VersionUpdateConstants.TAG, "The arg context is null");
        }
    }


    public static void checkForNotification(Context context) {
        if (context != null) {
            new CheckUpdateTask(context, VersionUpdateConstants.TYPE_NOTIFICATION, false).execute();
        } else {
            Log.e(VersionUpdateConstants.TAG, "The arg context is null");
        }

    }


}
