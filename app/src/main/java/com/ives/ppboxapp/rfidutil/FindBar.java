package com.ives.ppboxapp.rfidutil;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.ives.ppboxapp.R;


/**
 * TODO: document your custom view class.
 */
public class FindBar extends View {

    public FindBar(Context context) {
        super(context);
        init(null, 0);
    }

    public FindBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public FindBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public float percent = 0f;
    public int color = 0;
    private final Paint paint = new Paint();
    private final ValueAnimator animator = new ValueAnimator();

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.FindBar, defStyle, 0);

        percent = a.getFloat(R.styleable.FindBar_percent, 0f);
        color = a.getColor(R.styleable.FindBar_color, Color.RED);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(200);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (percent == 0f) {
            return;
        }
        final float h = getHeight();
        final float hh = percent * getHeight();
        paint.setColor(color);
        final float w = getWidth();
        if (canvas != null) {
            canvas.drawRect(0f, h - hh, w, h, paint);
        }
    }

    private static final String TAG = "FindBar";

    public void set(float percent, int color) {
        animator.cancel();
        animator.setValues(
                PropertyValuesHolder.ofFloat("percent", this.percent, percent),
                PropertyValuesHolder.ofInt("color", this.color, color)
        );
        final FindBar self = this;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                self.percent = (float) animation.getAnimatedValue("percent");
                self.color = (int) animation.getAnimatedValue("color");
                invalidate();
            }
        });
        animator.start();
    }

    public void set(float percent) {
        set(percent, color);
    }
}
