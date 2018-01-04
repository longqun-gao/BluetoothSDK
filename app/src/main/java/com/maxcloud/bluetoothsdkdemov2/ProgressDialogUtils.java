package com.maxcloud.bluetoothsdkdemov2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ProgressDialogUtils {
    private static ProgressDialog mProgressDialog;

    public static void showProgressDialog(Context context, CharSequence message) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(context, "", message);
            mProgressDialog.setCancelable(true);
        } else {
            mProgressDialog.show();
        }
    }

    public static void showProgressDialogNoCancel(Context context, CharSequence message) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(context, "", message);
            mProgressDialog.setCancelable(false);
        } else {
            mProgressDialog.show();
        }
    }

    public static void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public static void showProgressDialogWithButton(final Context context, CharSequence message, CharSequence msg) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(context, "", message);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, msg, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProgressDialog.dismiss();
                }
            });
        } else {
            mProgressDialog.show();
        }
    }
}
