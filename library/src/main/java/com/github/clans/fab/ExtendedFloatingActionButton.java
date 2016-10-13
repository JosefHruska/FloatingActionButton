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
import android.os.SystemClock;
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

/**
 * Menu button class for extended version of FloatingActionMenu
 *
 * @author Josef Hruška (josef@stepuplabs.io)
 */

public class ExtendedFloatingActionButton extends ImageButton {

    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_MINI = 1;
    private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private static final long PAUSE_GROWING_TIME = 200;
    private static final double BAR_SPIN_CYCLE_TIME = 500;
    private static final int BAR_MAX_LENGTH = 270;
    int mFabSize;
    boolean mShowShadow;
    int mShadowColor;
    int mShadowRadius = Util.dpToPx(getContext(), 4f);
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
    // Progress
    private boolean mProgressBarEnabled;
    private int mProgressWidth = Util.dpToPx(getContext(), 6f);
    private int mProgressColor;
    private int mProgressBackgroundColor;
    private boolean mShouldUpdateButtonPosition;
    private float mOriginalX = -1;
    private float mOriginalY = -1;
    private boolean mButtonPositionSaved;
    private RectF mProgressCircleBounds = new RectF();
    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean mProgressIndeterminate;
    private long mLastTimeAnimated;
    private float mSpinSpeed = 195.0f; //The amount of degrees per second
    private long mPausedTimeWithoutGrowing = 0;
    private double mTimeStartGrowing;
    private boolean mBarGrowingFromFront = true;
    private int mBarLength = 16;
    private float mBarExtraLength;
    private float mCurrentProgress;
    private float mTargetProgress;
    private int mProgress;
    private boolean mAnimateProgress;
    private boolean mShouldProgressIndeterminate;
    private boolean mShouldSetProgress;
    private int mProgressMax = 100;
    private boolean mShowProgressBackground;
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
        mShouldProgressIndeterminate = attr.getBoolean(R.styleable.FloatingActionButton_fab_progress_indeterminate, false);
        mProgressColor = attr.getColor(R.styleable.FloatingActionButton_fab_progress_color, 0xFF009688);
        mProgressBackgroundColor = attr.getColor(R.styleable.FloatingActionButton_fab_progress_backgroundColor, 0x4D000000);
        mProgressMax = attr.getInt(R.styleable.FloatingActionButton_fab_progress_max, mProgressMax);
        mShowProgressBackground = attr.getBoolean(R.styleable.FloatingActionButton_fab_progress_showBackground, true);

        if (attr.hasValue(R.styleable.FloatingActionButton_fab_progress)) {
            mProgress = attr.getInt(R.styleable.FloatingActionButton_fab_progress, 0);
            mShouldSetProgress = true;
        }

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

