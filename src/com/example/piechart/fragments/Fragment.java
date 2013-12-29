package com.example.piechart.fragments;

import android.app.Activity;

public class Fragment extends android.app.Fragment {

    public enum Type { Chart, Preferences }

    public interface ManagerInterface { void show(Type type); }

    protected ManagerInterface mManager;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mManager = (ManagerInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implements ManagerInterface");
        }
    }
}
