package com.example.tic_tac_toe_m.tictactoegame;

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
        setContentView(R.layout.activity_online_room);

        createRoomBtn = findViewById(R.id.btn_create_room);
        joinRoomBtn = findViewById(R.id.btn_join_room);

        // Animate buttons
        animateButtons();

        // Button click handlers
        createRoomBtn.setOnClickListener(v -> {
            Intent intent = new Intent(OnlineRoomActivity.this, CreateRoomActivity.class);
            startActivity(intent);
        });

        joinRoomBtn.setOnClickListener(v -> {
            Intent intent = new Intent(OnlineRoomActivity.this, JoinRoomActivity.class);
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
