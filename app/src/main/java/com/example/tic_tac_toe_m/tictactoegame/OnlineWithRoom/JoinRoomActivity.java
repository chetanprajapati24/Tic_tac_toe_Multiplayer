package com.example.tic_tac_toe_m.tictactoegame.OnlineWithRoom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinRoomActivity extends AppCompatActivity {

    private EditText roomCodeInput;
    private Button joinRoomBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_join_room);

        roomCodeInput = findViewById(R.id.et_room_code);
        joinRoomBtn = findViewById(R.id.btn_join);

        // Check if room code comes from a shared link
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("roomCode")) {
            String sharedRoomCode = intent.getStringExtra("roomCode");
            roomCodeInput.setText(sharedRoomCode); // Auto-fill
        }

        joinRoomBtn.setOnClickListener(v -> joinRoom());
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

    private void joinRoom() {
        String roomCode = roomCodeInput.getText().toString().trim();
        String playerName = getIntent().getStringExtra("Player_Name");

        if (roomCode.isEmpty()) {
            Toast.makeText(this, "Enter room code", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);

        // Disable button while checking
        joinRoomBtn.setEnabled(false);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                joinRoomBtn.setEnabled(true);

                if (snapshot.exists()) {
                 // roomRef.child("player1").setValue(playerName);
                    roomRef.child("player2").child("O").setValue(playerName); // Add this

                    Intent intent = new Intent(JoinRoomActivity.this, RoomplayerGameActivity.class);
                    intent.putExtra("roomCode", roomCode);
                    intent.putExtra("playerSymbol", "O"); // Guest plays O
                    intent.putExtra("Player_Name",playerName);
                    startActivity(intent);
                    finish();
                }
            }

                @Override
            public void onCancelled(@NonNull DatabaseError error) {
                joinRoomBtn.setEnabled(true);
                Toast.makeText(JoinRoomActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