        if (isInEditMode()) {
            if (mShouldProgressIndeterminate) {
                setIndeterminate(true);
            } else if (mShouldSetProgress) {
                saveButtonOriginalPosition();
                setProgress(mProgress, false);
            }
        }
        setClickable(true);
    }

    private void initShowAnimation(TypedArray attr) {
        int resourceId = attr.getResourceId(R.styleable.FloatingActionButton_fab_showAnimation, R.anim.fab_scale_up);
        mShowAnimation = AnimationUtils.loadAnimation(getContext(), resourceId);
    }

    private void initHideAnimation(TypedArray attr) {
        int resourceId = attr.getResourceId(R.styleable.FloatingActionButton_fab_hideAnimation, R.anim.fab_scale_down);
        mHideAnimation = AnimationUtils.loadAnimation(getContext(), resourceId);
    }

    private void initHideExtendedAnimation() {
        mHideExtendedAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fab_extended_hide);
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


        if (mProgressBarEnabled) {
            width += mProgressWidth * 2;
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
        int height;

        height = getCircleSize() + calculateShadowHeight();
        if (mProgressBarEnabled) {
            height += mProgressWidth * 2;
        }
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mProgressBarEnabled) {
            if (mShowProgressBackground) {
                canvas.drawArc(mProgressCircleBounds, 360, 360, false, mBackgroundPaint);
            }

            boolean shouldInvalidate = false;

            if (mProgressIndeterminate) {
                shouldInvalidate = true;

                long deltaTime = SystemClock.uptimeMillis() - mLastTimeAnimated;
                float deltaNormalized = deltaTime * mSpinSpeed / 1000.0f;

                updateProgressLength(deltaTime);

                mCurrentProgress += deltaNormalized;
                if (mCurrentProgress > 360f) {
                    mCurrentProgress -= 360f;
                }

                mLastTimeAnimated = SystemClock.uptimeMillis();
                float from = mCurrentProgress - 90;
                float to = mBarLength + mBarExtraLength;

                if (isInEditMode()) {
                    from = 0;
                    to = 135;
                }

                canvas.drawArc(mProgressCircleBounds, from, to, false, mProgressPaint);
            } else {
                if (mCurrentProgress != mTargetProgress) {
                    shouldInvalidate = true;
                    float deltaTime = (float) (SystemClock.uptimeMillis() - mLastTimeAnimated) / 1000;
                    float deltaNormalized = deltaTime * mSpinSpeed;

                    if (mCurrentProgress > mTargetProgress) {
                        mCurrentProgress = Math.max(mCurrentProgress - deltaNormalized, mTargetProgress);
                    } else {
                        mCurrentProgress = Math.min(mCurrentProgress + deltaNormalized, mTargetProgress);
                    }
                    mLastTimeAnimated = SystemClock.uptimeMillis();
                }

                canvas.drawArc(mProgressCircleBounds, -90, mCurrentProgress, false, mProgressPaint);
            }

            if (shouldInvalidate) {
                invalidate();
            }
        }
    }

    private void updateProgressLength(long deltaTimeInMillis) {
        if (mPausedTimeWithoutGrowing >= PAUSE_GROWING_TIME) {
            mTimeStartGrowing += deltaTimeInMillis;

            if (mTimeStartGrowing > BAR_SPIN_CYCLE_TIME) {
                mTimeStartGrowing -= BAR_SPIN_CYCLE_TIME;
                mPausedTimeWithoutGrowing = 0;
                mBarGrowingFromFront = !mBarGrowingFromFront;
            }

            float distance = (float) Math.cos((mTimeStartGrowing / BAR_SPIN_CYCLE_TIME + 1) * Math.PI) / 2 + 0.5f;
            float length = BAR_MAX_LENGTH - mBarLength;

            if (mBarGrowingFromFront) {
                mBarExtraLength = distance * length;
            } else {
                float newLength = length * (1 - distance);
                mCurrentProgress += (mBarExtraLength - newLength);
                mBarExtraLength = newLength;
            }
        } else {
            mPausedTimeWithoutGrowing += deltaTimeInMillis;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        saveButtonOriginalPosition();

        if (mShouldProgressIndeterminate) {
            setIndeterminate(true);
            mShouldProgressIndeterminate = false;
        } else if (mShouldSetProgress) {
            setProgress(mProgress, mAnimateProgress);
            mShouldSetProgress = false;
        } else if (mShouldUpdateButtonPosition) {
            updateButtonPosition();
            mShouldUpdateButtonPosition = false;
        }
        super.onSizeChanged(w, h, oldw, oldh);

        setupProgressBounds();
        setupProgressBarPaints();
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

        if (mProgressBarEnabled) {
            circleInsetVertical += mProgressWidth;
        }

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

    private void saveButtonOriginalPosition() {
        if (!mButtonPositionSaved) {
            if (mOriginalX == -1) {
                mOriginalX = getX();
            }

            if (mOriginalY == -1) {
                mOriginalY = getY();
            }

            mButtonPositionSaved = true;
        }
    }

    private void updateButtonPosition() {
        float x;
        float y;
        if (mProgressBarEnabled) {
            x = mOriginalX > getX() ? getX() + mProgressWidth : getX() - mProgressWidth;
            y = mOriginalY > getY() ? getY() + mProgressWidth : getY() - mProgressWidth;
        } else {
            x = mOriginalX;
            y = mOriginalY;
        }
        setX(x);
        setY(y);
        if (getParent() instanceof ExtendedFloatingActionMenu) {
            getActionMenu().setX(x);
            getActionMenu().setY(y);
        }
    }

    private void setupProgressBarPaints() {
        mBackgroundPaint.setColor(mProgressBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mProgressWidth);

        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);
    }

    private void setupProgressBounds() {
        int circleInsetHorizontal = hasShadow() ? getShadowX() : 0;
        int circleInsetVertical = hasShadow() ? getShadowY() : 0;
        mProgressCircleBounds = new RectF(
                circleInsetHorizontal + mProgressWidth / 2,
                circleInsetVertical + mProgressWidth / 2,
                calculateMeasuredWidth() - circleInsetHorizontal - mProgressWidth / 2,
                calculateMeasuredHeight() - circleInsetVertical - mProgressWidth / 2
        );
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

    void playReplaceExtendedAnimation() {
        startAnimation(mReplaceExtendedAnimation);
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

    /**
     * Sets the size of the <b>ExtendedFloatingActionButton</b> and invalidates its layout.
     *
     * @param size size of the <b>ExtendedFloatingActionButton</b>. Accepted values: SIZE_NORMAL, SIZE_MINI.
     */
    public void setButtonSize(int size) {
        if (size != SIZE_NORMAL && size != SIZE_MINI) {
            throw new IllegalArgumentException("Use @FabSize constants only!");
        }

        if (mFabSize != size) {
            mFabSize = size;
            updateBackground();
        }
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

    /**
     * <p>Change the indeterminate mode for the progress bar. In indeterminate
     * mode, the progress is ignored and the progress bar shows an infinite
     * animation instead.</p>
     *
     * @param indeterminate true to enable the indeterminate mode
     */
    public synchronized void setIndeterminate(boolean indeterminate) {
        if (!indeterminate) {
            mCurrentProgress = 0.0f;
        }

        mProgressBarEnabled = indeterminate;
        mShouldUpdateButtonPosition = true;
        mProgressIndeterminate = indeterminate;
        mLastTimeAnimated = SystemClock.uptimeMillis();
        setupProgressBounds();
        updateBackground();
    }

    public synchronized int getMax() {
        return mProgressMax;
    }

    public synchronized void setMax(int max) {
        mProgressMax = max;
    }

    public synchronized void setProgress(int progress, boolean animate) {
        if (mProgressIndeterminate) return;

        mProgress = progress;
        mAnimateProgress = animate;

        if (!mButtonPositionSaved) {
            mShouldSetProgress = true;
            return;
        }

        mProgressBarEnabled = true;
        mShouldUpdateButtonPosition = true;
        setupProgressBounds();
        saveButtonOriginalPosition();
        updateBackground();

        if (progress < 0) {
            progress = 0;
        } else if (progress > mProgressMax) {
            progress = mProgressMax;
        }

        if (progress == mTargetProgress) {
            return;
        }

        mTargetProgress = mProgressMax > 0 ? (progress / (float) mProgressMax) * 360 : 0;
        mLastTimeAnimated = SystemClock.uptimeMillis();

        if (!animate) {
            mCurrentProgress = mTargetProgress;
        }

        invalidate();
    }

    public synchronized int getProgress() {
        return mProgressIndeterminate ? 0 : mProgress;
    }

    public synchronized void hideProgress() {
        mProgressBarEnabled = false;
        mShouldUpdateButtonPosition = true;
        updateBackground();
    }

    public synchronized void setShowProgressBackground(boolean show) {
        mShowProgressBackground = show;
    }

    public synchronized boolean isProgressBackgroundShown() {
        return mShowProgressBackground;
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

//        label.updateBackground();
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

    public void setNormalMenuLabelColors() {
        if (getLabelView() != null) {
//            getLabelView().updateBackground();
        }
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

            if (mProgressBarEnabled) {
                circleInsetHorizontal += mProgressWidth;
                circleInsetVertical += mProgressWidth;
            }
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

            if (!isInEditMode()) {
                mPaint.setShadowLayer(mShadowRadius, mShadowXOffset, mShadowYOffset, mShadowColor);
            }

            mRadius = getCircleSize() / 2;

            if (mProgressBarEnabled && mShowProgressBackground) {
                mRadius += mProgressWidth;
            }
        }

        @Override
        public void draw(Canvas canvas) {
            drawRoundRectangleCanvas(canvas, mPaint);
        }

        private void drawRoundRectangleCanvas(Canvas canvas, Paint paint) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                canvas.drawRoundRect(new RectF(0, Util.dpToPx(getContext(), 8), Util.getScreenWidth(getContext()) - (getExtendedButtonPadding() + Util.dpToPx(getContext(), 3f)), Util.dpToPx(getContext(), 63f)), 70f, 70f, paint);
            } else {
                canvas.drawRoundRect(new RectF(0, Util.dpToPx(getContext(), 8), getExtendedButtonLandscapeWidth() - Util.dpToPx(getContext(), 2f), Util.dpToPx(getContext(), 63f)), 70f, 70f, paint);
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
