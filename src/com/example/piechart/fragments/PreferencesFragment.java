package com.example.piechart.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.*;
import android.widget.*;
import com.example.piechart.Constants;
import com.example.piechart.R;

import java.util.ArrayList;

public class PreferencesFragment extends ListFragment {

    private static final int ANIMATION_DURATION_ADD = 700;
    private static final int ANIMATION_DURATION_REMOVE = 500;

    private static final String ARG_VALUES = "ARG_VALUES";

    private FragmentManagerInterface mManager;
    private ListView mListView;
    private SeekAdapter mAdapter;

    private SparseIntArray mViewsTops = new SparseIntArray(Constants.MAX_VALUES_COUNT);

    private PreferencesFragment() {}

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
                boolean maxReached = mAdapter.getCount() < Constants.MAX_VALUES_COUNT;
                if (maxReached) {
                    addWithAnimation();
                } else {
                    showToast(R.string.preferences_added_max_items_number);
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
        mAdapter.add(Constants.DEFAULT_VALUE);
        ViewTreeObserver observer = mListView.getViewTreeObserver();
        observer.addOnPreDrawListener(new AddOnPreDrawListener(observer));
    }

    private void removeWithAnimation(View viewToRemove) {
        saveViewsTop(viewToRemove);

        int position = mListView.getPositionForView(viewToRemove);
        mAdapter.remove(position);

        ViewTreeObserver observer = mListView.getViewTreeObserver();
        observer.addOnPreDrawListener(new RemoveOnPreDrawListener(observer));
    }

    private void saveViewsTop(View viewToRemove) {
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        for (int i = 0; i < mListView.getChildCount(); ++i) {
            View child = mListView.getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                int itemId = (int) mAdapter.getItemId(position);
                mViewsTops.put(itemId, child.getTop());
            }
        }
    }

    private class AddOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

        private ViewTreeObserver observer;

        public AddOnPreDrawListener(ViewTreeObserver observer) {
            this.observer = observer;
        }

        @Override
        public boolean onPreDraw() {
            observer.removeOnPreDrawListener(this);
            int lastVisiblePosition = mListView.getLastVisiblePosition();
            if (lastVisiblePosition == mAdapter.getCount() - 1) {
                View addedView = mListView.getChildAt(lastVisiblePosition);
                addedView.setAlpha(0);
                addedView.animate().setDuration(ANIMATION_DURATION_ADD).alpha(1);
            }
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
                int oldTop = mViewsTops.get(itemId, -1);
                int newTop = child.getTop();
                if (oldTop >= 0) {
                    if (oldTop != newTop) {
                        child.setTranslationY(oldTop - newTop);
                        child.animate().translationY(0);
                    }
                } else {
                    int childHeight = child.getHeight() + mListView.getDividerHeight();
                    oldTop = newTop + (i > 0 ? childHeight : -childHeight);
                    child.setTranslationY(oldTop - newTop);
                    child.animate().setDuration(ANIMATION_DURATION_REMOVE).translationY(0);
                }
            }
            mViewsTops.clear();
            return true;
        }
    }

    private class SeekAdapter extends BaseAdapter {

        private LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

        public void add(int value) {
            sum += value;
            values.add(value);
            ids.add(++nextId);
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
            holder.value.setAlpha(0);
            holder.value.animate().alpha(1).setDuration(ANIMATION_DURATION_ADD);

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
}
