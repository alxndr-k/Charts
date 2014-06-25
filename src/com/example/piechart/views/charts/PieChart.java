package com.example.piechart.views.charts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.piechart.R;
import com.example.piechart.views.adapters.Slice;

import java.util.ArrayList;

public class PieChart extends View {

    public interface OnSliceSelectedListener { boolean onSelected(int index); }

    private static final int ANIMATION_DURATION_APPEARANCE = 1000;
    private static final int INNER_PADDING = 100;
    private final String NO_DATA_MESSAGE = getResources().getString(R.string.chart_no_data_to_build);

    private boolean mDrawValues = true;
    private int mTextSize;
    private int mEmptyTextSize;

    private ArrayList<Slice> mValues = new ArrayList<Slice>();

    private OnSliceSelectedListener mOnSliceSelectedListener;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mPieRect;
    private ValueAnimator mAppearanceAnimator = ValueAnimator.ofFloat(0, 1);

    private float mAppearance; // indicate appearance progress

    public PieChart(Context context) {
        this(context, null);
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAppearanceAnimator.setDuration(ANIMATION_DURATION_APPEARANCE);
        mAppearanceAnimator.addUpdateListener(mAnimationListener);
        mPaint.setTextAlign(Paint.Align.CENTER);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PieChart, 0, 0);
        try {
            mDrawValues = a.getBoolean(R.styleable.PieChart_showValues, true);
            mTextSize = a.getDimensionPixelSize(R.styleable.PieChart_textSize, getResources().getDimensionPixelOffset(R.dimen.font_size_small));
            mEmptyTextSize = a.getDimensionPixelSize(R.styleable.PieChart_emptyTextSize, getResources().getDimensionPixelOffset(R.dimen.font_size_middle));
        } finally {
            a.recycle();
        }
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchedSlice = getSlice(event.getX(), event.getY());
        boolean handle = false;
        if (touchedSlice >= 0 && mOnSliceSelectedListener != null) {
            handle = mOnSliceSelectedListener.onSelected(touchedSlice);
        }
        return handle || super.onTouchEvent(event);
    }

    public void refresh(boolean animate) {
        if (animate) {
            mAppearanceAnimator.start();
        } else {
            mAppearance = 1;
            invalidate();
        }
    }

    public void apply(ArrayList<Slice> values, boolean animate) {
        mValues.clear();

        if (values != null && values.size() > 0) {
            int total = 0;
            for (Slice slice : values) total += slice.value;

            float alignment = 360.0f;
            for (int i = 0; i < values.size() - 1; ++i) {
                float value = 360.0f * values.get(i).value / total;
                alignment -= value;
                mValues.add(new Slice(value, values.get(i).color));
            }

            int last = values.size() - 1;
            mValues.add(new Slice(alignment, values.get(last).color));
        }

        refresh(animate);
    }

    public void setOnSliceSelectedListener(OnSliceSelectedListener listener) {
        mOnSliceSelectedListener = listener;
    }

    private int getSlice(float x, float y) {
        float cx = mPieRect.centerX();
        float cy = mPieRect.centerY();
        double radius = Math.hypot((x - cx), (y - cy));

        // check if the point is inside chart
        if (!(radius * 2 <= mPieRect.height())) return -1;

        // get touch angle (related to the chart center)
        double angle = Math.toDegrees(Math.atan2(y - cy, x - cx));
        angle = (360 + angle) % 360;

        int sum = 0;
        for (int i = 0; i < mValues.size(); i++) {
            sum += mValues.get(i).value;
            if (sum > angle) return i;
        }

        return -1;
    }

    private void drawChart(Canvas canvas) {
        float startAngle = 0;
        for (Slice slice : mValues) {
            mPaint.setColor(slice.color);
            float sweep = slice.value * mAppearance;
            float start = startAngle + ((slice.value - sweep) / 2);
            canvas.drawArc(mPieRect, start, sweep, true, mPaint);

            if (mDrawValues) drawValues(canvas, startAngle, slice.value);

            startAngle += slice.value;
        }
    }

    private void drawValues(Canvas canvas, float startAngle, float value) {
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(mTextSize);
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
        mPaint.setColor(Color.GRAY);
        mPaint.setTextSize(mEmptyTextSize);
        canvas.drawText(NO_DATA_MESSAGE, getWidth() / 2, (canvas.getHeight() - mPaint.descent() - mPaint.ascent()) / 2, mPaint);
    }

    private ValueAnimator.AnimatorUpdateListener mAnimationListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mAppearance = (Float) animation.getAnimatedValue();
            invalidate();
        }
    };
}
