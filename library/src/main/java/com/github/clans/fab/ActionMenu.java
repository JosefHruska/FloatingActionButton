package com.github.clans.fab;

import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;

/**
 * WRITE DESCRIPTION PLS
 *
 * @author Josef Hruška (josef@stepuplabs.io)
 */

public abstract class ActionMenu extends ViewGroup {
    protected AnimatorSet mIconToggleSet;
    protected FloatingActionButton mMenuButton;
    protected boolean mMenuOpened;
    protected Animation mMenuButtonShowAnimation;
    protected Animation mMenuButtonHideAnimation;

    public ActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public abstract float getMenuX();

    public abstract float getMenuY();

    public abstract void setMenuButtonShowAnimation(Animation showAnimation);

    public abstract void setMenuButtonHideAnimation(Animation hideAnimation);

    public abstract boolean isOpened();

    public abstract void setCorrectPivot();
}
