package com.example.tic_tac_toe_m.tictactoegame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;

public class AiChooseSymbolActivity extends AppCompatActivity implements View.OnTouchListener {

    private ImageView backBtn, crossImg, crossRadioImg, circleImg, circleRadioImg;
    private Button continueBtn;

    private int pickSide = 0; // Default: 0 for Cross
    private String playerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen setup
     /*   requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_ai_choose_symbol);

        // Get player name
        playerName = getIntent().getStringExtra("p1");
        if (playerName == null) playerName = "";

        // View bindings
        backBtn = findViewById(R.id.ai_pick_side_back_btn);
        crossImg = findViewById(R.id.ai_pick_side_cross_img);
        circleImg = findViewById(R.id.ai_pick_side_circle_img);
        crossRadioImg = findViewById(R.id.ai_pick_side_cross_radio);
        circleRadioImg = findViewById(R.id.ai_pick_side_circle_radio);
        continueBtn = findViewById(R.id.ai_pick_side_continue_btn);

        // Back button
        backBtn.setOnClickListener(v -> onBackPressed());

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
            Intent intent = new Intent(this, AiGameActivity.class);
            intent.putExtra("p1", playerName);
            intent.putExtra("ps", pickSide);
            startActivity(intent);
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.ai_pick_side_continue_btn) {
            v.setAlpha(event.getAction() == MotionEvent.ACTION_DOWN ? 0.5f : 1f);
        }
        return false;
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Optional: Add custom behavior here if needed
    }

}
