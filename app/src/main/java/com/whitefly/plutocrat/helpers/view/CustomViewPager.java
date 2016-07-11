package com.whitefly.plutocrat.helpers.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.whitefly.plutocrat.helpers.AppPreference;

/**
 * Created by satjapotiamopas on 5/26/16 AD.
 */
public class CustomViewPager extends ViewPager {
    private boolean mIsPagingEnabled = true;

    public CustomViewPager(Context context) {
        super(context);
        ViewConfiguration.get(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration.get(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.mIsPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.mIsPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b) {
        this.mIsPagingEnabled = b;
    }
}
