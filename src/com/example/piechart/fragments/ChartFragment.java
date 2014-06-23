package com.example.piechart.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.*;
import com.example.piechart.R;
import com.example.piechart.views.adapters.Slice;
import com.example.piechart.views.charts.PieChart;

import java.util.ArrayList;

public class ChartFragment extends Fragment {

    private static final String ARG_VALUES = "ARG_VALUES";

    private FragmentManagerInterface mManager;
    private PieChart mChart;

    private ArrayList<Slice> mValues;

    public ChartFragment() {}

    public static ChartFragment newFragment(ArrayList<Slice> values) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_VALUES, values);

        ChartFragment fragment = new ChartFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mValues = (ArrayList<Slice>) getArguments().getSerializable(ARG_VALUES);
        mChart = new PieChart(inflater.getContext());
        mChart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        int padding = getResources().getDimensionPixelOffset(R.dimen.padding_middle);
        mChart.setPadding(padding, padding, padding, padding);
        mChart.apply(mValues, false);

        return mChart;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chart, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_preferences:
                mManager.show(FragmentManagerInterface.Type.Preferences);
                return true;
            case R.id.refresh:
                mChart.apply(mValues, true);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
