package com.example.tic_tac_toe_m.tictactoegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
        setContentView(R.layout.activity_join_room);

        roomCodeInput = findViewById(R.id.et_room_code);
        joinRoomBtn = findViewById(R.id.btn_join);

        joinRoomBtn.setOnClickListener(v -> {
            String roomCode = roomCodeInput.getText().toString().trim();

            if (roomCode.isEmpty()) {
                Toast.makeText(this, "Enter room code", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);

            roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        roomRef.child("player2").setValue("guest");

                        Intent intent = new Intent(JoinRoomActivity.this, MultiplayerGameActivity.class);
                        intent.putExtra("roomCode", roomCode);
                        intent.putExtra("playerSymbol", "O"); // Guest plays O
                        startActivity(intent);
                    } else {
                        Toast.makeText(JoinRoomActivity.this, "Room not found!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(JoinRoomActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
