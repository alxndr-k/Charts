package com.example.piechart.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.piechart.Constants;
import com.example.piechart.R;

import java.util.ArrayList;

public class SlicesAdapter extends BaseAdapter {

    public interface OnRemoveListener {
        void onRemove(View parent);
    }

    private LayoutInflater mInflater;

    private OnRemoveListener mRemoveListener;

    private ArrayList<Slice> mValues = new ArrayList<Slice>(Constants.MAX_VALUES_COUNT);
    private ArrayList<Integer> mStableIds = new ArrayList<Integer>(Constants.MAX_VALUES_COUNT); // array with unique id, need that for animation

    private int mNextId; // unique id for next item to add
    private int mSum;

    public SlicesAdapter(Context context, OnRemoveListener listener, ArrayList<Slice> values) {
        mInflater = LayoutInflater.from(context);
        mRemoveListener = listener;
        mValues = values;
        for (mNextId = 0; mNextId < mValues.size(); ++mNextId) {
            mStableIds.add(mNextId);
            mSum += mValues.get(mNextId).value;
        }
    }

    @Override
    public int getCount() {
        return mValues.size();
    }

    @Override
    public Slice getItem(int position) {
        return mValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mStableIds.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            view = mInflater.inflate(R.layout.slice_list_item, parent, false);

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
        holder.seek.setProgress((int) getItem(position).value);

        holder.value.setText(String.format("%.1f%%", mValues.get(position).value * 100.0 / mSum));

        return view;
    }

    public void add() {
        mSum += Constants.DEFAULT_VALUE;
        mValues.add(0, new Slice(Constants.DEFAULT_VALUE));
        mStableIds.add(0, ++mNextId);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        mSum -= mValues.get(position).value;
        mValues.remove(position);
        mStableIds.remove(position);
        notifyDataSetChanged();
    }

    private SeekBar.OnSeekBarChangeListener mSeekChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
            int index = (Integer) seekBar.getTag();
            mSum = (int) (mSum - mValues.get(index).value + value);
            mValues.get(index).value = value;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            notifyDataSetChanged();
        }
    };

    private View.OnClickListener mRemoveOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mRemoveListener.onRemove((View) v.getParent());
        }
    };

    private class ViewHolder {
        TextView value;
        SeekBar seek;
    }
}
