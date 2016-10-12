package com.github.clans.fab;

import android.content.Context;
import android.content.res.Configuration;
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
    private float mButtonPadding;

    ExtendedButtonShadowShape(Context ctx) {
        super();
        mContext = ctx;
        mButtonPadding = ctx.getResources().getDimensionPixelSize(R.dimen.extended_button_shadow_padding);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        float mExtraLeftShadow = -5f;
        float mExtraTopShadow = -0f;
        float mRadius = 70f;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            canvas.drawRoundRect(new RectF(Util.dpToPx(mContext, mExtraLeftShadow), Util.dpToPx(mContext, mExtraTopShadow), Util.getScreenWidth(mContext) - mButtonPadding, Util.dpToPx(mContext, 56f)), mRadius, mRadius, paint);
        } else {
            canvas.drawRoundRect(new RectF(Util.dpToPx(mContext, mExtraLeftShadow), Util.dpToPx(mContext, mExtraTopShadow), Util.getExtendedButtonLandscapeWidth(mContext), Util.dpToPx(mContext, 56f)), mRadius, mRadius, paint);
        }
    }
}
