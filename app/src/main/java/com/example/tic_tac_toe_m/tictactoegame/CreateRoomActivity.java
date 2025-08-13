package com.example.tic_tac_toe_m.tictactoegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateRoomActivity extends AppCompatActivity {

    private EditText roomCodeInput;
    private Button createRoomBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        roomCodeInput = findViewById(R.id.edit_room_code);
        createRoomBtn = findViewById(R.id.btn_create);

        createRoomBtn.setOnClickListener(v -> {
            String roomCode = roomCodeInput.getText().toString().trim();

            if (roomCode.isEmpty()) {
                Toast.makeText(this, "Enter room code", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create room in Firebase
            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);
            roomRef.child("player1").setValue("host");
            roomRef.child("board").setValue("---------"); // empty board
            roomRef.child("turn").setValue("X");

            // Start game as Player X (host)
            Intent intent = new Intent(this, MultiplayerGameActivity.class);
            intent.putExtra("roomCode", roomCode);
            intent.putExtra("playerSymbol", "X");
            startActivity(intent);
        });
    }
}
