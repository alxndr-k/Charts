package com.example.piechart;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.example.piechart.fragments.ChartFragment;
import com.example.piechart.fragments.Fragment;
import com.example.piechart.fragments.PreferencesFragment;

public class ChartActivity extends Activity implements Fragment.ManagerInterface{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        show(Fragment.Type.Chart);
    }

    @Override
    public void show(Fragment.Type type) {
        FragmentManager manager = getFragmentManager();

        Fragment fragment = (Fragment) manager.findFragmentByTag(type.toString());
        if (fragment == null) {
            fragment = newFragment(type);
            if (fragment == null) return;
        }

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(android.R.id.content, fragment, type.toString());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private Fragment newFragment(Fragment.Type type) {
        switch (type) {
            case Chart:
                return new ChartFragment();
            case Preferences:
                return new PreferencesFragment();
            default:
                return null;
        }
    }
}