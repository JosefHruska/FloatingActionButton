package com.github.clans.fab;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import static android.R.attr.animation;
import static com.github.clans.fab.FloatingActionButton.SIZE_NORMAL;

/**
 * Menu button class for extended version of FloatingActionMenu
 *
 * @author Josef Hruška (josef@stepuplabs.io)
 */

public class ExtendedFloatingActionButton extends ImageButton {

    private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private static final long PAUSE_GROWING_TIME = 200;
    private static final double BAR_SPIN_CYCLE_TIME = 500;
    private static final int BAR_MAX_LENGTH = 270;
    int mFabSize;
    boolean mShowShadow;
    int mShadowColor;
    int mShadowRadius = Util.dpToPx(getContext(), 8f);
    int mShadowXOffset = Util.dpToPx(getContext(), 1f);
    int mShadowYOffset = Util.dpToPx(getContext(), 3f);
    private int mColorNormal;
    private int mColorPressed;
    private int mColorDisabled;
    private int mColorRipple;
    private int mColorExtendedNormal;
    private int mColorExtendedPressed;
    private int mColorExtendedDisabled;
    private int mColorExtendedRipple;
    private Drawable mIcon;
    private int mIconSize = Util.dpToPx(getContext(), 24f);
    private Animation mShowAnimation;
    private Animation mHideAnimation;
    private Animation mReplaceExtendedAnimation;
    private Animation mHideExtendedAnimation;
    private String mLabelText;
    private OnClickListener mClickListener;
    private Drawable mBackgroundDrawable;
    GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            ExtendedLabel label = (ExtendedLabel) getTag(R.id.fab_label);
            if (label != null) {
                label.onActionDown();
            }
            onActionDown();
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            ExtendedLabel label = (ExtendedLabel) getTag(R.id.fab_label);
            if (label != null) {
                label.onActionUp();
            }
            onActionUp();
            return super.onSingleTapUp(e);
        }
    });
    private boolean mUsingElevation;
    private boolean mUsingElevationCompat;
    private Handler uiHandler = new Handler();

    public ExtendedFloatingActionButton(Context context) {
        this(context, null);
    }

    public ExtendedFloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExtendedFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Context mContext = context;
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExtendedFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, 0);

        mColorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal, 0xFFDA4336);
        mColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed, 0xFFE75043);
        mColorDisabled = attr.getColor(R.styleable.FloatingActionButton_fab_colorDisabled, 0xFFAAAAAA);
        mColorExtendedDisabled = attr.getColor(R.styleable.FloatingActionButton_fab_colorExtendedRipple, Color.parseColor("#a2000000"));
        mColorExtendedNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorExtendedNormal, Color.parseColor("#ffffff"));
        mColorExtendedPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorExtendedPressed, Color.parseColor("#50e0e0e0"));
        mColorExtendedRipple = attr.getColor(R.styleable.FloatingActionButton_fab_colorExtendedDisabled, Color.parseColor("#50eeeeee"));
        mColorRipple = attr.getColor(R.styleable.FloatingActionButton_fab_colorRipple, 0x99FFFFFF);
        mShowShadow = attr.getBoolean(R.styleable.FloatingActionButton_fab_showShadow, true);
        mShadowColor = attr.getColor(R.styleable.FloatingActionButton_fab_shadowColor, 0x66000000);
        mShadowRadius = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowRadius, mShadowRadius);
        mShadowXOffset = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowXOffset, mShadowXOffset);
        mShadowYOffset = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowYOffset, mShadowYOffset);
        mFabSize = attr.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL);
        mLabelText = attr.getString(R.styleable.FloatingActionButton_fab_label);
        int mLabelColorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_labelColorNormal, Color.BLACK);
        int mLabelColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_labelColorPressed, Color.BLACK);
        int mLabelColorRipple = attr.getColor(R.styleable.FloatingActionButton_fab_labelColorRipple, Color.BLACK);

        if (attr.hasValue(R.styleable.FloatingActionButton_fab_elevationCompat)) {
            float elevation = attr.getDimensionPixelOffset(R.styleable.FloatingActionButton_fab_elevationCompat, 0);
            if (isInEditMode()) {
                setElevation(elevation);
            } else {
                setElevationCompat(elevation);
            }
        }

        initShowAnimation(attr);
        initHideAnimation(attr);
        initReplaceExtendedAnimation();
        initHideExtendedAnimation();
        attr.recycle();

        setClickable(true);
    }

    private void initShowAnimation(TypedArray attr) {
        int resourceId = attr.getResourceId(R.styleable.FloatingActionButton_fab_showAnimation, R.anim.fab_scale_up);
        mShowAnimation = AnimationUtils.loadAnimation(getContext(), resourceId);
        mShowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ExtendedFloatingActionMenu menu = getActionMenu();
                CharSequence menuText = menu.mMenuText.getText().toString();
                if (!menuText.equals(menu.extendedButtonTextExpanded)) {
                   menu.mMenuText.setText(menu.extendedButtonTextExpanded);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void initHideAnimation(TypedArray attr) {
        int resourceId = attr.getResourceId(R.styleable.FloatingActionButton_fab_hideAnimation, R.anim.fab_scale_down);
        mHideAnimation = AnimationUtils.loadAnimation(getContext(), resourceId);
    }

    private void initHideExtendedAnimation() {
        mHideExtendedAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fab_extended_hide);
        mHideExtendedAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ExtendedFloatingActionMenu menu = getActionMenu();
                CharSequence menuText = menu.mMenuText.getText().toString();
                if (!menuText.equals(menu.extendedButtonTextCollapsed)) {
                    menu.mMenuText.setText(menu.extendedButtonTextCollapsed);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void initReplaceExtendedAnimation() {
        mReplaceExtendedAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fab_extended_replace);
    }

    protected int getCircleSize() {
        return getResources().getDimensionPixelSize(mFabSize == SIZE_NORMAL
                ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
    }

    private int getExtendedButtonPadding() {
        return getResources().getDimensionPixelSize(R.dimen.extended_button_padding);
    }

    private int getExtendedButtonLandscapeWidth() {
        return getResources().getDimensionPixelSize(R.dimen.extended_button_width_landscape);
    }

    protected int calculateMeasuredWidth() {
        int width;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = Util.getScreenWidth(getContext()) - getExtendedButtonPadding() + calculateShadowWidth();
        } else {
            width = getExtendedButtonLandscapeWidth() + calculateShadowWidth();
        }
        return width;
    }

    public int calculateMeasuredWidthPortrait() {
        return Util.getScreenWidth(getContext()) - getExtendedButtonPadding() + calculateShadowWidth();
    }

    public int calculateMeasuredWidthLandscape() {
        return getExtendedButtonLandscapeWidth() + calculateShadowWidth();
    }

    public int calculateMeasuredWidthAuto() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return Util.getScreenWidth(getContext()) - getExtendedButtonPadding() + calculateShadowWidth();
        } else {
            return getExtendedButtonLandscapeWidth() + calculateShadowWidth();
        }
    }

    protected int calculateMeasuredHeight() {
        return getCircleSize() + calculateShadowHeight();
    }

    int calculateShadowWidth() {
        return hasShadow() ? getShadowX() * 2 : 0;
    }

    int calculateShadowHeight() {
        return hasShadow() ? getShadowY() * 2 : 0;
    }

    private int getShadowX() {
        return mShadowRadius + Math.abs(mShadowXOffset);
    }

    private int getShadowY() {
        return mShadowRadius + Math.abs(mShadowYOffset);
    }

    private float calculateCenterX() {
        return (float) (getMeasuredWidth() / 2);
    }

    private float calculateCenterY() {
        return (float) (getMeasuredHeight() / 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateBackground();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params instanceof ViewGroup.MarginLayoutParams && mUsingElevationCompat) {
            ((ViewGroup.MarginLayoutParams) params).leftMargin += getShadowX();
            ((ViewGroup.MarginLayoutParams) params).topMargin += getShadowY();
            ((ViewGroup.MarginLayoutParams) params).rightMargin += getShadowX();
            ((ViewGroup.MarginLayoutParams) params).bottomMargin += getShadowY();
        }
        super.setLayoutParams(params);
    }

    void updateBackground() {
        LayerDrawable layerDrawable;
        int iconOffsetVertical = 0;
        int iconOffsetLeft = 0;
        int iconOffsetRight = 0;

        if (hasShadow()) {
            layerDrawable = new LayerDrawable(new Drawable[]{
                    new Shadow(),
                    createFillDrawable(),
                    getIconDrawable()
            });
        } else {
            layerDrawable = new LayerDrawable(new Drawable[]{
                    createFillDrawable(),
                    getIconDrawable()
            });
        }

        int iconSize = -1;
        if (getIconDrawable() != null) {
            iconSize = Math.max(getIconDrawable().getIntrinsicWidth(), getIconDrawable().getIntrinsicHeight());
        }

        int extraLeftOffset = 0;

        if (getLabelView() != null) {
            extraLeftOffset = Math.round(((getX() + calculateMeasuredWidth() / 2) - getLabelView().getX()) + getResources().getDimensionPixelSize(R.dimen.extended_button_gap_between_icon_text) / 3); // Align icon on the left side of label text
        }
        iconOffsetVertical = (calculateMeasuredHeight() - (iconSize > 0 ? iconSize : mIconSize)) / 2;
        iconOffsetLeft = (calculateMeasuredWidth() - (iconSize > 0 ? iconSize : mIconSize)) / 2 - extraLeftOffset;
        iconOffsetRight = (calculateMeasuredWidth() - (iconSize > 0 ? iconSize : mIconSize)) / 2 + extraLeftOffset;

        int circleInsetVertical = hasShadow() ? mShadowRadius + Math.abs(mShadowYOffset) : 0;

        layerDrawable.setLayerInset(
                hasShadow() ? 2 : 1,
                iconOffsetLeft,
                iconOffsetVertical + (circleInsetVertical / 4),
                iconOffsetRight,
                iconOffsetVertical +
                        (circleInsetVertical / 4)
        );

        setBackgroundCompat(layerDrawable);
    }

    protected Drawable getIconDrawable() {
        if (mIcon != null) {
            return mIcon;
        } else {
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable createFillDrawable() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{-android.R.attr.state_enabled}, createCircleDrawable(mColorDisabled));
        drawable.addState(new int[]{android.R.attr.state_pressed}, createCircleDrawable(mColorPressed));
        drawable.addState(new int[]{}, createCircleDrawable(mColorNormal));

        if (Util.hasLollipop()) {
            RippleDrawable ripple = new RippleDrawable(new ColorStateList(new int[][]{{}},
                    new int[]{mColorRipple}), drawable, null);

            setOutlineProvider(new ViewOutlineProvider() {
                int extraShadowSpace = 5;

                @Override
                public void getOutline(View view, Outline outline) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        outline.setRoundRect(0, -8, Util.getScreenWidth(getContext()) - (getExtendedButtonPadding() - Util.dpToPx(getContext(), extraShadowSpace)), Util.dpToPx(getContext(), 70f), 25f);

                    } else {
                        outline.setRoundRect(0, -8, getExtendedButtonLandscapeWidth() + Util.dpToPx(getContext(), extraShadowSpace), Util.dpToPx(getContext(), 70f), 25f);
                    }
                }
            });

            setClipToOutline(true);
            mBackgroundDrawable = ripple;
            return ripple;
        }

        mBackgroundDrawable = drawable;
        return drawable;
    }

    private Drawable createCircleDrawable(int color) {
        CircleDrawable shapeDrawable;
        shapeDrawable = new CircleDrawable(new ExtendedButtonShadowShape(getContext()));
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBackgroundCompat(Drawable drawable) {
        if (Util.hasJellyBean()) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    Animation getShowAnimation() {
        return mShowAnimation;
    }

    public void setShowAnimation(Animation showAnimation) {
        mShowAnimation = showAnimation;
    }

    Animation getHideAnimation() {
        return mHideAnimation;
    }

    public void setHideAnimation(Animation hideAnimation) {
        mHideAnimation = hideAnimation;
    }

    void playShowAnimation() {
        mHideAnimation.cancel();
        startAnimation(mShowAnimation);
    }

    ExtendedFloatingActionMenu getActionMenu() {
        return ((ExtendedFloatingActionMenu) getParent());
    }


    void playHideAnimation() {
        mShowAnimation.cancel();
        startAnimation(mHideExtendedAnimation);
    }

    OnClickListener getOnClickListener() {
        return mClickListener;
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        super.setOnClickListener(l);
        mClickListener = l;
        View label = (View) getTag(R.id.fab_label);
        if (label != null) {
            label.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) {
                        mClickListener.onClick(ExtendedFloatingActionButton.this);
                    }
                }
            });
        }
    }

    ExtendedLabel getLabelView() {
        return (ExtendedLabel) getTag(R.id.fab_label);
    }

    void setColors(int colorNormal, int colorPressed, int colorRipple) {
        mColorNormal = colorNormal;
        mColorPressed = colorPressed;
        mColorRipple = colorRipple;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionDown() {
        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed});
        } else if (Util.hasLollipop()) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed});
            ripple.setHotspot(calculateCenterX(), calculateCenterY());
            ripple.setVisible(true, true);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionUp() {
        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[]{android.R.attr.state_enabled});
        } else if (Util.hasLollipop()) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[]{android.R.attr.state_enabled});
            ripple.setHotspot(calculateCenterX(), calculateCenterY());
            ripple.setVisible(true, true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mClickListener != null && isEnabled()) {
            ExtendedLabel label = (ExtendedLabel) getTag(R.id.fab_label);
            if (label == null) return super.onTouchEvent(event);

            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                    if (label != null) {
                        label.onActionUp();
                    }
                    onActionUp();
                    break;

                case MotionEvent.ACTION_CANCEL:
                    if (label != null) {
                        label.onActionUp();
                    }
                    onActionUp();
                    break;
            }
            mGestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    /* ===== API methods ===== */

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (mIcon != drawable) {
            mIcon = drawable;
            updateBackground();
        }
    }

    @Override
    public void setImageResource(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        if (mIcon != drawable) {
            mIcon = drawable;
            updateBackground();
        }
    }

    public int getButtonSize() {
        return mFabSize;
    }

    public void setColorNormalResId(int colorResId) {
        setColorNormal(getResources().getColor(colorResId));
    }

    public int getColorNormal() {
        return mColorNormal;
    }

    public void setColorNormal(int color) {
        if (mColorNormal != color) {
            mColorNormal = color;
            updateBackground();
        }
    }

    public void setColorPressedResId(int colorResId) {
        setColorPressed(getResources().getColor(colorResId));
    }

    public int getColorPressed() {
        return mColorPressed;
    }

    public void setColorPressed(int color) {
        if (color != mColorPressed) {
            mColorPressed = color;
            updateBackground();
        }
    }

    public void setColorRippleResId(int colorResId) {
        setColorRipple(getResources().getColor(colorResId));
    }

    public int getColorRipple() {
        return mColorRipple;
    }

    public void setColorRipple(int color) {
        if (color != mColorRipple) {
            mColorRipple = color;
            updateBackground();
        }
    }

    public void setColorDisabledResId(int colorResId) {
        setColorDisabled(getResources().getColor(colorResId));
    }

    public int getColorDisabled() {
        return mColorDisabled;
    }

    public void setColorDisabled(int color) {
        if (color != mColorDisabled) {
            mColorDisabled = color;
            updateBackground();
        }
    }

    public void setShowShadow(boolean show) {
        if (mShowShadow != show) {
            mShowShadow = show;
            updateBackground();
        }
    }

    public boolean hasShadow() {
        return !mUsingElevation && mShowShadow;
    }

    /**
     * Sets the shadow radius of the <b>ExtendedFloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowRadius(int dimenResId) {
        int shadowRadius = getResources().getDimensionPixelSize(dimenResId);
        if (mShadowRadius != shadowRadius) {
            mShadowRadius = shadowRadius;
            requestLayout();
            updateBackground();
        }
    }

    public int getShadowRadius() {
        return mShadowRadius;
    }

    /**
     * Sets the shadow radius of the <b>ExtendedFloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowRadiusDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowRadius(float shadowRadiusDp) {
        mShadowRadius = Util.dpToPx(getContext(), shadowRadiusDp);
        requestLayout();
        updateBackground();
    }

    /**
     * Sets the shadow x offset of the <b>ExtendedFloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowXOffset(int dimenResId) {
        int shadowXOffset = getResources().getDimensionPixelSize(dimenResId);
        if (mShadowXOffset != shadowXOffset) {
            mShadowXOffset = shadowXOffset;
            requestLayout();
            updateBackground();
        }
    }

    public int getShadowXOffset() {
        return mShadowXOffset;
    }

    /**
     * Sets the shadow x offset of the <b>ExtendedFloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowXOffsetDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowXOffset(float shadowXOffsetDp) {
        mShadowXOffset = Util.dpToPx(getContext(), shadowXOffsetDp);
        requestLayout();
        updateBackground();
    }

    /**
     * Sets the shadow y offset of the <b>ExtendedFloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowYOffset(int dimenResId) {
        int shadowYOffset = getResources().getDimensionPixelSize(dimenResId);
        if (mShadowYOffset != shadowYOffset) {
            mShadowYOffset = shadowYOffset;
            requestLayout();
            updateBackground();
        }
    }

    public int getShadowYOffset() {
        return mShadowYOffset;
    }

    /**
     * Sets the shadow y offset of the <b>ExtendedFloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowYOffsetDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowYOffset(float shadowYOffsetDp) {
        mShadowYOffset = Util.dpToPx(getContext(), shadowYOffsetDp);
        requestLayout();
        updateBackground();
    }

    public void setShadowColorResource(int colorResId) {
        int shadowColor = getResources().getColor(colorResId);
        if (mShadowColor != shadowColor) {
            mShadowColor = shadowColor;
            updateBackground();
        }
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    public void setShadowColor(int color) {
        if (mShadowColor != color) {
            mShadowColor = color;
            updateBackground();
        }
    }

    /**
     * Checks whether <b>ExtendedFloatingActionButton</b> is hidden
     *
     * @return true if <b>ExtendedFloatingActionButton</b> is hidden, false otherwise
     */
    public boolean isHidden() {
        return getVisibility() == INVISIBLE;
    }

    /**
     * Makes the <b>ExtendedFloatingActionButton</b> to appear and sets its visibility to {@link #VISIBLE}
     *
     * @param animate if true - plays "show animation"
     */
    public void show(boolean animate) {
        if (isHidden()) {
            if (animate) {
                playShowAnimation();
            }
            super.setVisibility(VISIBLE);
        }
    }

    /**
     * Makes the <b>ExtendedFloatingActionButton</b> to disappear and sets its visibility to {@link #INVISIBLE}
     *
     * @param animate if true - plays "hide animation"
     */
    public void hide(boolean animate) {
        if (!isHidden()) {
            if (animate) {
                playHideAnimation();
            }
            super.setVisibility(INVISIBLE);
        }
    }

    public void toggle(boolean animate) {
        if (isHidden()) {
            show(animate);
        } else {
            hide(animate);
        }
    }

    public String getLabelText() {
        return mLabelText;
    }

    public void setLabelText(String text) {
        mLabelText = text;
        TextView labelView = getLabelView();
        if (labelView != null) {
            labelView.setText(text);
        }
    }

    public int getLabelVisibility() {
        TextView labelView = getLabelView();
        if (labelView != null) {
            return labelView.getVisibility();
        }

        return -1;
    }

    public void setLabelVisibility(int visibility) {
        ExtendedLabel labelView = getLabelView();
        if (labelView != null) {
            labelView.setVisibility(visibility);
            labelView.setHandleVisibilityChanges(visibility == VISIBLE);
        }
    }

    @Override
    public void setElevation(float elevation) {
        if (Util.hasLollipop() && elevation > 0) {
            super.setElevation(elevation);
            if (!isInEditMode()) {
                mUsingElevation = true;
                mShowShadow = false;
            }
            updateBackground();
        }
    }

    /**
     * Sets the shadow color and radius to mimic the native elevation.
     * <p>
     * <p><b>API 21+</b>: Sets the native elevation of this view, in pixels. Updates margins to
     * make the view hold its position in layout across different platform versions.</p>
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setElevationCompat(float elevation) {
        mShadowColor = 0x26000000;
        mShadowRadius = Math.round(elevation / 2);
        mShadowXOffset = 0;
        mShadowYOffset = Math.round(mFabSize == SIZE_NORMAL ? elevation : elevation / 2);

        if (Util.hasLollipop()) {
            super.setElevation(elevation);
            mUsingElevationCompat = true;
            mShowShadow = false;
            updateBackground();

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams != null) {
                setLayoutParams(layoutParams);
            }
        } else {
            mShowShadow = true;
            updateBackground();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ExtendedLabel label = (ExtendedLabel) getTag(R.id.fab_label);
        if (label != null) {
            label.setEnabled(enabled);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        ExtendedLabel label = (ExtendedLabel) getTag(R.id.fab_label);
        if (label != null) {
            label.setVisibility(visibility);
        }
    }

    /**
     * <b>This will clear all AnimationListeners.</b>
     */
    public void hideButtonInMenu(boolean animate) {
        if (!isHidden() && getVisibility() != GONE) {
            hide(animate);

            ExtendedLabel label = getLabelView();
            if (label != null) {
                label.setVisibility(View.INVISIBLE);
            }

            getHideAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(GONE);
                    getHideAnimation().setAnimationListener(null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    public void showButtonInMenu(boolean animate) {
        if (getVisibility() == VISIBLE) return;

        setVisibility(INVISIBLE);
        show(animate);
        ExtendedLabel label = getLabelView();
        if (label != null) {
            label.show();
        }
    }

    /**
     * Set the label's background colors
     */
    public void setLabelColors(int colorNormal, int colorPressed, int colorRipple) {
        ExtendedLabel label = getLabelView();

        int left = label.getPaddingLeft();
        int top = label.getPaddingTop();
        int right = label.getPaddingRight();
        int bottom = label.getPaddingBottom();

        label.setPadding(left, top, right, bottom);
    }

    public void setLabelTextColor(int color) {
        getLabelView().setTextColor(color);
    }

    public void setLabelTextColor(ColorStateList colors) {
        getLabelView().setTextColor(colors);
    }

    public void setExtended(Boolean isExtended) {
        mColorNormal = mColorExtendedNormal;
        mColorPressed = mColorExtendedPressed;
        mColorRipple = mColorExtendedRipple;
        mColorDisabled = mColorExtendedDisabled;
        setColorRipple(mColorRipple);
        setIconColor(getActionMenu().getMenuButtonColorNormal());
    }

    public void setIconColor(int color) {
        PorterDuff.Mode filterMode = PorterDuff.Mode.SRC_ATOP;
        mIcon.setColorFilter(color, filterMode);
    }

    private class CircleDrawable extends ShapeDrawable {

        private int circleInsetHorizontal;
        private int circleInsetVertical;

        protected CircleDrawable() {
        }

        private CircleDrawable(Shape s) {
            super(s);
            circleInsetHorizontal = hasShadow() ? mShadowRadius + Math.abs(mShadowXOffset) : 0;
            circleInsetVertical = hasShadow() ? mShadowRadius + Math.abs(mShadowYOffset) : 0;
        }

        @Override
        public void draw(Canvas canvas) {
            setBounds(circleInsetHorizontal, circleInsetVertical, calculateMeasuredWidth()
                    - circleInsetHorizontal, calculateMeasuredHeight() - circleInsetVertical);

            super.draw(canvas);
        }
    }

    private class Shadow extends Drawable {

        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint mErase = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float mRadius;

        private Shadow() {
            this.init();
        }

        private void init() {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mColorNormal);
            mErase.setXfermode(PORTER_DUFF_CLEAR);
            mPaint.setShadowLayer(mShadowRadius, mShadowXOffset, mShadowYOffset, mShadowColor);
            mRadius = getCircleSize() / 2;
        }

        @Override
        public void draw(Canvas canvas) {
            drawRoundRectangleCanvas(canvas, mPaint);
            drawRoundRectangleCanvas(canvas, mErase);
        }

        private void drawRoundRectangleCanvas(Canvas canvas, Paint paint) {
            float canvasXOffset = Util.dpToPx(getContext(), 6f);  // We need shadow to has same width as button.
            float canvasYOffset = Util.dpToPx(getContext(), 18);  // We need shadow to be located little bit lower.
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                canvas.drawRoundRect(new RectF(canvasXOffset, canvasYOffset, Util.getScreenWidth(getContext()) - (getExtendedButtonPadding() + Util.dpToPx(getContext(), 7f)), Util.dpToPx(getContext(), 60f)), 70f, 70f, paint);
            } else {
                canvas.drawRoundRect(new RectF(canvasXOffset, canvasYOffset, getExtendedButtonLandscapeWidth() - canvasXOffset, Util.dpToPx(getContext(), 60f)), 70f, 70f, paint);
            }
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }
}