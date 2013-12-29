package com.example.piechart.fragments;

import android.os.Bundle;
import android.view.*;
import com.example.piechart.R;
import com.example.piechart.charts.PieChart;

import java.util.Random;

public class ChartFragment extends Fragment {

    private static final int MAX_VALUES_COUNT = 10;

    private PieChart mChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chart_fragment, container, false);

        view.findViewById(R.id.refresh).setOnClickListener(mRefreshChartOnClickListener);
        mChart = (PieChart) view.findViewById(R.id.chart);
        mChart.apply(generateValues());

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
                mManager.show(Type.Preferences);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private float[] generateValues() {
        Random rand = new Random();
        int count = rand.nextInt(MAX_VALUES_COUNT - 2) + 2;
        float[] values = new float[count];

        for (int i = 0; i < count; ++i) values[i] = rand.nextFloat();

        return values;
    }

    private View.OnClickListener mRefreshChartOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mChart.apply(generateValues());
        }
    };
}
