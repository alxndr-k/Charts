package com.example.piechart.views;

import android.animation.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import com.example.piechart.Constants;
import com.example.piechart.SlicesAdapter;

import java.util.ArrayList;
import java.util.List;

public class AnimatedListView extends ListView {

    private SparseArray<Rect> mTops = new SparseArray<Rect>(Constants.MAX_VALUES_COUNT);
    private SparseArray<BitmapDrawable> mSnaps = new SparseArray<BitmapDrawable>(Constants.MAX_VALUES_COUNT);
    private List<BitmapDrawable> mCellBitmapDrawables = new ArrayList<BitmapDrawable>(Constants.MAX_VALUES_COUNT);


    public AnimatedListView(Context context) {
        super(context);
    }

    public void addWithAnimation() {
        saveViews(null, true);
        ((SlicesAdapter) getAdapter()).add();
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnPreDrawListener(new AddOnPreDrawListener());
    }

    public void removeWithAnimation(final View viewToRemove) {
        saveViews(viewToRemove, false);
        int position = getPositionForView(viewToRemove);
        ((SlicesAdapter) getAdapter()).remove(position);
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnPreDrawListener(new RemoveOnPreDrawListener());
    }

    private void saveViews(View ignoreView, boolean saveSnap) {
        int firstVisiblePosition = getFirstVisiblePosition();
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            if (child != ignoreView) {
                int position = firstVisiblePosition + i;
                int itemId = (int) getAdapter().getItemId(position);
                mTops.put(itemId, new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom()));
                if (saveSnap) mSnaps.put(itemId, getDrawableFromView(child));
            }
        }
    }

    private BitmapDrawable getDrawableFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return new BitmapDrawable(getResources(), bitmap);
    }

    private class AddOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

        @Override
        public boolean onPreDraw() {
            getViewTreeObserver().removeOnPreDrawListener(this);

            ArrayList<Animator> animators = new ArrayList<Animator>();
            View newChild = getChildAt(0);
            PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f);
            PropertyValuesHolder pvhTranslateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, newChild.getWidth() * 0.2f, 0.0f);
            animators.add(ObjectAnimator.ofPropertyValuesHolder(newChild, pvhAlpha, pvhTranslateX));

            int firstVisiblePosition = getFirstVisiblePosition();
            for (int i = 1; i < getChildCount(); ++i) {
                int id = (int) getAdapter().getItemId(firstVisiblePosition + i);
                Rect oldBounds = mTops.get(id);
                View child = getChildAt(i);
                int newTop = child.getTop();
                int delta;
                if (oldBounds != null) {
                    delta = oldBounds.top - newTop;
                } else {
                    delta = child.getHeight();
                }
                animators.add(ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, delta, 0));
                mTops.delete(id);
                mSnaps.delete(id);
            }

            for (int i = 0; i < mTops.size(); ++i) {
                int key = mTops.keyAt(i);
                Rect startBounds = mTops.get(key);
                Rect endBounds = new Rect(startBounds);
                endBounds.offset(0, startBounds.top - startBounds.bottom);
                BitmapDrawable drawable = mSnaps.get(key);

                ObjectAnimator animator = ObjectAnimator.ofObject(drawable, "bounds", sBoundsEvaluator, startBounds, endBounds);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    private Rect mLastBound = null;
                    private Rect mCurrentBound = new Rect();

                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        Rect bounds = (Rect) valueAnimator.getAnimatedValue();
                        mCurrentBound.set(bounds);
                        if (mLastBound != null) {
                            mCurrentBound.union(mLastBound);
                        }
                        mLastBound = bounds;
                        invalidate(mCurrentBound);
                    }
                });
                mCellBitmapDrawables.add(drawable);
                animators.add(animator);
                mTops.remove(key);
                mSnaps.remove(key);
            }

            setEnabled(false);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(animators);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setEnabled(true);
                    mCellBitmapDrawables.clear();
                    invalidate();
                }
            });
            set.start();

            return true;
        }
    }

    private class RemoveOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

        public boolean onPreDraw() {
            getViewTreeObserver().removeOnPreDrawListener(this);

            int firstVisiblePosition = getFirstVisiblePosition();
            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                int position = firstVisiblePosition + i;
                int itemId = (int) getAdapter().getItemId(position);
                Rect oldBounds = mTops.get(itemId);
                int newTop = child.getTop();
                if (oldBounds != null) {
                    if (oldBounds.top != newTop) {
                        child.setTranslationY(oldBounds.top - newTop);
                        child.animate().translationY(0);
                    }
                } else {
                    int childHeight = child.getHeight() + getDividerHeight();
                    int oldTop = newTop + (i > 0 ? childHeight : -childHeight);
                    child.setTranslationY(oldTop - newTop);
                    child.animate().translationY(0);
                }
            }
            mTops.clear();
            return true;
        }
    }

    static final TypeEvaluator<Rect> sBoundsEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };
}
