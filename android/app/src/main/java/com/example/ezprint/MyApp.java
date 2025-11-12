package com.example.ezprint;

import android.app.Application;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class MyApp extends Application implements DefaultLifecycleObserver {

    private static boolean isAppInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // Register lifecycle observer
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(LifecycleOwner owner) {
        isAppInForeground = true;
    }

    @Override
    public void onStop(LifecycleOwner owner) {
        isAppInForeground = false;
    }

    public static boolean isAppInForeground() {
        return isAppInForeground;
    }
}
