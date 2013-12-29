package com.example.piechart.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import com.example.piechart.R;

public class PreferencesFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView view = new TextView(getActivity());
        view.setText("Hello, world!");
        view.setGravity(Gravity.CENTER);
        return view;
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
                mManager.show(Type.Chart);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
