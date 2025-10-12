package com.example.tic_tac_toe_m.tictactoegame.TwoPlayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;

public class OfflineGetPlayersNamesActivity extends AppCompatActivity implements View.OnTouchListener {

    private EditText playerOneName, playerTwoName;
    private Button playerOneButton, playerTwoButton;
   // private ImageView backBtn;
    private LinearLayout playerOneLayout, playerTwoLayout;

    private String playerOne, playerTwo;
    private boolean isFirstLayoutVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_offline_get_players_names);

        // Bind views
  //      backBtn = findViewById(R.id.player_names_back_btn);
        playerOneName = findViewById(R.id.player_one_name_edttxt);
        playerTwoName = findViewById(R.id.player_two_name_edttxt);
        playerOneButton = findViewById(R.id.player_one_btn);
        playerTwoButton = findViewById(R.id.player_two_btn);
        playerOneLayout = findViewById(R.id.player_one_layout);
        playerTwoLayout = findViewById(R.id.player_two_layout);

        // First "Next" Button
        playerOneButton.setOnTouchListener(this);
        playerOneButton.setOnClickListener(v -> {
            String name = playerOneName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Enter Player One Name", Toast.LENGTH_SHORT).show();
            } else {
                playerOne = name;
                isFirstLayoutVisible = false;
                playerOneLayout.setVisibility(View.GONE);
                playerTwoLayout.setVisibility(View.VISIBLE);
                slideUp(playerTwoLayout);
            }
        });

        // Back button
    //    backBtn.setOnClickListener(v -> onBackPressed());

        // Second "Next" Button
        playerTwoButton.setOnTouchListener(this);
        playerTwoButton.setOnClickListener(v -> {
            String name = playerTwoName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Enter Player Two Name", Toast.LENGTH_SHORT).show();
            } else {
                playerTwo = name;
                Intent intent = new Intent(OfflineGetPlayersNamesActivity.this, ChooseSymbolActivity.class);
                intent.putExtra("p1", playerOne);
                intent.putExtra("p2", playerTwo);
                startActivity(intent);
            }
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

    // Button press alpha animation
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if ((v == playerOneButton && isFirstLayoutVisible) || (v == playerTwoButton && !isFirstLayoutVisible)) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.5f);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1f);
            }
        }
        return false;
    }

    // Slide-up animation
    private void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0, 0,
                view.getHeight(), 0);
        animate.setDuration(400);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // Optional: remove gesture back override since it only calls super
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
