package com.example.tic_tac_toe_m.tictactoegame;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

public class SettingsActivity extends AppCompatActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch vibrationSwitch, soundSwitch;
    private LinearLayout rateUs, feedback;
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        // Bind views
        vibrationSwitch = findViewById(R.id.vibration_switch);
        soundSwitch = findViewById(R.id.sound_switch);
        backBtn = findViewById(R.id.settings_back_btn);
        rateUs = findViewById(R.id.rate_us_layout);
        feedback = findViewById(R.id.feedback_layout);

        // Initialize switch states
        vibrationSwitch.setChecked(MyServices.VIBRATION_CHECK);
        soundSwitch.setChecked(MyServices.SOUND_CHECK);

        // Set listeners
        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                MyServices.VIBRATION_CHECK = isChecked);

        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                MyServices.SOUND_CHECK = isChecked);

        backBtn.setOnClickListener(v -> onBackPressed());

        rateUs.setOnClickListener(v -> askRatings());

        feedback.setOnClickListener(v -> composeEmail("Tic Tac Toe Feedback"));
    }

    private void askRatings() {
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                flow.addOnCompleteListener(task2 -> {
                    // Review flow completed
                });
            } else {
                // You can show a fallback message or ignore silently
            }
        });
    }

    private void composeEmail(String subject) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:chiragprajapati24.cp@gmail..com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(intent, "Send feedback"));
            }
        } catch (ActivityNotFoundException e) {
            // Fallback or toast if no email client is found
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
