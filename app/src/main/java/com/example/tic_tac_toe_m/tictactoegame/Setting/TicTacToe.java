package com.example.tic_tac_toe_m.tictactoegame.Setting;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.example.tic_tac_toe_m.R;

public class TicTacToe extends Application {

    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }

            @Override
            public void onActivityStarted(Activity activity) {
                activityReferences++;
                if (activityReferences == 1 && !isActivityChangingConfigurations) {
                    // App comes to foreground
                    MyServices.startBackgroundMusic(activity, R.raw.gotheme);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) { }

            @Override
            public void onActivityPaused(Activity activity) { }

            @Override
            public void onActivityStopped(Activity activity) {
                activityReferences--;
                isActivityChangingConfigurations = activity.isChangingConfigurations();
                if (activityReferences == 0 && !isActivityChangingConfigurations) {
                    // App goes to background
                    MyServices.stopBackgroundMusic();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

            @Override
            public void onActivityDestroyed(Activity activity) { }
        });
    }
}
