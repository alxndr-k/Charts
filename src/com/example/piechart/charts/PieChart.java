package com.example.piechart.charts;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import com.example.piechart.R;

import java.util.ArrayList;
import java.util.Random;

public class PieChart extends View {

    private static final int ANIMATION_DURATION_APPEARANCE = 1000;
    private static final int INNER_PADDING = 60;
    private final String NO_DATA_MESSAGE = getResources().getString(R.string.chart_no_data_to_build);

    private boolean mDrawValues = true;

    private ArrayList<Slice> mValues = new ArrayList<Slice>();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mPieRect;
    private ObjectAnimator mAppearanceAnimator = ObjectAnimator.ofFloat(this, "appearance", 1);

    private float mAppearance; // indicate appearance progress

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAppearanceAnimator.setDuration(ANIMATION_DURATION_APPEARANCE);
        mPaint.setTextAlign(Paint.Align.CENTER);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PieChart, 0, 0);
        try {
            mDrawValues = a.getBoolean(R.styleable.PieChart_showValues, false);
            int textSize = a.getDimensionPixelSize(R.styleable.PieChart_textSize, getResources().getDimensionPixelOffset(R.dimen.font_size_middle));
            mPaint.setTextSize(textSize);
        } finally {
            a.recycle();
        }

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
        int diameter = Math.min(width, height) - (mDrawValues ? INNER_PADDING : 0);

        int left = getPaddingLeft() + (width - diameter) / 2;
        int top = getPaddingTop() + (height - diameter) / 2;

        mPieRect = new RectF(left, top, left + diameter, top + diameter);
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
        int startAngle = 0;
        for (Slice slice : mValues) {
            mPaint.setColor(slice.color);
            float sweep = slice.value * mAppearance;
            float start = startAngle + ((slice.value - sweep) / 2);
            canvas.drawArc(mPieRect, start, sweep, true, mPaint);

            if (mDrawValues) drawValues(canvas, startAngle, slice.value);

            startAngle += slice.value;
        }
    }

    private void drawValues(Canvas canvas, int startAngle, int value) {
        mPaint.setColor(Color.WHITE);
        double angle = Math.toRadians(startAngle + value / 2);
        float radius = mPieRect.width() / 2;
        float x = (float) (radius * Math.cos(angle));
        float y = (float) (radius * Math.sin(angle));
        float absoluteX = mPieRect.left + radius + x;
        float absoluteY = mPieRect.top + radius + y;

        canvas.drawLine(absoluteX - x * 0.05f, absoluteY - y * 0.05f, absoluteX + x * 0.05f, absoluteY + y * 0.05f, mPaint);

        String text = String.format("%.1f%%", value * 100.0 / 360 * mAppearance);
        Rect rect = new Rect();
        mPaint.getTextBounds(text, 0, text.length(), rect);
        canvas.drawText(text, absoluteX + x * 0.1f, absoluteY + y * 0.1f + rect.height() / 2, mPaint); // + rect.height() / 2 - vertical align
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
