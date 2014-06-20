package com.example.piechart.views;

import android.animation.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import com.example.piechart.Constants;
import com.example.piechart.views.adapters.SlicesAdapter;

import java.util.ArrayList;

public class AnimatedListView extends ListView {

    private SparseArray<Rect> mTops = new SparseArray<Rect>(Constants.MAX_VALUES_COUNT);
    private SparseArray<BitmapDrawable> mSnaps = new SparseArray<BitmapDrawable>(Constants.MAX_VALUES_COUNT);

    private Drawable mDrawable; // view's snapshot that not exist during adding or removing, use for animation
    private long mNextToRemoveItem;

    public AnimatedListView(Context context) {
        super(context);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mDrawable != null) {
            mDrawable.draw(canvas);
        }
    }

    public void addWithAnimation() {
        saveViews(null, true);
        ((SlicesAdapter) getAdapter()).add();
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnPreDrawListener(new AddOnPreDrawListener());
    }

    public void removeWithAnimation(final View viewToRemove) {
        mNextToRemoveItem = getPositionForView(viewToRemove);
        mDrawable = getDrawableFromView(viewToRemove);
        mDrawable.setBounds(new Rect(new Rect(viewToRemove.getLeft(), viewToRemove.getTop(), viewToRemove.getRight(), viewToRemove.getBottom())));

        saveViews(viewToRemove, false);
        int position = getPositionForView(viewToRemove);
        ((SlicesAdapter) getAdapter()).remove(position);
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnPreDrawListener(new RemoveOnPreDrawListener());
    }

    private void animateDrawable(Drawable drawable, Rect startBounds, Rect endBounds) {
        mDrawable = drawable;
        ObjectAnimator animator = ObjectAnimator.ofObject(mDrawable, "bounds", sBoundsEvaluator, startBounds, endBounds);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private Rect lastBound = null;
            private Rect currentBound = new Rect();

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Rect bounds = (Rect) valueAnimator.getAnimatedValue();
                currentBound.set(bounds);
                if (lastBound != null) {
                    currentBound.union(lastBound);
                }
                lastBound = bounds;
                invalidate(currentBound);
            }
        });
        animator.start();
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

            int firstVisiblePosition = getFirstVisiblePosition();
            int i = 0;
            ArrayList<Animator> animators = new ArrayList<Animator>();
            if (firstVisiblePosition == 0) {
                i = 1;
                View newChild = getChildAt(0);
                PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0, 1);
                PropertyValuesHolder pvhTranslationY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, newChild.getTranslationY() - newChild.getHeight() / 2, newChild.getTranslationY());
                animators.add(ObjectAnimator.ofPropertyValuesHolder(newChild, pvhScaleY, pvhTranslationY));
            }

            while (i < getChildCount()) {
                int id = (int) getAdapter().getItemId(firstVisiblePosition + i);
                Rect oldBounds = mTops.get(id);
                View child = getChildAt(i);
                int newTop = child.getTop();
                int delta;
                if (oldBounds != null) {
                    delta = oldBounds.top - newTop;
                } else {
                    delta = child.getTop() - child.getHeight();
                }
                animators.add(ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, delta, 0));
                mTops.delete(id);
                mSnaps.delete(id);
                i++;
            }

            if (mTops.size() > 0) {
                int key = mTops.keyAt(0);
                Rect startBounds = mTops.get(key);
                Rect endBounds = new Rect(startBounds);
                endBounds.offset(0, startBounds.bottom - startBounds.top);
                animateDrawable(mSnaps.get(key), startBounds, endBounds);
            }

            mTops.clear();
            mSnaps.clear();
            setEnabled(false);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(animators);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setEnabled(true);
                    mDrawable = null;
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

            Rect startBounds = mDrawable.getBounds();
            Rect endBounds = new Rect(startBounds);
            View view = getChildAt((int) (mNextToRemoveItem - firstVisiblePosition));
            if (view != null) {
                endBounds.top = view.getTop();
            } else {
                view = getChildAt((int) (mNextToRemoveItem - firstVisiblePosition) - 1);
                if (view != null) {
                    endBounds.top = view.getBottom();
                }
            }
            endBounds.bottom = endBounds.top;
            animateDrawable(mDrawable, startBounds, endBounds);

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
                    child.animate().translationY(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mDrawable = null;
                        }
                    });
                }
            }
            mTops.clear();
            return true;
        }
    }

    private static final TypeEvaluator<Rect> sBoundsEvaluator = new TypeEvaluator<Rect>() {
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
