package com.example.tic_tac_toe_m.tictactoegame.Setting;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;

public class SettingsActivity extends AppCompatActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch vibrationSwitch, soundSwitch;
    private LinearLayout feedback;

    private SharedPreferences preferences;
    private static final String PREF_NAME = "TicTacToeSettings";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_VIBRATION = "vibration_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        // Bind views
        vibrationSwitch = findViewById(R.id.vibration_switch);
        soundSwitch = findViewById(R.id.sound_switch);
        feedback = findViewById(R.id.feedback_layout);

        // Initialize SharedPreferences
        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Load saved settings
        boolean isSoundEnabled = preferences.getBoolean(KEY_SOUND, true);
        boolean isVibrationEnabled = preferences.getBoolean(KEY_VIBRATION, true);

        vibrationSwitch.setChecked(isVibrationEnabled);
        soundSwitch.setChecked(isSoundEnabled);

        // Update MyServices variables to match saved state
        MyServices.VIBRATION_CHECK = isVibrationEnabled;
        MyServices.SOUND_CHECK = isSoundEnabled;

        // Handle switch changes
        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MyServices.VIBRATION_CHECK = isChecked;
            savePreference(KEY_VIBRATION, isChecked);
        });

        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MyServices.SOUND_CHECK = isChecked;
            savePreference(KEY_SOUND, isChecked);

            if (!isChecked) {
                MyServices.stopBackgroundMusic();
            } else {
                MyServices.startBackgroundMusic(this, R.raw.gotheme);
            }
        });

        feedback.setOnClickListener(v -> composeEmail("Tic Tac Toe Feedback"));

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void composeEmail(String subject) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:chiragprajapati24.cp@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(intent, "Send feedback"));
            }
        } catch (ActivityNotFoundException e) {
            // Show a Toast or log if no email app is found
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
