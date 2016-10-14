package com.github.clans.fab;

import android.animation.AnimatorSet;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.ImageView;

/**
 * WRITE DESCRIPTION PLS
 *
 * @author Josef Hruška (josef@stepuplabs.io)
 */

public interface FloatingMenu {
    ViewGroup.MarginLayoutParams generateLayoutParams(AttributeSet attrs);

    boolean onTouchEvent(MotionEvent event);

    boolean isOpened();

    void toggle(boolean animate);

    void open(boolean animate);

    void close(boolean animate);

    void setIconAnimationInterpolator(Interpolator interpolator);

    void setIconAnimationOpenInterpolator(Interpolator openInterpolator);

    void setIconAnimationCloseInterpolator(Interpolator closeInterpolator);

    boolean isAnimated();

    void setAnimated(boolean animated);

    int getAnimationDelayPerItem();

    void setAnimationDelayPerItem(int animationDelayPerItem);

    void setOnMenuToggleListener(FloatingActionMenu.OnMenuToggleListener listener);

    boolean isIconAnimated();

    void setIconAnimated(boolean animated);

    ImageView getMenuIconView();

    AnimatorSet getIconToggleAnimatorSet();

    void setIconToggleAnimatorSet(AnimatorSet toggleAnimatorSet);

    void setMenuButtonShowAnimation(Animation showAnimation);

    void setMenuButtonHideAnimation(Animation hideAnimation);

    void setAnimationInProgressListener();

    boolean isAnimating();

    boolean isMenuHidden();

    boolean isMenuButtonHidden();

    void showMenu(boolean animate);

    void hideMenu(boolean animate);

    void toggleMenu(boolean animate);

    void showMenuButton(boolean animate);

    void hideMenuButton(boolean animate);

    void toggleMenuButton(boolean animate);

    void setClosedOnTouchOutside(boolean close);

    void setMenuButtonColorNormalResId(int colorResId);

    int getMenuButtonColorNormal();

    void setMenuButtonColorNormal(int color);

    void setMenuButtonColorPressedResId(int colorResId);

    int getMenuButtonColorPressed();

    void setMenuButtonColorPressed(int color);

    void setMenuButtonColorRippleResId(int colorResId);

    int getMenuButtonColorRipple();

    void setMenuButtonColorRipple(int color);

    void addMenuButton(FloatingActionButton fab);

    void removeMenuButton(FloatingActionButton fab);

    void addMenuButton(FloatingActionButton fab, int index);

    void setCorrectPivot();

    void removeAllMenuButtons();

    String getMenuButtonLabelText();

    void setMenuButtonLabelText(String text);

    void setOnMenuButtonClickListener(View.OnClickListener clickListener);

    void setOnMenuButtonLongClickListener(View.OnLongClickListener longClickListener);
}
