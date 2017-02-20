package org.cn.iot.device.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.cn.iot.device.R;

/**
 * Created by chenning on 2016/8/25.
 */
public class DeviceListDivider extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    public DeviceListDivider(Context ctx) {
        this(ctx, R.drawable.divider_device_list);
    }

    public DeviceListDivider(Context ctx, int id) {
        mDivider = ctx.getResources().getDrawable(id);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        RecyclerView.LayoutManager lm = parent.getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) lm).getOrientation() == LinearLayoutManager.VERTICAL) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicHeight(), 0);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        RecyclerView.LayoutManager lm = parent.getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) lm).getOrientation() == LinearLayoutManager.VERTICAL) {
                drawLinearVertical(canvas, parent);
            } else {
            }
        }
    }

    private void drawLinearVertical(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int topA = parent.getPaddingTop();
        final int bottomB = parent.getHeight() - parent.getPaddingBottom();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = Math.max(topA, child.getBottom() + params.bottomMargin);
            final int bottom = Math.min(bottomB, top + mDivider.getIntrinsicHeight());
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
    }

    private void drawLinearHorizontal(Canvas canvas, RecyclerView recyclerView) {
    }

}
