package com.github.clans.fab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.shapes.RectShape;

/**
 * Shape for extended action menu.
 *
 * @author Josef Hruška (josef@stepuplabs.io)
 */

public class ExtendedButtonShadowShape extends RectShape {
    private Context mContext;
    private float mRadius = 75f;
    private float mExtraTopShadow = -7f;
    private float mExtraLeftShadow = -5f;
    private float mButtonPadding;

    ExtendedButtonShadowShape(Context ctx) {
        super();
        mContext = ctx;
        mButtonPadding = ctx.getResources().getDimensionPixelSize(R.dimen.extended_button_shadow_padding);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) { //
        canvas.drawRoundRect(new RectF(Util.dpToPx(mContext, mExtraLeftShadow), Util.dpToPx(mContext, mExtraTopShadow), Util.getScreenWidth(mContext) - mButtonPadding, Util.dpToPx(mContext, 50f)), mRadius, mRadius, paint);
    }
}
