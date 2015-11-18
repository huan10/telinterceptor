package com.seal.telinterceptor;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by huan on 2015/11/14.
 */
public class PhoneInfoLayout extends FrameLayout {
    private TextView mTvName;
    private TextView mTvLocation;
    private TextView mTvType;
    private TextView mTvCount;

    public PhoneInfoLayout(Context context) {
        super(context);
        init(context);
    }

    public PhoneInfoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhoneInfoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.layout_phone_info, this);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mTvLocation = (TextView) findViewById(R.id.tv_location);
        mTvType = (TextView) findViewById(R.id.tv_type);
        mTvCount = (TextView) findViewById(R.id.tv_count);
    }

    public void setData(PhoneInfo phoneInfo) {
        if(phoneInfo != null) {
            if(!TextUtils.isEmpty(phoneInfo.operator)) {
                mTvType.setVisibility(VISIBLE);
                mTvType.setText(phoneInfo.operator);
            } else {
                mTvType.setVisibility(GONE);
            }

            if(!TextUtils.isEmpty(phoneInfo.location)) {
                mTvLocation.setVisibility(VISIBLE);
                mTvLocation.setText(phoneInfo.location);
            } else {
                mTvLocation.setVisibility(GONE);
            }

            if(!TextUtils.isEmpty(phoneInfo.name)) {
                mTvName.setVisibility(VISIBLE);
                mTvName.setText(phoneInfo.name);
            } else {
                mTvName.setVisibility(GONE);
            }

            if(phoneInfo.count > 0) {
                mTvCount.setVisibility(VISIBLE);
                mTvCount.setText("标记次数：" + String.valueOf(phoneInfo.count));
            } else {
                mTvCount.setVisibility(GONE);
            }
        }
    }
}
