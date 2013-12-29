package com.example.piechart.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.example.piechart.Constans;
import com.example.piechart.R;

import java.util.ArrayList;

public class PreferencesFragment extends ListFragment {

    private static final String ARG_VALUES = "ARG_VALUES";

    private FragmentManagerInterface mManager;
    private ArrayList<Integer> mValues;
    private BaseAdapter mAdapter;

    private MenuItem mAdd;

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

        mValues = getArguments().getIntegerArrayList(ARG_VALUES);
        mAdapter = new SeekAdapter();
        setListAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.preferences, menu);
        mAdd = menu.findItem(R.id.add);
        updateAddVisibility();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_chart:
                mManager.show(FragmentManagerInterface.Type.Chart);
                return true;
            case R.id.add:
                mValues.add(Constans.MAX_VALUE / 2);
                mAdapter.notifyDataSetChanged();
                updateAddVisibility();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateAddVisibility() {
        boolean visible = mValues.size() < Constans.MAX_VALUES_COUNT;
        mAdd.setEnabled(visible);
    }

    private class SeekAdapter extends BaseAdapter {

        private LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @Override
        public int getCount() {
            return mValues.size();
        }

        @Override
        public Object getItem(int position) {
            return mValues.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO: add states for remove
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                view = inflater.inflate(R.layout.slice_list_item, parent, false);

                SeekBar seek = (SeekBar) view.findViewById(R.id.seek);
                seek.setOnSeekBarChangeListener(mSeekChangeListener);

                View remove = view.findViewById(R.id.remove);
                remove.setOnClickListener(mRemoveOnClickListener);

                holder = new ViewHolder();
                holder.seek = seek;
                holder.remove = remove;
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.seek.setTag(position);
            holder.seek.setProgress((java.lang.Integer) getItem(position));
            holder.remove.setTag(position);
            holder.remove.setEnabled(getCount() > Constans.MIN_VALUES_COUNT);

            return view;
        }

        private SeekBar.OnSeekBarChangeListener mSeekChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int index = (Integer) seekBar.getTag();
                mValues.set(index, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        private View.OnClickListener mRemoveOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = (Integer) v.getTag();
                mValues.remove(index);
                notifyDataSetInvalidated();
                updateAddVisibility();
            }
        };

        private class ViewHolder {
            SeekBar seek;
            View remove;
        }
    }
}
