package com.example.piechart.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.*;
import com.example.piechart.R;
import com.example.piechart.charts.PieChart;

import java.util.ArrayList;

public class ChartFragment extends Fragment {

    private static final String ARG_VALUES = "ARG_VALUES";

    private FragmentManagerInterface mManager;
    private PieChart mChart;

    public ChartFragment() {}

    public static ChartFragment newFragment(ArrayList<Integer> values) {
        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_VALUES, values);

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
        View view = inflater.inflate(R.layout.chart_fragment, container, false);

        view.findViewById(R.id.refresh).setOnClickListener(mRefreshChartOnClickListener);

        ArrayList<Integer> values = getArguments().getIntegerArrayList(ARG_VALUES);
        mChart = (PieChart) view.findViewById(R.id.chart);
        mChart.apply(values);

        return view;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View.OnClickListener mRefreshChartOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mChart.refresh();
        }
    };
}
