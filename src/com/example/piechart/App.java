package com.example.piechart;

import android.app.Application;

public class App extends Application {

    private static App self;

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
    }

    public static boolean isLandscape() {
        return self.getResources().getBoolean(R.bool.is_landscape);
    }
}
