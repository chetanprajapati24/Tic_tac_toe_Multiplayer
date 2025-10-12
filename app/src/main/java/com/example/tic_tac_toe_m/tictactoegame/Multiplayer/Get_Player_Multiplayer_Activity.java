package com.example.tic_tac_toe_m.tictactoegame.Multiplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tic_tac_toe_m.R;
import com.example.tic_tac_toe_m.tictactoegame.OnlineWithRoom.Get_PlayerName_RoomActivity;
import com.example.tic_tac_toe_m.tictactoegame.OnlineWithRoom.OnlineRoomActivity;

public class Get_Player_Multiplayer_Activity extends AppCompatActivity {

    EditText playerName;
    Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_get_player_multiplayer);

        playerName = findViewById(R.id.multi_player_name_edttxt);
        nextBtn = findViewById(R.id.multi_player_name_btn);

        nextBtn.setOnClickListener(v -> {
            String playerNm = playerName.getText().toString().trim();

            if (playerNm.isEmpty()) {
                Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Get_Player_Multiplayer_Activity.this, MultiplayerActivity.class);
            intent.putExtra(MyConstants.playerName, playerNm);
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
}