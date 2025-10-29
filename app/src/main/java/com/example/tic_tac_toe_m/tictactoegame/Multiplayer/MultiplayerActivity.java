package com.example.tic_tac_toe_m.tictactoegame.Multiplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class MultiplayerActivity extends AppCompatActivity {

    private static final long WAIT_TIMEOUT_MS = 30000; // 30s wait

    private DatabaseReference lobbyRef;
    private DatabaseReference gamesRef;
    private String playerId;
    private String playerName;
    private TextView statusText;
    private Button continueBtn;

    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;
    private ValueEventListener lobbyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_multiplayer);

        statusText = findViewById(R.id.status_text);
        continueBtn = findViewById(R.id.btn_continue_search);

        lobbyRef = FirebaseDatabase.getInstance().getReference("lobby");
        gamesRef = FirebaseDatabase.getInstance().getReference("games");

        playerId = String.valueOf(System.currentTimeMillis());
        playerName = getIntent().getStringExtra(MyConstants.playerName);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player" + (int) (Math.random() * 1000);
        }

        startSearch();

        continueBtn.setOnClickListener(v -> {
            continueBtn.setVisibility(View.GONE);
            startSearch(); // reinitiate search
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void startSearch() {
        statusText.setText("Searching for opponent...");
        lobbyRef.child(playerId).removeValue(); // ensure clean start

        lobbyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    DataSnapshot waiting = snapshot.getChildren().iterator().next();
                    String opponentId = waiting.getKey();
                    String opponentName = waiting.getValue(String.class);

                    String gameId = gamesRef.push().getKey();
                    if (gameId == null) {
                        Toast.makeText(MultiplayerActivity.this, "Unable to create game. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    gamesRef.child(gameId).child("players").child(playerId).setValue(playerName);
                    gamesRef.child(gameId).child("players").child(opponentId).setValue(opponentName);

                    for (int i = 0; i < 9; i++) {
                        gamesRef.child(gameId).child("board").child(String.valueOf(i)).setValue("");
                    }
                    gamesRef.child(gameId).child("turn").setValue("X");
                    gamesRef.child(gameId).child("turnStartTime").setValue(System.currentTimeMillis());
                    lobbyRef.child(opponentId).removeValue();

                    launchMatch(gameId);
                } else {
                    lobbyRef.child(playerId).setValue(playerName);
                    statusText.setText("Waiting for opponent...");

                    timeoutRunnable = () -> {
                        lobbyRef.child(playerId).removeValue();
                        statusText.setText("No opponent found.");
                        continueBtn.setVisibility(View.VISIBLE);
                    };
                    timeoutHandler.postDelayed(timeoutRunnable, WAIT_TIMEOUT_MS);

                    lobbyListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            if (!snap.exists()) {
                                gamesRef.orderByChild("players/" + playerId).equalTo(playerName)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot gamesSnap) {
                                                for (DataSnapshot g : gamesSnap.getChildren()) {
                                                    String gameId = g.getKey();
                                                    if (gameId != null) {
                                                        timeoutHandler.removeCallbacks(timeoutRunnable);
                                                        launchMatch(gameId);
                                                        break;
                                                    }
                                                }
                                            }

                                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                            }
                        }

                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    };
                    lobbyRef.child(playerId).addValueEventListener(lobbyListener);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MultiplayerActivity.this, "Lobby error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void launchMatch(String gameId) {
        Intent intent = new Intent(this, MatchGameActivity.class);
        intent.putExtra("gameId", gameId);
        intent.putExtra("playerId", playerId);
        intent.putExtra("playerName", playerName);
        startActivity(intent);
        if (lobbyListener != null) lobbyRef.child(playerId).removeEventListener(lobbyListener);
        timeoutHandler.removeCallbacksAndMessages(null);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lobbyRef.child(playerId).removeValue();
        timeoutHandler.removeCallbacksAndMessages(null);
        if (lobbyListener != null) lobbyRef.child(playerId).removeEventListener(lobbyListener);
    }
}
