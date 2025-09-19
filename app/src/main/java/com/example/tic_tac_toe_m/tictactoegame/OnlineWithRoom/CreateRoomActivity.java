package com.example.tic_tac_toe_m.tictactoegame.OnlineWithRoom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class CreateRoomActivity extends AppCompatActivity {

    private Button createRoomBtn, shareRoomBtn;
    private TextView roomCodeText;
    private String roomCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        roomCodeText = findViewById(R.id.tv_room_code);
        createRoomBtn = findViewById(R.id.btn_create);
        shareRoomBtn = findViewById(R.id.btn_share);

        roomCode = generateRoomCode().toUpperCase();
        roomCodeText.setText("Room Code: " + roomCode);

        createRoomBtn.setOnClickListener(v -> createRoom());
        shareRoomBtn.setOnClickListener(v -> shareRoomLink());
    }

    private String generateRoomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(rnd.nextInt(characters.length())));
        }
        return code.toString();
    }

    private void createRoom() {
        String playerName = getIntent().getStringExtra("Player_Name");
        DatabaseReference roomRef = FirebaseDatabase.getInstance()
                .getReference("rooms")
                .child(roomCode);

        // Initialize room data
        roomRef.child("player1").child("X").setValue(playerName);
        roomRef.child("board").setValue("---------"); // empty board
        roomRef.child("turn").setValue("X");

        // Open game activity
        Intent intent = new Intent(this, RoomplayerGameActivity.class);
        intent.putExtra("roomCode", roomCode);
        intent.putExtra("playerSymbol", "X");
        intent.putExtra("Player_Name", playerName);
        startActivity(intent);
    }

    private void shareRoomLink() {
        String roomLink = "Join my Tic Tac Toe room! Room code: " + roomCode;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, roomLink);
        startActivity(Intent.createChooser(shareIntent, "Share Room via"));
    }
}
