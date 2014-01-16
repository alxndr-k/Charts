package com.example.piechart;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.example.piechart.fragments.ChartFragment;
import com.example.piechart.fragments.FragmentManagerInterface;
import com.example.piechart.fragments.PreferencesFragment;

import java.util.ArrayList;
import java.util.Random;

public class ChartActivity extends Activity implements FragmentManagerInterface {

    private static final String SAVED_VALUES = "SAVED_VALUES";
    private ArrayList<Integer> mValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mValues = generateValues();
            show(FragmentManagerInterface.Type.Chart);
        } else {
            mValues = savedInstanceState.getIntegerArrayList(SAVED_VALUES);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(SAVED_VALUES, mValues);
    }

    @Override
    public void show(Type type) {
        FragmentManager manager = getFragmentManager();

        Fragment fragment = manager.findFragmentByTag(type.toString());
        if (fragment == null) {
            fragment = newFragment(type);
            if (fragment == null) return;
        }

        FragmentTransaction transaction = manager.beginTransaction();
        addAnimation(transaction, type);
        transaction.replace(android.R.id.content, fragment, type.toString());
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

    private static void addAnimation(FragmentTransaction transaction, Type type) {
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

    private static ArrayList<Integer> generateValues() {
        Random rand = new Random();

        int count = rand.nextInt(Constants.MAX_VALUES_COUNT) + 1;
        ArrayList<Integer> values = new ArrayList<Integer>(count);
        for (int i = 0; i < count; ++i) values.add(rand.nextInt(Constants.MAX_VALUE));

        return values;
    }
}