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

    public interface OnValueChangedListener {
        void onChanged();

        void onRemove(View parent);
    }

    private LayoutInflater mInflater;

    private OnValueChangedListener mListener;

    private ArrayList<Slice> mValues = new ArrayList<Slice>(Constants.MAX_VALUES_COUNT);
    private ArrayList<Integer> mStableIds = new ArrayList<Integer>(Constants.MAX_VALUES_COUNT); // array with unique id, need that for animation

    private int mNextId; // unique id for next item to add
    private int mSum;

    public SlicesAdapter(Context context, OnValueChangedListener listener, ArrayList<Slice> values) {
        mInflater = LayoutInflater.from(context);
        mListener = listener;
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
            view = mInflater.inflate(R.layout.li_slice, parent, false);
            view.findViewById(R.id.remove).setOnClickListener(mRemoveOnClickListener);

            holder = new ViewHolder();
            holder.value = (TextView) view.findViewById(R.id.value);
            holder.seek = (SeekBar) view.findViewById(R.id.seek);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.seek.setTag(position);
        holder.seek.setProgress((int) getItem(position).value);
        holder.seek.setOnSeekBarChangeListener(mSeekChangeListener);

        holder.value.setText(String.format("%.1f%%", mValues.get(position).value * 100.0 / mSum));

        return view;
    }

    public void add() {
        mSum += Constants.DEFAULT_VALUE;
        mValues.add(0, new Slice(Constants.DEFAULT_VALUE));
        mStableIds.add(0, ++mNextId);
        notifyDataSetChanged();
        mListener.onChanged();
    }

    public void remove(int position) {
        mSum -= mValues.get(position).value;
        mValues.remove(position);
        mStableIds.remove(position);
        notifyDataSetChanged();
        mListener.onChanged();
    }

    private SeekBar.OnSeekBarChangeListener mSeekChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
            int index = (Integer) seekBar.getTag();
            mSum = (int) (mSum - mValues.get(index).value + value);
            mValues.get(index).value = value;

            if (fromUser) {
                mListener.onChanged();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            notifyDataSetChanged();
        }
    };

    private View.OnClickListener mRemoveOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onRemove((View) v.getParent());
        }
    };

    private class ViewHolder {
        TextView value;
        SeekBar seek;
    }
}
