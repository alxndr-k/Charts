package com.example.piechart.charts;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import com.example.piechart.R;

import java.util.ArrayList;
import java.util.Random;

public class PieChart extends View {

    private static final int TEXT_SIZE = 60;
    private static final int ANIMATION_DURATION_APPEARANCE = 1000;
    private final String NO_DATA_MESSAGE = getResources().getString(R.string.chart_no_data_to_build);

    private ArrayList<Slice> mValues = new ArrayList<Slice>();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mRect;
    private ObjectAnimator mAppearanceAnimator = ObjectAnimator.ofFloat(this, "appearance", 1);

    private float mAppearance; // indicate appearance progress

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAppearanceAnimator.setDuration(ANIMATION_DURATION_APPEARANCE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(TEXT_SIZE);
    }

    public void refresh() {
        mAppearanceAnimator.start();
    }

    public void apply(ArrayList<Integer> values) {
        mValues.clear();

        if (values != null && values.size() > 0) {
            int total = 0;
            for (int value : values) total += value;

            int alignment = 360;
            for (int i = 0; i < values.size() - 1; ++i) {
                int value = 360 * values.get(i) / total;
                alignment -= value;
                mValues.add(new Slice(value));
            }

            mValues.add(new Slice(alignment));
        }

        refresh();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int width = w - getPaddingLeft() - getPaddingRight();
        int height = h - getPaddingTop() - getPaddingBottom();
        int diameter = Math.min(width, height);

        int left = getPaddingLeft() + (width - diameter) / 2;
        int top = getPaddingTop() + (height - diameter) / 2;

        mRect = new RectF(left, top, left + diameter, top + diameter);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mValues.size() > 0) {
            drawChart(canvas);
        } else {
            drawNoData(canvas);
        }
    }

    private void drawChart(Canvas canvas) {
        float startAngle = 0;
        for (Slice slice : mValues) {
            mPaint.setColor(slice.color);
            float sweep = slice.value * mAppearance;
            float start = startAngle + ((slice.value - sweep) / 2);
            canvas.drawArc(mRect, start, sweep, true, mPaint);
            startAngle += slice.value;
        }
    }

    private void drawNoData(Canvas canvas) {
        mPaint.setColor(Color.WHITE);
        canvas.drawText(NO_DATA_MESSAGE, getWidth() / 2, getHeight() / 2, mPaint);
    }

    @SuppressWarnings("unused")
    private void setAppearance(float appearance) {
        mAppearance = appearance;
        invalidate();
    }

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
