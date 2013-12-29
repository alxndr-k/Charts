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

    public static final int MAX_VALUES_COUNT = 10;
    public static final int MIN_VALUES_COUNT = 2;
    private static final int MAX_VALUE = 100;

    private ArrayList<Integer> mValues = generateValues();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        show(FragmentManagerInterface.Type.Chart);
    }

    @Override
    public void show(FragmentManagerInterface.Type type) {
        FragmentManager manager = getFragmentManager();

        Fragment fragment = manager.findFragmentByTag(type.toString());
        if (fragment == null) {
            fragment = newFragment(type);
            if (fragment == null) return;
        }

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(android.R.id.content, fragment, type.toString());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private Fragment newFragment(FragmentManagerInterface.Type type) {
        switch (type) {
            case Chart:
                return ChartFragment.newFragment(mValues);
            case Preferences:
                return PreferencesFragment.newFragment(mValues);
            default:
                return null;
        }
    }

    private static ArrayList<Integer> generateValues() {
        Random rand = new Random();

        int count = rand.nextInt(MAX_VALUES_COUNT - 2) + 2; // min 2 slices need
        ArrayList<Integer> values = new ArrayList<Integer>(count);
        for (int i = 0; i < count; ++i) values.add(rand.nextInt(MAX_VALUE));

        return values;
    }
}