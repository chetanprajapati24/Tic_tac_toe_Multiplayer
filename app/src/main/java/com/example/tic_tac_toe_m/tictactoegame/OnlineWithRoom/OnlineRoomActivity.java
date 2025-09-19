package com.example.tic_tac_toe_m.tictactoegame.OnlineWithRoom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;

import com.example.tic_tac_toe_m.R;

public class OnlineRoomActivity extends AppCompatActivity {

    private static final int STARTUP_DELAY = 300;
    private static final int ANIM_ITEM_DURATION = 1000;
    private static final int ITEM_DELAY = 300;

    private Button createRoomBtn, joinRoomBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_online_room);

        createRoomBtn = findViewById(R.id.btn_create_room);
        joinRoomBtn = findViewById(R.id.btn_join_room);

        // Animate buttons
        animateButtons();

        // Button click handlers
        createRoomBtn.setOnClickListener(v -> {
            String playerName = getIntent().getStringExtra("Player_Name");
            Intent intent = new Intent(OnlineRoomActivity.this, CreateRoomActivity.class);
            intent.putExtra("Player_Name", playerName);
            startActivity(intent);
        });

        joinRoomBtn.setOnClickListener(v -> {
            String playerName = getIntent().getStringExtra("Player_Name");
            Intent intent = new Intent(OnlineRoomActivity.this, JoinRoomActivity.class);
            intent.putExtra("Player_Name", playerName);
            startActivity(intent);
        });
    }
    private void animateButtons() {
        View[] views = {createRoomBtn, joinRoomBtn};

        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            ViewPropertyAnimatorCompat viewAnimator = ViewCompat.animate(v)
                    .scaleY(1).scaleX(1)
                    .setStartDelay((ITEM_DELAY * i) + STARTUP_DELAY)
                    .setDuration(ANIM_ITEM_DURATION);

            viewAnimator.setInterpolator(new DecelerateInterpolator()).start();
        }
    }
}
