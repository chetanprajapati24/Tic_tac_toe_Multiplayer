package com.example.tic_tac_toe_m.tictactoegame.AI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;

public class AiChooseSymbolActivity extends AppCompatActivity implements View.OnTouchListener {

    private ImageView  crossImg, crossRadioImg, circleImg, circleRadioImg;
    private Button continueBtn;

    private int pickSide = 0; // Default: 0 for Cross
    private String playerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Full screen make
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_ai_choose_symbol);

        // Get player name
        playerName = getIntent().getStringExtra("p1");
        if (playerName == null) playerName = "";

        // View bindings
        crossImg = findViewById(R.id.ai_pick_side_cross_img);
        circleImg = findViewById(R.id.ai_pick_side_circle_img);
        crossRadioImg = findViewById(R.id.ai_pick_side_cross_radio);
        circleRadioImg = findViewById(R.id.ai_pick_side_circle_radio);
        continueBtn = findViewById(R.id.ai_pick_side_continue_btn);

        // Back button
       // backBtn.setOnClickListener(v -> onBackPressed());

        // Cross selected
        crossRadioImg.setOnClickListener(v -> {
            pickSide = 0;
            crossRadioImg.setImageResource(R.drawable.radio_button_checked);
            circleRadioImg.setImageResource(R.drawable.radio_button_unchecked);
            circleImg.setAlpha(0.3f);
            crossImg.setAlpha(1.0f);
        });

        // Circle selected
        circleRadioImg.setOnClickListener(v -> {
            pickSide = 1;
            circleRadioImg.setImageResource(R.drawable.radio_button_checked);
            crossRadioImg.setImageResource(R.drawable.radio_button_unchecked);
            crossImg.setAlpha(0.3f);
            circleImg.setAlpha(1.0f);
        });

        // Continue button
        continueBtn.setOnTouchListener(this);
        continueBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AIDifficultyActivity.class);
            intent.putExtra("p1", playerName);
            intent.putExtra("ps", pickSide);
            startActivity(intent);
        });
        // Inside onCreate()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back behavior or just close activity
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.ai_pick_side_continue_btn) {
            v.setAlpha(event.getAction() == MotionEvent.ACTION_DOWN ? 0.5f : 1f);
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Optional: Add custom behavior here if needed
    }

}
