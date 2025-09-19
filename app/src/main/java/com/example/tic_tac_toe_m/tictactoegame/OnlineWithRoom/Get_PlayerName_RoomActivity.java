package com.example.tic_tac_toe_m.tictactoegame.OnlineWithRoom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;

public class Get_PlayerName_RoomActivity extends AppCompatActivity {

    EditText playerName;
    Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_player_name_room);

        playerName = findViewById(R.id.room_player_name_edttxt);
        nextBtn = findViewById(R.id.ai_player_name_btn);

        nextBtn.setOnClickListener(v -> {
            String playerNm = playerName.getText().toString().trim();

            if (playerNm.isEmpty()) {
                Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Get_PlayerName_RoomActivity.this, OnlineRoomActivity.class);
            intent.putExtra("Player_Name", playerNm);
            startActivity(intent);
        });
    }
}
