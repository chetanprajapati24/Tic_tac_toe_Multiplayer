package com.example.tic_tac_toe_m.tictactoegame.Multiplayer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    }
}