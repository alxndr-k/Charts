package com.example.piechart;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.example.piechart.fragments.ChartFragment;
import com.example.piechart.fragments.FragmentManagerInterface;
import com.example.piechart.fragments.PreferencesFragment;
import com.example.piechart.views.adapters.Slice;

import java.util.ArrayList;
import java.util.Random;

public class ChartActivity extends Activity implements FragmentManagerInterface {

    private static final String SAVED_VALUES = "SAVED_VALUES";
    private ArrayList<Slice> mValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mValues = generateValues();
        } else {
            mValues = (ArrayList<Slice>) savedInstanceState.getSerializable(SAVED_VALUES);
        }

        setContentView(R.layout.a_main);
        removeFragments();
        if (App.isLandscape()) {
            show(Type.Preferences, R.id.holder_right);
            show(Type.Chart, R.id.holder_left);
        } else {
            show(Type.Chart, R.id.holder);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_VALUES, mValues);
    }

    @Override
    public void onChanged() {
        if (App.isLandscape()) {
            ChartFragment fragment = (ChartFragment) getFragmentManager().findFragmentByTag(Type.Chart.name());
            fragment.update();
        }
    }

    @Override
    public void show(Type type) {
        show(type, R.id.holder);
    }

    private void show(Type type, int holder) {
        FragmentManager manager = getFragmentManager();

        Fragment fragment = newFragment(type);

        FragmentTransaction transaction = manager.beginTransaction();
        addAnimation(transaction, type);
        transaction.replace(holder, fragment, type.name());
        transaction.commit();
    }

    private Fragment newFragment(Type type) {
        switch (type) {
            case Chart:
                return ChartFragment.newFragment(mValues);
            case Preferences:
                return PreferencesFragment.newFragment(mValues);
            default:
                return null;
        }
    }

    private void removeFragments() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        for (Type type : Type.values()) {
            Fragment fragment = manager.findFragmentByTag(type.name());
            if (fragment != null) transaction.remove(fragment);
        }

        transaction.commit();
    }

    private static void addAnimation(FragmentTransaction transaction, Type type) {
        if (App.isLandscape()) return;

        switch (type) {
            case Chart:
                transaction.setCustomAnimations(R.animator.view_enter_left, R.animator.view_exit_left);
                break;
            case Preferences:
                transaction.setCustomAnimations(R.animator.view_enter_right, R.animator.view_exit_right);
                break;
            default:
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
    }

    private static ArrayList<Slice> generateValues() {
        Random rand = new Random();

        int count = rand.nextInt(Constants.MAX_VALUES_COUNT) + 1;
        ArrayList<Slice> values = new ArrayList<Slice>(count);
        for (int i = 0; i < count; ++i) values.add(new Slice(rand.nextInt(Constants.MAX_VALUE)));

        return values;
    }
}