package com.github.clans.fab;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.label;

/**
 * Menu class for extended version of FloatingActionMenu
 *
 * @author Josef Hruška (josef@stepuplabs.io)
 */

public class ExtendedFloatingActionMenu extends ViewGroup implements FloatingMenu {

    protected static final int EXTENDED_ACTION_MENU = -1;
    private static final int ANIMATION_DURATION = 300;
    private static final float CLOSED_PLUS_ROTATION = 0f;
    private static final float OPENED_PLUS_ROTATION_LEFT = -90f - 45f;
    private static final float OPENED_PLUS_ROTATION_RIGHT = 90f + 45f;
    private static final int OPEN_UP = 0;

    private static final int LABELS_POSITION_LEFT = 0;
    private AnimatorSet mOpenAnimatorSet = new AnimatorSet();
    private AnimatorSet mCloseAnimatorSet = new AnimatorSet();
    private AnimatorSet mIconToggleSet;
    protected String extendedButtonTextCollapsed = null;
    protected String extendedButtonTextExpanded = null;
    private int mExtendedButtonTextColor = 0;
    private int mExtendedButtonBackgroundColor;
    private int mExtendedButtonTextSize = 0;
    private int mButtonSpacing = Util.dpToPx(getContext(), 0f);
    private int mMaxButtonWidth;
    private int mLabelsMargin = Util.dpToPx(getContext(), 0f);
    private int mLabelsVerticalOffset = Util.dpToPx(getContext(), 0f);
    private int mButtonsCount;
    private boolean mMenuOpened;
    private boolean mIsMenuOpening;
    private Handler mUiHandler = new Handler();
    private int mLabelsPaddingTop = Util.dpToPx(getContext(), 4f);
    private int mLabelsPaddingRight = Util.dpToPx(getContext(), 0f);
    private int mLabelsPaddingBottom = Util.dpToPx(getContext(), 4f);
    private int mLabelsPaddingLeft = Util.dpToPx(getContext(), 8f);
    private ColorStateList mLabelsNormalTextColor;
    private float mLabelsTextSize;
    private int mLabelsCornerRadius = Util.dpToPx(getContext(), 3f);
    private boolean mLabelsShowShadow;
    private boolean mMenuShowShadow;
    private int mMenuShadowColor;
    private float mMenuShadowRadius = 4f;
    private float mMenuShadowXOffset = 1f;
    private float mMenuShadowYOffset = 3f;
    private int mMenuColorNormal;
    private int mMenuColorPressed;
    private int mMenuColorRipple;
    private Drawable mIcon;
    private int mAnimationDelayPerItem;
    private Interpolator mOpenInterpolator;
    private Interpolator mCloseInterpolator;
    private boolean mIsAnimated = true;
    private boolean animationInProgress = false;
    private boolean mLabelsSingleLine;
    private int mLabelsEllipsize;
    private int mLabelsMaxLines;
    private int mLabelsStyle;
    private Typeface mCustomTypefaceFromFont;
    private boolean mIconAnimated = true;
    private ImageView mImageToggle;
    protected TextView mMenuText;
    private Animation mMenuButtonShowAnimation;
    private Animation mMenuButtonHideAnimation;
    private Animation mImageToggleShowAnimation;
    private Animation mImageToggleHideAnimation;
    private boolean mIsMenuButtonAnimationRunning;
    private boolean mIsSetClosedOnTouchOutside;
    private int mOpenDirection;
    private FloatingMenuToggleListener mToggleListener;
    private ExtendedFloatingActionButton mMenuButton;
    private ValueAnimator mShowBackgroundAnimator;
    private ValueAnimator mHideBackgroundAnimator;
    private int mBackgroundColor;
    private int mLabelsPosition;
    private Context mLabelsContext;
    private String mMenuLabelText;
    private boolean mUsingMenuLabel;

    public ExtendedFloatingActionMenu(Context context) {
        this(context, null);
    }

