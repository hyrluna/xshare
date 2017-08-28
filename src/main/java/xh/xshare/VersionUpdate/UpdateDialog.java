package xh.xshare.VersionUpdate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;

import xh.xshare.R;

class UpdateDialog {
    private static AlertDialog dialog;
    private static AlertDialog.Builder builder;

    static void show(final Context context, String content, final String downloadUrl, String version) {
        if (isContextValid(context)) {
            builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.android_auto_update_dialog_title);
            builder.setMessage("( " + version + " )")
                    .setPositiveButton(R.string.android_auto_update_dialog_btn_download, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int id) {
                            dialogInterface.dismiss();
                            setDownloadingDialog(context);
                            goToDownload(context, downloadUrl);
                        }
                    });
//                    .setNegativeButton(R.string.android_auto_update_dialog_btn_cancel, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                        }
//                    })

            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);    //点击对话框外面,对话框不消失
            dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                    }
                    return true;
                }
            });
            dialog.show();
        }
    }

    private static boolean isContextValid(Context context) {
        return context instanceof Activity && !((Activity) context).isFinishing();
    }


    private static void goToDownload(Context context, String downloadUrl) {
        Intent intent = new Intent(context.getApplicationContext(), DownloadService.class);
        intent.putExtra(VersionUpdateConstants.APK_DOWNLOAD_URL, downloadUrl);
        context.startService(intent);
    }

    private static void setDownloadingDialog(Context context) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.android_auto_update_dialog_title);
        builder.setMessage("下載中");
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);    //点击对话框外面,对话框不消失
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                }
                return true;
            }
        });
        dialog.show();
    }
}
