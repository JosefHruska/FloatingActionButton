package com.github.clans.fab;

import android.animation.AnimatorSet;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * WRITE DESCRIPTION PLS
 *
 * @author Josef Hruška (josef@stepuplabs.io)
 */

public interface FloatingMenu {
    boolean isOpened();

    ImageView getMenuIconView();

    AnimatorSet getIconToggleAnimatorSet();

    float getMenuX();

    float getMenuY();

    void setMenuButtonShowAnimation(Animation showAnimation);

    void setMenuButtonHideAnimation(Animation hideAnimation);

    void setCorrectPivot();
}