    public ExtendedFloatingActionMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExtendedFloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionMenu, 0, 0);
        extendedButtonTextExpanded = attr.getString(R.styleable.FloatingActionMenu_menu_extendedButtonTextExpanded);
        extendedButtonTextCollapsed = attr.getString(R.styleable.FloatingActionMenu_menu_extendedButtonTextCollapsed);
        mExtendedButtonTextColor = attr.getInt(R.styleable.FloatingActionMenu_menu_extendedButtonTextColor, Color.WHITE);
        mExtendedButtonBackgroundColor = attr.getInt(R.styleable.FloatingActionMenu_menu_extendedButtonTextColor, Color.WHITE);
        mExtendedButtonTextSize = attr.getInt(R.styleable.FloatingActionMenu_menu_extendedButtonTextSize, 16);
        mButtonSpacing = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_buttonSpacing, mButtonSpacing);
        mLabelsMargin = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_margin, mLabelsMargin);
        mLabelsPosition = attr.getInt(R.styleable.FloatingActionMenu_menu_labels_position, LABELS_POSITION_LEFT);
        mLabelsPaddingTop = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingTop, mLabelsPaddingTop);
        mLabelsPaddingRight = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingRight, mLabelsPaddingRight);
        mLabelsPaddingBottom = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingBottom, mLabelsPaddingBottom);
        mLabelsPaddingLeft = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingLeft, mLabelsPaddingLeft);
        mLabelsNormalTextColor = attr.getColorStateList(R.styleable.FloatingActionMenu_menu_labels_textNormalColorList);
        if (mLabelsNormalTextColor == null) {
            mLabelsNormalTextColor = ColorStateList.valueOf(Color.WHITE);
        }
        ColorStateList mLabelsExtendedTextColor = attr.getColorStateList(R.styleable.FloatingActionMenu_menu_labels_textExtendedColorList);
        if (mLabelsExtendedTextColor == null) {
            mLabelsExtendedTextColor = ColorStateList.valueOf(Color.WHITE);
        }
        mLabelsTextSize = attr.getDimension(R.styleable.FloatingActionMenu_menu_labels_textSize, getResources().getDimension(R.dimen.labels_text_size));
        mLabelsCornerRadius = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_cornerRadius, mLabelsCornerRadius);
        mLabelsShowShadow = attr.getBoolean(R.styleable.FloatingActionMenu_menu_labels_showShadow, true);
        mMenuShowShadow = attr.getBoolean(R.styleable.FloatingActionMenu_menu_showShadow, true);
        mMenuShadowColor = attr.getColor(R.styleable.FloatingActionMenu_menu_shadowColor, 0x66000000);
        mMenuShadowRadius = attr.getDimension(R.styleable.FloatingActionMenu_menu_shadowRadius, mMenuShadowRadius);
        mMenuShadowXOffset = attr.getDimension(R.styleable.FloatingActionMenu_menu_shadowXOffset, mMenuShadowXOffset);
        mMenuShadowYOffset = attr.getDimension(R.styleable.FloatingActionMenu_menu_shadowYOffset, mMenuShadowYOffset);
        mMenuColorNormal = attr.getColor(R.styleable.FloatingActionMenu_menu_colorNormal, 0xFFDA4336);
        mMenuColorPressed = attr.getColor(R.styleable.FloatingActionMenu_menu_colorPressed, 0xFFE75043);
        mMenuColorRipple = attr.getColor(R.styleable.FloatingActionMenu_menu_colorRipple, 0x99FFFFFF);
        mAnimationDelayPerItem = attr.getInt(R.styleable.FloatingActionMenu_menu_animationDelayPerItem, 50);
        mIcon = attr.getDrawable(R.styleable.FloatingActionMenu_menu_icon);
        if (mIcon == null) {
            mIcon = getResources().getDrawable(R.drawable.fab_add);
        }
        mLabelsSingleLine = attr.getBoolean(R.styleable.FloatingActionMenu_menu_labels_singleLine, false);
        mLabelsEllipsize = attr.getInt(R.styleable.FloatingActionMenu_menu_labels_ellipsize, 0);
        mLabelsMaxLines = attr.getInt(R.styleable.FloatingActionMenu_menu_labels_maxLines, -1);
        int mMenuFabSize = attr.getInt(R.styleable.FloatingActionMenu_menu_fab_size, FloatingActionButton.SIZE_NORMAL);
        mLabelsStyle = attr.getResourceId(R.styleable.FloatingActionMenu_menu_labels_style, 0);
        String customFont = attr.getString(R.styleable.FloatingActionMenu_menu_labels_customFont);
        try {
            if (!TextUtils.isEmpty(customFont)) {
                mCustomTypefaceFromFont = Typeface.createFromAsset(getContext().getAssets(), customFont);
            }
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Unable to load specified custom font: " + customFont, ex);
        }
        mOpenDirection = attr.getInt(R.styleable.FloatingActionMenu_menu_openDirection, OPEN_UP);
        mBackgroundColor = attr.getColor(R.styleable.FloatingActionMenu_menu_backgroundColor, Color.TRANSPARENT);

        if (attr.hasValue(R.styleable.FloatingActionMenu_menu_fab_label)) {
            mUsingMenuLabel = true;
            mMenuLabelText = attr.getString(R.styleable.FloatingActionMenu_menu_fab_label);
        }

        if (attr.hasValue(R.styleable.FloatingActionMenu_menu_labels_padding)) {
            int padding = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_padding, 0);
            initPadding(padding);
        }

        mOpenInterpolator = new OvershootInterpolator();
        mCloseInterpolator = new AnticipateInterpolator();
        mLabelsContext = new ContextThemeWrapper(getContext(), mLabelsStyle);

        initBackgroundDimAnimation();
        createExtendedMenuButton();
        initMenuButtonAnimations(attr);
        attr.recycle();
    }

    private void initMenuButtonAnimations(TypedArray attr) {
        int showResId = attr.getResourceId(R.styleable.FloatingActionMenu_menu_fab_show_animation, R.anim.fab_scale_up);
        setMenuButtonShowAnimation(AnimationUtils.loadAnimation(getContext(), showResId));
        mImageToggleShowAnimation = AnimationUtils.loadAnimation(getContext(), showResId);
        int hideResId = attr.getResourceId(R.styleable.FloatingActionMenu_menu_fab_hide_animation, R.anim.fab_scale_down);
        setMenuButtonHideAnimation(AnimationUtils.loadAnimation(getContext(), hideResId));
        mImageToggleHideAnimation = AnimationUtils.loadAnimation(getContext(), hideResId);
    }

    private void initBackgroundDimAnimation() {
        final int maxAlpha = Color.alpha(mBackgroundColor);
        final int red = Color.red(mBackgroundColor);
        final int green = Color.green(mBackgroundColor);
        final int blue = Color.blue(mBackgroundColor);

        mShowBackgroundAnimator = ValueAnimator.ofInt(0, maxAlpha);
        mShowBackgroundAnimator.setDuration(ANIMATION_DURATION);
        mShowBackgroundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer alpha = (Integer) animation.getAnimatedValue();
                setBackgroundColor(Color.argb(alpha, red, green, blue));
            }
        });

        mHideBackgroundAnimator = ValueAnimator.ofInt(maxAlpha, 0);
        mHideBackgroundAnimator.setDuration(ANIMATION_DURATION);
        mHideBackgroundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer alpha = (Integer) animation.getAnimatedValue();
                setBackgroundColor(Color.argb(alpha, red, green, blue));
            }
        });
    }

    private boolean isBackgroundEnabled() {
        return mBackgroundColor != Color.TRANSPARENT;
    }

    private void initPadding(int padding) {
        mLabelsPaddingTop = padding;
        mLabelsPaddingRight = padding;
        mLabelsPaddingBottom = padding;
        mLabelsPaddingLeft = padding;
    }

    private void createExtendedMenuButton() { // Create wide (extended) menu button

        mMenuButton = new ExtendedFloatingActionButton(getContext(), null, EXTENDED_ACTION_MENU);
        mMenuButton.mShowShadow = mMenuShowShadow;
        if (mMenuShowShadow) {
            mMenuButton.mShadowRadius = Util.dpToPx(getContext(), mMenuShadowRadius);
            mMenuButton.mShadowXOffset = Util.dpToPx(getContext(), mMenuShadowXOffset);
            mMenuButton.mShadowYOffset = Util.dpToPx(getContext(), mMenuShadowYOffset);
        }
        mMenuButton.setColors(mMenuColorNormal, mMenuColorPressed, mMenuColorRipple);
        mMenuButton.mShadowColor = mMenuShadowColor;
        mMenuButton.updateBackground();
        mMenuButton.setLabelText(mMenuLabelText);

        mMenuText = new TextView(getContext());
        mMenuText.setTypeface(null, Typeface.BOLD);
        mMenuText.setText(extendedButtonTextCollapsed);
        mMenuText.setTextSize(mExtendedButtonTextSize);
        mMenuText.setTextColor(mExtendedButtonTextColor);

        mImageToggle = new ImageView(getContext());
        mImageToggle.setImageDrawable(mIcon);

        addView(mMenuButton, super.generateDefaultLayoutParams());
        addView(mMenuText);
        addView(mImageToggle);

        createDefaultIconAnimation();
    }

    private void createDefaultIconAnimation() {
        float collapseAngle;
        float expandAngle;
        if (mOpenDirection == OPEN_UP) {
            collapseAngle = mLabelsPosition == LABELS_POSITION_LEFT ? OPENED_PLUS_ROTATION_LEFT : OPENED_PLUS_ROTATION_RIGHT;
            expandAngle = mLabelsPosition == LABELS_POSITION_LEFT ? OPENED_PLUS_ROTATION_LEFT : OPENED_PLUS_ROTATION_RIGHT;
        } else {
            collapseAngle = mLabelsPosition == LABELS_POSITION_LEFT ? OPENED_PLUS_ROTATION_RIGHT : OPENED_PLUS_ROTATION_LEFT;
            expandAngle = mLabelsPosition == LABELS_POSITION_LEFT ? OPENED_PLUS_ROTATION_RIGHT : OPENED_PLUS_ROTATION_LEFT;
        }

        ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(
                mImageToggle,
                "rotation",
                collapseAngle,
                CLOSED_PLUS_ROTATION
        );

        ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(
                mImageToggle,
                "rotation",
                CLOSED_PLUS_ROTATION,
                expandAngle
        );

        mOpenAnimatorSet.play(expandAnimator);
        mCloseAnimatorSet.play(collapseAnimator);

        mOpenAnimatorSet.setInterpolator(mOpenInterpolator);
        mCloseAnimatorSet.setInterpolator(mCloseInterpolator);

        mOpenAnimatorSet.setDuration(ANIMATION_DURATION);
        mCloseAnimatorSet.setDuration(ANIMATION_DURATION);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;
        mMaxButtonWidth = 0;
        int maxLabelWidth = 0;

        measureChildWithMargins(mImageToggle, widthMeasureSpec, 0, heightMeasureSpec, 0);
        measureChildWithMargins(mMenuText, widthMeasureSpec, 0, heightMeasureSpec, 0);

        for (int i = 0; i < mButtonsCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE || child == mImageToggle || child == mMenuText)
                continue;

            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
        }

        for (int i = 0; i < mButtonsCount; i++) {
            int usedWidth = 0;
            View child = getChildAt(i);

            if (child.getVisibility() == GONE || child == mImageToggle || child == mMenuText)
                continue;

            usedWidth += child.getMeasuredWidth();
            height += child.getMeasuredHeight();

            ExtendedLabel label = (ExtendedLabel) child.getTag(R.id.fab_label);
            if (label != null) {
                int labelOffset = (mMaxButtonWidth - child.getMeasuredWidth()) / (mUsingMenuLabel ? 1 : 2);
                int labelUsedWidth = child.getMeasuredWidth() + mLabelsMargin + labelOffset;
                measureChildWithMargins(label, widthMeasureSpec, 0, heightMeasureSpec, 0); // I allow label to use just the space which is not already used by its FAB - we dont need it with extended FAB it would cause visibility problems
                usedWidth += label.getMeasuredWidth();
                maxLabelWidth = Math.max(maxLabelWidth, usedWidth + labelOffset);
            }
        }

        width = Math.max(mMaxButtonWidth, maxLabelWidth + mLabelsMargin) + getPaddingLeft() + getPaddingRight();
        height += mButtonSpacing * (mButtonsCount - 1) + getPaddingTop() + getPaddingBottom();
        height = adjustForOvershoot(height);
        if (getLayoutParams().width == LayoutParams.MATCH_PARENT) {
            width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        }

        if (getLayoutParams().height == LayoutParams.MATCH_PARENT) {
            height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int buttonsHorizontalCenter = mLabelsPosition == LABELS_POSITION_LEFT
                ? r - l - mMaxButtonWidth / 2 - getPaddingRight()
                : mMaxButtonWidth / 2 + getPaddingRight();
        boolean openUp = mOpenDirection == OPEN_UP;

        int leftTranslation = 0;
        int menuButtonTop = openUp
                ? b - t - mMenuButton.calculateMeasuredHeight() - getPaddingBottom()
                : getPaddingTop();
        int menuButtonLeft = buttonsHorizontalCenter - mMenuButton.calculateMeasuredWidth() / 2;
        leftTranslation = Util.dpToPx(getContext(), 8);
        mMenuButton.layout(menuButtonLeft + leftTranslation, menuButtonTop, menuButtonLeft + leftTranslation + mMenuButton.getMeasuredWidth(),
                menuButtonTop + mMenuButton.getMeasuredHeight());

        int nextY = openUp
                ? menuButtonTop + mMenuButton.calculateMeasuredHeight() + mButtonSpacing
                : menuButtonTop;

        for (int i = mButtonsCount - 1; i >= 0; i--) {
            View child = getChildAt(i);

            if (child == mMenuText || child == mImageToggle || child instanceof TextView) continue;

            ExtendedFloatingActionButton fab = (ExtendedFloatingActionButton) child;

            if (fab.getVisibility() == GONE) continue;

            int childExtendedExtraX = getResources().getDimensionPixelSize(R.dimen.extended_button_child_left_offset);
            int childX = 0;
            int childY = 0;
            childX = buttonsHorizontalCenter - fab.getMeasuredWidth() / 2 + childExtendedExtraX;
            childY = openUp ? nextY - fab.getMeasuredHeight() - mButtonSpacing : nextY;

            if (fab != mMenuButton) {

                fab.setExtended(true);
                fab.setLabelTextColor(getMenuButtonColorNormal());
                fab.measure(fab.calculateMeasuredWidth(), fab.calculateMeasuredHeight());

                fab.layout(childX, childY, childX + fab.calculateMeasuredWidth(),
                        childY + fab.calculateMeasuredHeight());

                if (!mIsMenuOpening) {
                    fab.hide(false);
                }
            }

            View label = (View) fab.getTag(R.id.fab_label);
            if (label != null) {
                int labelsOffset = (mUsingMenuLabel ? mMaxButtonWidth / 2 : fab.getMeasuredWidth() / 2) + mLabelsMargin;
                int labelXNearButton = mLabelsPosition == LABELS_POSITION_LEFT
                        ? buttonsHorizontalCenter - labelsOffset
                        : buttonsHorizontalCenter + labelsOffset;

                int labelXAwayFromButton = mLabelsPosition == LABELS_POSITION_LEFT
                        ? labelXNearButton - label.getMeasuredWidth()
                        : labelXNearButton + label.getMeasuredWidth();

                int labelLeft = mLabelsPosition == LABELS_POSITION_LEFT
                        ? labelXAwayFromButton
                        : labelXNearButton;

                int labelRight = mLabelsPosition == LABELS_POSITION_LEFT
                        ? labelXNearButton
                        : labelXAwayFromButton;

                int fabVerticalCenter = Math.round(fab.getY() + fab.getCircleSize() / 2);
                int alignedLabelTop = fabVerticalCenter - Math.round(label.getMeasuredHeight() / 2.5f);
                int alignedLabelBottom = fabVerticalCenter + Math.round(label.getMeasuredHeight() / 1.5f);
                int relatedButtonWidth = fab.calculateMeasuredWidth() / 2;
                float WHITE_SPACE_PERCENTAGE = 1.6f;

                int extendedLabelLeft = (labelLeft + relatedButtonWidth + label.getMeasuredWidth() / 2) + Math.round(mImageToggle.getWidth() * WHITE_SPACE_PERCENTAGE) / 2;
                int extendedLabelRight = (labelRight + relatedButtonWidth + label.getMeasuredWidth() / 2) + Math.round(mImageToggle.getWidth() * WHITE_SPACE_PERCENTAGE) / 2;
                label.layout(extendedLabelLeft, alignedLabelTop, extendedLabelRight, alignedLabelBottom);

                if (!mIsMenuOpening) {
                    label.setVisibility(INVISIBLE);
                }
            }
            int extendedButtonSpacingExtra = getResources().getDimensionPixelSize(R.dimen.extended_button_spacing_extra);
            int buttonSpacing = mButtonSpacing + (extendedButtonSpacingExtra);
            fab.updateBackground();
            nextY = openUp
                    ? childY - buttonSpacing
                    : childY + child.getMeasuredHeight() + buttonSpacing;
        }
        alignMenuText();
        alignMenuIcon();
    }

    private int adjustForOvershoot(int dimension) {
        return (int) (dimension * 0.03 + dimension);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bringChildToFront(mMenuButton);
        bringChildToFront(mImageToggle);
        bringChildToFront(mMenuText);
        mButtonsCount = getChildCount();
        alignMenuText();// FIXME: Do we really need this ?
        alignMenuIcon();
        setAnimationInProgressListener();
        createLabels();
    }

    private void createLabels() {
        for (int i = 0; i < mButtonsCount; i++) {

            if (getChildAt(i) instanceof ExtendedFloatingActionButton) {
                final ExtendedFloatingActionButton fab = (ExtendedFloatingActionButton) getChildAt(i);
                if (fab.getTag(R.id.fab_label) != null) continue;
                if (fab != mMenuButton) {
                    fab.setExtended(true);
                    fab.setBackgroundColor(mExtendedButtonBackgroundColor);
                }
                addLabel(fab);
            }
        }
    }

    private void alignMenuText() {
        int textIconGap = Math.round(getResources().getDimension(R.dimen.extended_button_gap_between_icon_text));
        int textTop = Math.round(mMenuButton.getY()) + mMenuButton.getMeasuredHeight() / 2 - mMenuText.getMeasuredHeight() / 2;
        int buttonRightEdge = Util.getScreenWidth(getContext()) - (getPaddingRight());
        int textLeft = 0;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            textLeft = (Util.getScreenWidth(getContext()) / 2 - mMenuText.getMeasuredWidth() / 2) + mImageToggle.getMeasuredWidth() / 2 + textIconGap / 2;
        } else {
            textLeft = (buttonRightEdge - mMenuButton.calculateMeasuredWidthAuto() / 2) - mMenuText.getMeasuredWidth() / 2 + mImageToggle.getMeasuredWidth() / 2 + textIconGap / 2;
        }
        mMenuText.layout(textLeft, textTop, textLeft + mMenuText.getMeasuredWidth(),
                textTop + mMenuText.getMeasuredHeight());
    }

    private void alignMenuIcon() {
        int iconLeft = Math.round(mMenuText.getX()) - (mImageToggle.getMeasuredWidth() + getResources().getDimensionPixelOffset(R.dimen.extended_button_gap_between_icon_text));
        int iconTop = Math.round(mMenuButton.getY()) + mMenuButton.getMeasuredHeight() / 2 - mImageToggle.getMeasuredHeight() / 2;
        mImageToggle.layout(iconLeft, iconTop, iconLeft + mImageToggle.getMeasuredWidth(),
                iconTop + mImageToggle.getMeasuredHeight());
    }

    private void addLabel(ExtendedFloatingActionButton fab) {
        String text = fab.getLabelText();

        if (TextUtils.isEmpty(text)) return;

        final ExtendedLabel label = new ExtendedLabel(mLabelsContext);
        label.setClickable(true);
        label.setFab(fab);
        if (mLabelsStyle > 0) {
            label.setTextAppearance(getContext(), mLabelsStyle);
            label.setUsingStyle(true);
        } else {
            if (mLabelsEllipsize > 0) {
                setLabelEllipsize(label);
            }
            label.setMaxLines(mLabelsMaxLines);
            label.setTypeface(null, Typeface.BOLD);
            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLabelsTextSize);
            label.setTextColor(mLabelsNormalTextColor);

            int left = mLabelsPaddingLeft;
            int top = mLabelsPaddingTop;
            if (mLabelsShowShadow) {
                left += fab.getShadowRadius() + Math.abs(fab.getShadowXOffset());
                top += fab.getShadowRadius() + Math.abs(fab.getShadowYOffset());
            }

            label.setPadding(
                    left,
                    top,
                    mLabelsPaddingLeft,
                    mLabelsPaddingTop
            );

            if (mLabelsMaxLines < 0 || mLabelsSingleLine) {
                label.setSingleLine(mLabelsSingleLine);
            }
        }

        if (mCustomTypefaceFromFont != null) {
            label.setTypeface(mCustomTypefaceFromFont);
        }
        label.setText(text);
        fab.updateBackground();
        // label.setOnClickListener(fab.getOnClickListener());
        addView(label);
        fab.setTag(R.id.fab_label, label);
    }

    public void setFloatingActionMenuTypeface(int typefaceStyle) {
        if (mMenuText != null) {
            mMenuText.setTypeface(null, typefaceStyle);
        }

        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child instanceof ExtendedFloatingActionButton) {
                final ExtendedFloatingActionButton fab = (ExtendedFloatingActionButton) child;
                ExtendedLabel label = (ExtendedLabel) fab.getTag(R.id.fab_label);
                if (label != null && label.isHandleVisibilityChanges()) {
                    label.setTypeface(null, typefaceStyle);
                }
            }
        }
    }

    private void setLabelEllipsize(ExtendedLabel label) {
        switch (mLabelsEllipsize) {
            case 1:
                label.setEllipsize(TextUtils.TruncateAt.START);
                break;
            case 2:
                label.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                break;
            case 3:
                label.setEllipsize(TextUtils.TruncateAt.END);
                break;
            case 4:
                label.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                break;
        }
    }

    @Override
    public MarginLayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected MarginLayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected MarginLayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT,
                MarginLayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    private void hideMenuButtonWithImage(boolean animate) {
        if (!isMenuButtonHidden()) {
            mMenuButton.hide(animate);
            if (animate) {
                mImageToggle.startAnimation(mImageToggleHideAnimation);
            }
            mIsMenuButtonAnimationRunning = false;
        }
    }

    private void showMenuButtonWithImage(boolean animate) {
        if (isMenuButtonHidden()) {
            mMenuButton.show(animate);
            if (animate) {
                mImageToggle.startAnimation(mImageToggleShowAnimation);
            }
            mImageToggle.setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsSetClosedOnTouchOutside) {
            boolean handled = false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handled = isOpened();
                    break;
                case MotionEvent.ACTION_UP:
                    close(mIsAnimated);
                    handled = true;
            }
            return handled;
        }
        return super.onTouchEvent(event);
    }

    public boolean isOpened() {
        return mMenuOpened;
    }

    /* ===== API methods ===== */

    public void toggle(boolean animate) {
        if (isOpened()) {
            close(animate);
        } else {
            open(animate);
        }
    }

    public void open(final boolean animate) {
        if (!isOpened()) {
            if (isBackgroundEnabled()) {
                mShowBackgroundAnimator.start();
            }

            if (mIconAnimated) {
                if (mIconToggleSet != null) {
                    mIconToggleSet.start();
                } else {
                    mCloseAnimatorSet.cancel();
                    mOpenAnimatorSet.start();
                }
            }

            int delay = 0;
            int counter = 0;
            mIsMenuOpening = true;
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (child instanceof ExtendedFloatingActionButton && child.getVisibility() != GONE) {
                    counter++;

                    final ExtendedFloatingActionButton fab = (ExtendedFloatingActionButton) child;
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isOpened()) return;

                            if (fab != mMenuButton) {
                                fab.show(animate);
                            }

                            ExtendedLabel label = (ExtendedLabel) fab.getTag(R.id.fab_label);
                            if (label != null && label.isHandleVisibilityChanges()) {
                                if (fab != mMenuButton) {
                                    label.show();
                                }
                            }
                        }
                    }, delay);
                    delay += mAnimationDelayPerItem;
                }
            }

            if (!animate) {
                mMenuText.setText(extendedButtonTextExpanded);
            }

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMenuOpened = true;

                    if (mToggleListener != null) {
                        mToggleListener.onMenuToggle(true);
                    }
                }
            }, ++counter * mAnimationDelayPerItem);
        }
    }

    public void close(final boolean animate) {
        if (isOpened()) {
            if (isBackgroundEnabled()) {
                mHideBackgroundAnimator.start();
            }

            if (mIconAnimated) {
                if (mIconToggleSet != null) {
                    mIconToggleSet.start();
                } else {
                    mCloseAnimatorSet.start();
                    mOpenAnimatorSet.cancel();
                }
            }

            int delay = 0;
            int counter = 0;
            mIsMenuOpening = false;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof ExtendedFloatingActionButton && child.getVisibility() != GONE) {
                    counter++;
                    final ExtendedFloatingActionButton fab = (ExtendedFloatingActionButton) child;
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isOpened()) return;
                            if (fab != mMenuButton) {
                                fab.hide(animate);
                            }
                            ExtendedLabel label = (ExtendedLabel) fab.getTag(R.id.fab_label);
                            if (label != null && label.isHandleVisibilityChanges()) {
                                label.hide();
                            }
                        }
                    }, delay);
                    delay += mAnimationDelayPerItem;
                }
            }
            if (!animate) {
                mMenuText.setText(extendedButtonTextExpanded);
            }

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMenuOpened = false;

                    if (mToggleListener != null) {
                        mToggleListener.onMenuToggle(false);
                    }
                }
            }, ++counter * mAnimationDelayPerItem);
        }
    }

    public void setAnimationInProgressListener() {
        setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationInProgress = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationInProgress = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * Returns true if an animation on this layout has not ended yet.
     */
    public boolean isAnimating() {
        return animationInProgress;
    }

    public boolean isAnimated() {
        return mIsAnimated;
    }

    /**
     * Sets whether open and close actions should be animated
     *
     * @param animated if <b>false</b> - menu items will appear/disappear instantly without any animation
     */
    public void setAnimated(boolean animated) {
        mIsAnimated = animated;
        mOpenAnimatorSet.setDuration(animated ? ANIMATION_DURATION : 0);
        mCloseAnimatorSet.setDuration(animated ? ANIMATION_DURATION : 0);
    }

    public int getAnimationDelayPerItem() {
        return mAnimationDelayPerItem;
    }

    public void setAnimationDelayPerItem(int animationDelayPerItem) {
        mAnimationDelayPerItem = animationDelayPerItem;
    }

    public void setOnMenuToggleListener(FloatingMenuToggleListener listener) {
        mToggleListener = listener;
    }

    public boolean isIconAnimated() {
        return mIconAnimated;
    }

    public void setIconAnimated(boolean animated) {
        mIconAnimated = animated;
    }

    public ExtendedFloatingActionButton getMenuButton() {
        return mMenuButton;
    }

    public ImageView getMenuIconView() {
        return mImageToggle;
    }

    public AnimatorSet getIconToggleAnimatorSet() {
        return mIconToggleSet;
    }

    public void setIconToggleAnimatorSet(AnimatorSet toggleAnimatorSet) {
        mIconToggleSet = toggleAnimatorSet;
    }

    public void setMenuButtonShowAnimation(Animation showAnimation) {
        mMenuButtonShowAnimation = showAnimation;
        mMenuButton.setShowAnimation(showAnimation);
    }

    public void setMenuButtonHideAnimation(Animation hideAnimation) {
        mMenuButtonHideAnimation = hideAnimation;
        mMenuButton.setHideAnimation(hideAnimation);
    }

    public boolean isMenuHidden() {
        return getVisibility() == INVISIBLE;
    }

    public boolean isMenuButtonHidden() {
        return mMenuButton.isHidden();
    }

    public void setCorrectPivot() {
        int buttonLeftPadding = ((Util.getScreenWidth(getContext()) - mMenuButton.calculateMeasuredWidth()) - getPaddingRight()); // We suppose that left padding is equal to right padding.
        int pivotX = Math.round(buttonLeftPadding + mMenuButton.calculateMeasuredWidth() / 2);
        int pivotY = Math.round(mMenuButton.getY() + mMenuButton.calculateMeasuredHeight() / 2);
        setPivotX(pivotX);
        setPivotY(pivotY);
        mMenuButton.setPivotX(pivotX);
        mMenuButton.setPivotY(pivotY);
    }

    /**
     * Makes the whole {@link #ExtendedFloatingActionMenu} to appear and sets its visibility to {@link #VISIBLE}
     *
     * @param animate if true - plays "show animation"
     */
    public void showMenu(boolean animate) {
        setCorrectPivot();
        if (isMenuHidden()) {
            if (animate) {
                startAnimation(mMenuButtonShowAnimation);
            }
            setVisibility(VISIBLE);
        }
    }

    /**
     * Makes the {@link #ExtendedFloatingActionMenu} to disappear and sets its visibility to {@link #INVISIBLE}
     *
     * @param animate if true - plays "hide animation"
     */
    public void hideMenu(final boolean animate) {
        setCorrectPivot();
        if (!isMenuHidden() && !mIsMenuButtonAnimationRunning) {
            mIsMenuButtonAnimationRunning = true;
            if (isOpened()) {
                close(animate);
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (animate) {
                            startAnimation(mMenuButtonHideAnimation);
                        }
                        setVisibility(INVISIBLE);
                        mIsMenuButtonAnimationRunning = false;
                    }
                }, mAnimationDelayPerItem * mButtonsCount);
            } else {
                if (animate) {
                    startAnimation(mMenuButtonHideAnimation);
                }
                setVisibility(INVISIBLE);
                mIsMenuButtonAnimationRunning = false;
            }
        }
    }

    public void toggleMenu(boolean animate) {
        if (isMenuHidden()) {
            showMenu(animate);
        } else {
            hideMenu(animate);
        }
    }

    /**
     * Makes the {@link ExtendedFloatingActionButton} to appear inside the {@link #ExtendedFloatingActionMenu} and
     * sets its visibility to {@link #VISIBLE}
     *
     * @param animate if true - plays "show animation"
     */
    public void showMenuButton(boolean animate) {
        setCorrectPivot();
        if (isMenuButtonHidden()) {
            showMenuButtonWithImage(animate);
        }
    }

    /**
     * Makes the {@link ExtendedFloatingActionButton} to disappear inside the {@link #ExtendedFloatingActionMenu} and
     * sets its visibility to {@link #INVISIBLE}
     *
     * @param animate if true - plays "hide animation"
     */
    public void hideMenuButton(final boolean animate) {
        setCorrectPivot();
        if (!isMenuButtonHidden() && !mIsMenuButtonAnimationRunning) {
            mIsMenuButtonAnimationRunning = true;
            if (isOpened()) {
                close(animate);
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideMenuButtonWithImage(animate);
                    }
                }, mAnimationDelayPerItem * mButtonsCount);
            } else {
                hideMenuButtonWithImage(animate);
            }
        }
    }

    public void toggleMenuButton(boolean animate) {
        if (isMenuButtonHidden()) {
            showMenuButton(animate);
        } else {
            hideMenuButton(animate);
        }
    }

    public void setClosedOnTouchOutside(boolean close) {
        mIsSetClosedOnTouchOutside = close;
    }

    public void setMenuButtonColorNormalResId(int colorResId) {
        mMenuColorNormal = getResources().getColor(colorResId);
        mMenuButton.setColorNormalResId(colorResId);
    }

    public int getMenuButtonColorNormal() {
        return mMenuColorNormal;
    }

    public void setMenuButtonColorNormal(int color) {
        mMenuColorNormal = color;
        mMenuButton.setColorNormal(color);
    }

    public void setMenuButtonColorPressedResId(int colorResId) {
        mMenuColorPressed = getResources().getColor(colorResId);
        mMenuButton.setColorPressedResId(colorResId);
    }

    @Override
    public float getMenuX() {
        return getPivotX();
    }

    @Override
    public float getMenuY() {
        return getPivotY();
    }

    public int getMenuButtonColorPressed() {
        return mMenuColorPressed;
    }

    public void setMenuButtonColorPressed(int color) {
        mMenuColorPressed = color;
        mMenuButton.setColorPressed(color);
    }

    public void setMenuButtonColorRippleResId(int colorResId) {
        mMenuColorRipple = getResources().getColor(colorResId);
        mMenuButton.setColorRippleResId(colorResId);
    }

    public int getMenuButtonColorRipple() {
        return mMenuColorRipple;
    }

    public void setMenuButtonColorRipple(int color) {
        mMenuColorRipple = color;
        mMenuButton.setColorRipple(color);
    }

    public void addMenuButton(ExtendedFloatingActionButton fab) {
        addView(fab, mButtonsCount - 2);
        mButtonsCount++;
        addLabel(fab);
    }

    public void removeMenuButton(ExtendedFloatingActionButton fab) {
        removeView(fab.getLabelView());
        removeView(fab);
        mButtonsCount--;
    }
    @Override
    public void setExtendedButtonTextCollapsed(String menuText) {
        extendedButtonTextCollapsed = menuText;
    }

    @Override
    public void setExtendedButtonTextExpanded(String menuText) {
        extendedButtonTextExpanded = menuText;
    }

    public void setmExtendedButtonTextColor(int menuTextColor) {
        mExtendedButtonTextColor = menuTextColor;
    }

    public void setmExtendedButtonTextSize(int menuTextSize) {
        mExtendedButtonTextSize = menuTextSize;
    }

    public void addMenuButton(ExtendedFloatingActionButton fab, int index) {
        int size = mButtonsCount - 2;
        if (index < 0) {
            index = 0;
        } else if (index > size) {
            index = size;
        }

        addView(fab, index);
        mButtonsCount++;
        addLabel(fab);
    }

    public void removeAllMenuButtons() {
        close(true);

        List<ExtendedFloatingActionButton> viewsToRemove = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v != mMenuButton && v != mImageToggle && v instanceof ExtendedFloatingActionButton) {
                viewsToRemove.add((ExtendedFloatingActionButton) v);
            }
        }
        for (ExtendedFloatingActionButton v : viewsToRemove) {
            removeMenuButton(v);
        }
    }

    public String getMenuButtonLabelText() {
        return mMenuLabelText;
    }

    public void setMenuButtonLabelText(String text) {
        mMenuButton.setLabelText(text);
    }

    public void setOnMenuButtonClickListener(OnClickListener clickListener) {
        mMenuButton.setOnClickListener(clickListener);
    }

    public void setOnMenuButtonLongClickListener(OnLongClickListener longClickListener) {
        mMenuButton.setOnLongClickListener(longClickListener);
    }
}