package com.example.piechart;

import android.app.Activity;
import android.os.Bundle;

public class PreferencesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.isLandscape()) return;

        setContentView(R.layout.a_preferences);
    }
}
