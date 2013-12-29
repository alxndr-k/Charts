package com.example.piechart.fragments;

public interface FragmentManagerInterface {

    public enum Type {Chart, Preferences}

    public void show(Type type);

}