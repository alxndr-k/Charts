package com.example.piechart.charts;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class PieChart extends View {

    private ArrayList<Slice> mValues = new ArrayList<Slice>();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mRect;
    private ObjectAnimator mAppearanceAnimator = ObjectAnimator.ofFloat(this, PieChart.APPEARANCE, 1);

    private float mAppearance; // indicate appearance progress

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void refresh() {
        mAppearanceAnimator.start();
    }

    public void apply(ArrayList<Integer> values) {
        mValues.clear();

        int total = 0;
        for (int value : values) total += value;

        int alignment = 360;
        for (int i = 0; i < values.size() - 1; ++i) {
            int value = 360 * values.get(i) / total;
            alignment -= value;
            mValues.add(new Slice(value));
        }

        mValues.add(new Slice(alignment));

        refresh();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int ww = w - getPaddingLeft();
        int hh = h - getPaddingTop();
        int diameter = Math.min(ww, hh);

        mRect = new RectF(getPaddingLeft(), getPaddingTop(), diameter, diameter);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float startAngle = 0;
        for (Slice slice : mValues) {
            mPaint.setColor(slice.color);
            float sweep = slice.value * mAppearance;
            float start = startAngle + ((slice.value - sweep) / 2);
            canvas.drawArc(mRect, start, sweep, true, mPaint);
            startAngle += slice.value;
        }
    }

    private void setAppearance(float appearance) {
        mAppearance = appearance;
        invalidate();
    }

    public static final Property<View, Float> APPEARANCE = new Property<View, Float>(Float.class, "appearance") {
        @Override
        public void set(View chart, Float value) {
            ((PieChart) chart).setAppearance(value);
        }

        @Override
        public Float get(View object) {
            return 0.0f; // always start from 0
        }
    };

    private static class Slice {

        private static final int COLOR_OFFSET = 64;
        private static final int COLOR_MAX = 255;
        private static final int COLOR_RANDOMIZE = COLOR_MAX - COLOR_OFFSET;

        private static final int BYTE_SHIFT = 8;

        private static final Random sRand = new Random();

        private int value;
        private int color;

        public Slice(int value) {
            this.value = value;
            this.color = getColor();
        }

        private int getColor() {
            int t = getRandomColor() << (BYTE_SHIFT * 3);
            int r = getRandomColor() << (BYTE_SHIFT * 2);
            int g = getRandomColor() << BYTE_SHIFT;
            int b = getRandomColor();

            return t | r | g | b;
        }

        private int getRandomColor() {
            return COLOR_OFFSET + sRand.nextInt(COLOR_RANDOMIZE);
        }
    }
}
