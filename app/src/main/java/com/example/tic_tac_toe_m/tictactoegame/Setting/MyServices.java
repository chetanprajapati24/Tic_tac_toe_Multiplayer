package com.example.tic_tac_toe_m.tictactoegame.Setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.example.tic_tac_toe_m.R;

public class MyServices {

    public static boolean VIBRATION_CHECK = false;
    public static boolean SOUND_CHECK = true;

    private static MediaPlayer mediaPlayer;

    private static final String PREF_NAME = "TicTacToeSettings";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_VIBRATION = "vibration_enabled";

    // ✅ Load saved settings (call once at app start)
    public static void loadSettings(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SOUND_CHECK = preferences.getBoolean(KEY_SOUND, true);
        VIBRATION_CHECK = preferences.getBoolean(KEY_VIBRATION, false);
    }

    // ✅ Save setting when toggled
    public static void saveSetting(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    // ✅ Vibration control
    public static void vibrate(Context context, long duration) {
        if (!VIBRATION_CHECK) return; // exit if vibration disabled

        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null) {
                Log.d("VIBRATION", "No vibrator on device");
                return;
            }

            if (duration <= 0) duration = 100;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(duration);
            }
        } catch (SecurityException e) {
            Log.e("VIBRATION", "Vibration permission denied: " + e.getMessage());
        } catch (Exception e) {
            Log.e("VIBRATION", "Vibration error: " + e.getMessage());
        }
    }

    // ✅ Start background music
    public static void startBackgroundMusic(Context context, int resId) {
        if (!SOUND_CHECK) return; // don't play if sound disabled

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, resId);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    // ✅ Stop background music
    public static void stopBackgroundMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    // ✅ Release media player (call on app exit)
    public static void releaseBackgroundMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
