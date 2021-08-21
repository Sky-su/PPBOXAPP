package com.ives.ppboxapp.utils;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {
    private static Toast mToast;
    private static Handler mHandler = new Handler();
    private static Runnable r = new Runnable() {
        public void run() {
            mToast.cancel();
        }
    };

    public static void showToast(Context mContext, String text, int duration) {

        mHandler.removeCallbacks(r);
        if (mToast != null)
            mToast.setText(text);
        else
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        LinearLayout layout = (LinearLayout) mToast.getView();
        TextView tv = (TextView) layout.getChildAt(0);
        tv.setTextSize(18);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setText(text);
        mHandler.postDelayed(r, duration);

        mToast.show();
    }

    public static void showToast(Context mContext, int resId, int duration) {
        showToast(mContext, mContext.getResources().getString(resId), duration);
    }


}
