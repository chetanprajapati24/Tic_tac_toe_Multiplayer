package com.example.tic_tac_toe_m.tictactoegame.AI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;

public class AIDifficultyActivity extends AppCompatActivity {

    private String playerName;
    private int playerSide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_aidifficulty);

        playerName = getIntent().getStringExtra("p1");
        playerSide = getIntent().getIntExtra("ps", 0);

        findViewById(R.id.btn_easy).setOnClickListener(v -> launchGame("Easy"));
        findViewById(R.id.btn_medium).setOnClickListener(v -> launchGame("Medium"));
        findViewById(R.id.btn_hard).setOnClickListener(v -> launchGame("Hard"));
    }

    private void launchGame(String level) {
        Intent intent = new Intent(this, AiGameActivity.class);
        intent.putExtra("p1", playerName);
        intent.putExtra("ps", playerSide);
        intent.putExtra("level", level);
        startActivity(intent);
    }
}
