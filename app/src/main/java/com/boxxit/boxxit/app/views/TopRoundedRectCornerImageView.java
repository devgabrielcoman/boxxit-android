package com.boxxit.boxxit.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

public class TopRoundedRectCornerImageView extends android.support.v7.widget.AppCompatImageView {

    private float radius = 18.0f;
    private Path path;
    private RectF rect;
    private float[] raddi;

    public TopRoundedRectCornerImageView(Context context) {
        super(context);
        init();
    }

    public TopRoundedRectCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TopRoundedRectCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        path = new Path();
        raddi = new float[]{radius,radius,radius,radius,0,0,0,0};
//        rect = new RectF(0, 0, this.getWidth(), this.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        path.addRoundRect(rect, raddi, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}