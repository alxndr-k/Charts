package com.example.piechart.fragments;

import android.animation.*;
import android.app.Activity;
import android.app.ListFragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.*;
import android.widget.*;
import com.example.piechart.Constants;
import com.example.piechart.R;

import java.util.ArrayList;
import java.util.List;

public class PreferencesFragment extends ListFragment {

    private static final int ANIMATION_DURATION = 500;

    private static final String ARG_VALUES = "ARG_VALUES";

    private FragmentManagerInterface mManager;
    private ListView mListView;
    private SeekAdapter mAdapter;

    private SparseArray<Rect> mTops = new SparseArray<Rect>(Constants.MAX_VALUES_COUNT);
    private SparseArray<BitmapDrawable> mSnaps = new SparseArray<BitmapDrawable>(Constants.MAX_VALUES_COUNT);
    private List<BitmapDrawable> mCellBitmapDrawables = new ArrayList<BitmapDrawable>(Constants.MAX_VALUES_COUNT);

    public PreferencesFragment() {}

    public static PreferencesFragment newFragment(ArrayList<Integer> values) {
        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_VALUES, values);

        PreferencesFragment fragment = new PreferencesFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mManager = (FragmentManagerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implements FragmentManagerInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new SeekAdapter();
        setListAdapter(mAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = getListView();
        mListView.setDivider(null);
        setEmptyText(getString(R.string.preferences_no_items));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.preferences, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_chart:
                mManager.show(FragmentManagerInterface.Type.Chart);
                return true;
            case R.id.add:
                boolean maxReached = mAdapter.getCount() >= Constants.MAX_VALUES_COUNT;
                if (maxReached) {
                    showToast(R.string.preferences_added_max_items_number);
                } else {
                    addWithAnimation();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showToast(int message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void addWithAnimation() {
        saveViews(null, true);
        mAdapter.add();
        ViewTreeObserver observer = mListView.getViewTreeObserver();
        observer.addOnPreDrawListener(new AddOnPreDrawListener(observer));
    }

    private void removeWithAnimation(final View viewToRemove) {
        saveViews(viewToRemove, false);
        int position = mListView.getPositionForView(viewToRemove);
        mAdapter.remove(position);
        ViewTreeObserver observer = mListView.getViewTreeObserver();
        observer.addOnPreDrawListener(new RemoveOnPreDrawListener(observer));
    }

    private void saveViews(View ignoreView, boolean saveSnap) {
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        for (int i = 0; i < mListView.getChildCount(); ++i) {
            View child = mListView.getChildAt(i);
            if (child != ignoreView) {
                int position = firstVisiblePosition + i;
                int itemId = (int) mAdapter.getItemId(position);
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

        private ViewTreeObserver observer;

        public AddOnPreDrawListener(ViewTreeObserver observer) {
            this.observer = observer;
        }

        @Override
        public boolean onPreDraw() {
            observer.removeOnPreDrawListener(this);

            ArrayList<Animator> animators = new ArrayList<Animator>();
            View newChild = mListView.getChildAt(0);
            PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f);
            PropertyValuesHolder pvhTranslateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, newChild.getWidth() * 0.2f, 0.0f);
            animators.add(ObjectAnimator.ofPropertyValuesHolder(newChild, pvhAlpha, pvhTranslateX));

            int firstVisiblePosition = mListView.getFirstVisiblePosition();
            for (int i = 1; i < mListView.getChildCount(); ++i) {
                int id = (int) mAdapter.getItemId(firstVisiblePosition + i);
                Rect oldBounds = mTops.get(id);
                View child = mListView.getChildAt(i);
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
                        mListView.invalidate(mCurrentBound);
                    }
                });
                mCellBitmapDrawables.add(drawable);
                animators.add(animator);
                mTops.remove(key);
                mSnaps.remove(key);
            }

            mListView.setEnabled(true);
            AnimatorSet set = new AnimatorSet();
            set.setDuration(ANIMATION_DURATION);
            set.playTogether(animators);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCellBitmapDrawables.clear();
                    mListView.setEnabled(true);
                    mListView.invalidate();
                }
            });
            set.start();

            return true;
        }
    }

    private class RemoveOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

        private ViewTreeObserver observer;

        public RemoveOnPreDrawListener(ViewTreeObserver observer) {
            this.observer = observer;
        }

        public boolean onPreDraw() {
            observer.removeOnPreDrawListener(this);

            int firstVisiblePosition = mListView.getFirstVisiblePosition();
            for (int i = 0; i < mListView.getChildCount(); ++i) {
                View child = mListView.getChildAt(i);
                int position = firstVisiblePosition + i;
                int itemId = (int) mAdapter.getItemId(position);
                Rect oldBounds = mTops.get(itemId);
                int newTop = child.getTop();
                if (oldBounds != null) {
                    if (oldBounds.top != newTop) {
                        child.setTranslationY(oldBounds.top - newTop);
                        child.animate().translationY(0);
                    }
                } else {
                    int childHeight = child.getHeight() + mListView.getDividerHeight();
                    int oldTop = newTop + (i > 0 ? childHeight : -childHeight);
                    child.setTranslationY(oldTop - newTop);
                    child.animate().translationY(0);
                }
            }
            mTops.clear();
            return true;
        }
    }

    private class SeekAdapter extends BaseAdapter {

        private LayoutInflater inflater = LayoutInflater.from(getActivity());

        private ArrayList<Integer> values = new ArrayList<Integer>(Constants.MAX_VALUES_COUNT);
        private ArrayList<Integer> ids = new ArrayList<Integer>(Constants.MAX_VALUES_COUNT); // array with unique id, need that for animation

        private int nextId; // unique id for next item to add
        private int sum;

        public SeekAdapter() {
            values = getArguments().getIntegerArrayList(ARG_VALUES);
            for (nextId = 0; nextId < values.size(); ++nextId) {
                ids.add(nextId);
                sum += values.get(nextId);
            }
        }

        public void add() {
            sum += Constants.DEFAULT_VALUE;
            values.add(0, Constants.DEFAULT_VALUE);
            ids.add(0, ++nextId);
            notifyDataSetChanged();
        }

        public void remove(int position) {
            sum -= values.get(position);
            values.remove(position);
            ids.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return values.size();
        }

        @Override
        public Object getItem(int position) {
            return values.get(position);
        }

        @Override
        public long getItemId(int position) {
            return ids.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                view = inflater.inflate(R.layout.slice_list_item, parent, false);

                SeekBar seek = (SeekBar) view.findViewById(R.id.seek);
                seek.setOnSeekBarChangeListener(mSeekChangeListener);

                view.findViewById(R.id.remove).setOnClickListener(mRemoveOnClickListener);

                holder = new ViewHolder();
                holder.value = (TextView) view.findViewById(R.id.value);
                holder.seek = seek;
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.seek.setTag(position);
            holder.seek.setProgress((Integer) getItem(position));

            holder.value.setText(String.format("%.1f%%", values.get(position) * 100.0 / sum));

            return view;
        }

        private SeekBar.OnSeekBarChangeListener mSeekChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                int index = (Integer) seekBar.getTag();
                sum = sum - values.get(index) + value;
                values.set(index, value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                notifyDataSetChanged();
            }
        };

        private View.OnClickListener mRemoveOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeWithAnimation((View) v.getParent());
            }
        };

        private class ViewHolder {
            TextView value;
            SeekBar seek;
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
