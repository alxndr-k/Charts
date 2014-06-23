package com.example.piechart.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import com.example.piechart.Constants;
import com.example.piechart.R;
import com.example.piechart.views.AnimatedListView;
import com.example.piechart.views.adapters.Slice;
import com.example.piechart.views.adapters.SlicesAdapter;

import java.util.ArrayList;

public class PreferencesFragment extends Fragment implements SlicesAdapter.OnRemoveListener {

    private static final String ARG_VALUES = "ARG_VALUES";

    private FragmentManagerInterface mManager;
    private AnimatedListView mListView;
    private SlicesAdapter mAdapter;

    public PreferencesFragment() {
    }

    public static PreferencesFragment newFragment(ArrayList<Slice> values) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_VALUES, values);

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_preferences, container, false);
        mListView = (AnimatedListView) view.findViewById(R.id.list_view);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        mAdapter = new SlicesAdapter(getActivity(), this, (ArrayList<Slice>) getArguments().getSerializable(ARG_VALUES));
        mListView.setAdapter(mAdapter);
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
                mManager.show(FragmentManagerInterface.Type.Chart);
                return true;
            case R.id.add:
                boolean maxReached = mAdapter.getCount() >= Constants.MAX_VALUES_COUNT;
                if (maxReached) {
                    showToast(R.string.preferences_added_max_items_number);
                } else {
                    mListView.addWithAnimation();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRemove(View view) {
        mListView.removeWithAnimation(view);
    }

    private void showToast(int message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
