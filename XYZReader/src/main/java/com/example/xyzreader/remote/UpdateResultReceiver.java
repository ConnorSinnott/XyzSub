package com.example.xyzreader.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Spectre on 3/7/2016.
 */
public class UpdateResultReceiver extends BroadcastReceiver {

    public static final String EXTRA_RESULT = "Result";

    public interface OnUpdateFailed {
        void onUpdateFailed();
    }

    public interface OnUpdateSucceeded {
        void onUpdateSucceeded();
    }

    private static OnUpdateFailed mOnUpdateFailed;
    private static OnUpdateSucceeded mOnUpdateSucceeded;

    public static void setOnUpdateFailed(OnUpdateFailed onUpdateFailed) {
        mOnUpdateFailed = onUpdateFailed;
    }

    public static void setOnUpdateSucceeded(OnUpdateSucceeded onUpdateSucceeded) {
        mOnUpdateSucceeded = onUpdateSucceeded;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean result = intent.getExtras().getBoolean(EXTRA_RESULT);
        if (result) {
            if (mOnUpdateSucceeded != null) {
                mOnUpdateSucceeded.onUpdateSucceeded();
            }
        } else {
            if (mOnUpdateFailed != null) {
                mOnUpdateFailed.onUpdateFailed();
            }
        }
    }


}
