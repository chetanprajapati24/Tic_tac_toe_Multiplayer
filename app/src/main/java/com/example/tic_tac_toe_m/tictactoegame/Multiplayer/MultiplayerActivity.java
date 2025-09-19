package com.example.tic_tac_toe_m.tictactoegame.Multiplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

public class MultiplayerActivity extends AppCompatActivity {

    private static final long WAIT_TIMEOUT_MS = 30000; // 30s wait before cancel

    private DatabaseReference lobbyRef;
    private DatabaseReference gamesRef;
    private String playerId;
    private String playerName;
    private TextView statusText;

    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;

    private ValueEventListener lobbyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer); // keep your existing lobby layout

        statusText = findViewById(R.id.status_text != 0 ? R.id.status_text : R.id.waiting_text);
        // try both common ids in case layout differs

        lobbyRef = FirebaseDatabase.getInstance().getReference("lobby");
        gamesRef = FirebaseDatabase.getInstance().getReference("games");

        playerId = String.valueOf(System.currentTimeMillis()); // simple id
        playerName = getIntent().getStringExtra(MyConstants.playerName);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player" + (int)(Math.random() * 1000);
        }

        startSearch();
    }

    private void startSearch() {
        statusText.setText("Searching for opponent...");
        // Try to pair immediately
        lobbyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    // take first waiting player
                    DataSnapshot waiting = snapshot.getChildren().iterator().next();
                    String opponentId = waiting.getKey();
                    String opponentName = waiting.getValue(String.class);

                    // create new game
                    String gameId = gamesRef.push().getKey();
                    if (gameId == null) {
                        Toast.makeText(MultiplayerActivity.this, "Unable to create game. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // write both players to game node
                    gamesRef.child(gameId).child("players").child(playerId).setValue(playerName);
                    gamesRef.child(gameId).child("players").child(opponentId).setValue(opponentName);

                    // init board
                    for (int i = 0; i < 9; i++) {
                        gamesRef.child(gameId).child("board").child(String.valueOf(i)).setValue("");
                    }
                    gamesRef.child(gameId).child("turn").setValue("X");
                    gamesRef.child(gameId).child("turnStartTime").setValue(System.currentTimeMillis());

                    // remove opponent from lobby
                    lobbyRef.child(opponentId).removeValue();

                    launchMatch(gameId);
                } else {
                    // no one waiting — put self in lobby and wait
                    lobbyRef.child(playerId).setValue(playerName);
                    statusText.setText("Waiting for opponent...");

                    // set up timeout
                    timeoutRunnable = () -> {
                        // timeout reached -> remove from lobby and stop
                        lobbyRef.child(playerId).removeValue();
                        Toast.makeText(MultiplayerActivity.this, "No opponent found. Try again later.", Toast.LENGTH_SHORT).show();
                        finish(); // go back to previous screen
                    };
                    timeoutHandler.postDelayed(timeoutRunnable, WAIT_TIMEOUT_MS);

                    // listen for being removed from lobby (which indicates pairing happened)
                    lobbyListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            if (!snap.exists()) {
                                // removed from lobby — find the game that contains this player
                                // Search games where this player exists
                                gamesRef.orderByChild("players/" + playerId).equalTo(playerName)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot gamesSnap) {
                                                for (DataSnapshot g : gamesSnap.getChildren()) {
                                                    String gameId = g.getKey();
                                                    if (gameId != null) {
                                                        // cancel timeout and listener, then launch
                                                        timeoutHandler.removeCallbacks(timeoutRunnable);
                                                        launchMatch(gameId);
                                                    }
                                                    break;
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
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
        // cleanup: remove any lobby listener and callbacks
        if (lobbyListener != null) lobbyRef.child(playerId).removeEventListener(lobbyListener);
        timeoutHandler.removeCallbacksAndMessages(null);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ensure removal from lobby if we leave early
        lobbyRef.child(playerId).removeValue();
        timeoutHandler.removeCallbacksAndMessages(null);
        if (lobbyListener != null) lobbyRef.child(playerId).removeEventListener(lobbyListener);
    }
}
