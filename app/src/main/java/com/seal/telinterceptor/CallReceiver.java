package com.seal.telinterceptor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Created by huan on 2015/11/12.
 */
public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "tag";
    private TelephonyManager mTelephonyManager;
    private static PhoneInfoLayout mPhoneInfoLayout;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(mPhoneInfoLayout == null) {
            mPhoneInfoLayout = new PhoneInfoLayout(context);
        }

        mTelephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        switch (mTelephonyManager.getCallState()) {
            case TelephonyManager.CALL_STATE_RINGING:
                PhoneChecker.getInstance().isJunk(context, number, new PhoneChecker.JunkCheckerListener() {
                    @Override
                    public void onJunk(final PhoneInfo phoneInfo) {
                        if(phoneInfo.count > 30) {
                            endCall();
                        }

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showFloatWindow(context, phoneInfo);
                            }
                        });
                    }
                });

                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                PhoneChecker.getInstance().isJunk(context, number, new PhoneChecker.JunkCheckerListener() {
                    @Override
                    public void onJunk(final PhoneInfo phoneInfo) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showFloatWindow(context, phoneInfo);
                            }
                        });
                    }
                });
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                hideFloatWindow(context);
                break;
        }
    }

    private void showFloatWindow(final Context context, final PhoneInfo phoneInfo) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        layoutParams.x = 0;
        layoutParams.y = 600;

        hideFloatWindow(context);

        windowManager.addView(mPhoneInfoLayout, layoutParams);
        mPhoneInfoLayout.setTag("1");
        mPhoneInfoLayout.setData(phoneInfo);
    }

    private void hideFloatWindow(final Context context) {
        if("1".equals(mPhoneInfoLayout.getTag())) {
            mPhoneInfoLayout.setTag("0");
            final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(mPhoneInfoLayout);
        }
    }

    private void endCall() {
        Class<TelephonyManager> c = TelephonyManager.class;
        try {
            Method getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null);
            getITelephonyMethod.setAccessible(true);
            ITelephony iTelephony = null;
            Log.e(TAG, "End call.");
            iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null);
            iTelephony.endCall();
        } catch (Exception e) {
            Log.e(TAG, "Fail to answer ring call.", e);
        }
    }
}
