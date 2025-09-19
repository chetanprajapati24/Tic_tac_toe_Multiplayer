package com.example.tic_tac_toe_m.tictactoegame.Setting;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class MyServices {
    public static boolean VIBRATION_CHECK = false;
    public static boolean SOUND_CHECK = true;

    public static void vibrate(Context context, long duration) {
        if (VIBRATION_CHECK) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(duration);
                }
            } else {
                Log.d("VIBRATION", "No vibrator on device");
            }
        }
    }

}