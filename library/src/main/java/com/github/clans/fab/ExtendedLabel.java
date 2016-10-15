package com.github.clans.fab;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * TextView for extended version of FloatingActionMenu
 *
 * @author Josef Hruška (josef@stepuplabs.io)
 */

public class ExtendedLabel extends TextView {

    private Drawable mBackgroundDrawable;
    private int mRawWidth;
    private int mRawHeight;
    private ExtendedFloatingActionButton mFab;
    private boolean mUsingStyle;
    GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            onActionDown();
            if (mFab != null) {
                mFab.onActionDown();
            }
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onActionUp();
            if (mFab != null) {
                mFab.onActionUp();
            }
            return super.onSingleTapUp(e);
        }
    });
    private boolean mHandleVisibilityChanges = true;

    public ExtendedLabel(Context context) {
        super(context);
    }

    public ExtendedLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedLabel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight());
    }

    void show() {
        setVisibility(VISIBLE);
    }

    void hide() {
        setVisibility(INVISIBLE);
    }

    private int calculateMeasuredWidth() {
        if (mRawWidth == 0) {
            mRawWidth = getMeasuredWidth();
        }
        return getMeasuredWidth();
    }

    private int calculateMeasuredHeight() {
        if (mRawHeight == 0) {
            mRawHeight = getMeasuredHeight();
        }
        return getMeasuredHeight();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionDown() {
        if (mUsingStyle) {
            mBackgroundDrawable = getBackground();
        }

        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[]{android.R.attr.state_pressed});
        } else if (Util.hasLollipop() && mBackgroundDrawable instanceof RippleDrawable) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed});
            ripple.setHotspot(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            ripple.setVisible(true, true);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionUp() {
        if (mUsingStyle) {
            mBackgroundDrawable = getBackground();
        }

        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[]{});
        } else if (Util.hasLollipop() && mBackgroundDrawable instanceof RippleDrawable) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[]{});
            ripple.setHotspot(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            ripple.setVisible(true, true);
        }
    }

    void setFab(ExtendedFloatingActionButton fab) {
        mFab = fab;
    }

    void setUsingStyle(boolean usingStyle) {
        mUsingStyle = usingStyle;
    }

    boolean isHandleVisibilityChanges() {
        return mHandleVisibilityChanges;
    }

    void setHandleVisibilityChanges(boolean handle) {
        mHandleVisibilityChanges = handle;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mFab != null) {  // Ripple is not handled.
            return false;
        }

        if (mFab == null || mFab.getOnClickListener() == null || !mFab.isEnabled()) {
            return super.onTouchEvent(event);
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                onActionUp();
                mFab.onActionUp();
                break;

            case MotionEvent.ACTION_CANCEL:
                onActionUp();
                mFab.onActionUp();
                break;
        }
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}