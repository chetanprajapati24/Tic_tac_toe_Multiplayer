package com.example.tic_tac_toe_m.tictactoegame;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MultiplayerGameActivity extends AppCompatActivity {

    private String roomCode, playerSymbol;
    private DatabaseReference roomRef;
    private ImageView[] cells = new ImageView[9];
    private boolean isMyTurn = false;
    private String currentBoard = "---------"; // initial empty board

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game);

        // Get data from intent
        roomCode = getIntent().getStringExtra("roomCode");
        playerSymbol = getIntent().getStringExtra("playerSymbol");
        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);

        // Link ImageViews
        for (int i = 0; i < 9; i++) {
            int id = getResources().getIdentifier("img_" + (i + 1), "id", getPackageName());
            cells[i] = findViewById(id);
            int finalI = i;
            cells[i].setOnClickListener(v -> makeMove(finalI));
        }

        // Listen for board changes
        roomRef.child("board").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String board = snapshot.getValue(String.class);
                if (board != null && board.length() == 9) {
                    currentBoard = board;
                    updateBoard(board);
                    checkWinner(board);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Listen for turn changes
        roomRef.child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String turn = snapshot.getValue(String.class);
                isMyTurn = turn != null && turn.equals(playerSymbol);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void makeMove(int index) {
        if (!isMyTurn) {
            Toast.makeText(this, "Wait for your turn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read board and check if move is valid
        roomRef.child("board").get().addOnSuccessListener(snapshot -> {
            String board = snapshot.getValue(String.class);
            if (board == null || board.charAt(index) != '-') {
                Toast.makeText(this, "Invalid move", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update board
            StringBuilder updatedBoard = new StringBuilder(board);
            updatedBoard.setCharAt(index, playerSymbol.charAt(0));

            roomRef.child("board").setValue(updatedBoard.toString());
            roomRef.child("turn").setValue(playerSymbol.equals("X") ? "O" : "X");
        });
    }

    private void updateBoard(String board) {
        for (int i = 0; i < 9; i++) {
            char cell = board.charAt(i);
            if (cell == 'X') {
                cells[i].setImageResource(R.drawable.cross);
                cells[i].setEnabled(false);
            } else if (cell == 'O') {
                cells[i].setImageResource(R.drawable.circle);
                cells[i].setEnabled(false);
            } else {
                cells[i].setImageDrawable(null);
                cells[i].setEnabled(true);
            }
        }
    }

    private void checkWinner(String board) {
        int[][] winPositions = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // cols
                {0, 4, 8}, {2, 4, 6}             // diagonals
        };

        for (int[] pos : winPositions) {
            char a = board.charAt(pos[0]);
            char b = board.charAt(pos[1]);
            char c = board.charAt(pos[2]);

            if (a != '-' && a == b && b == c) {
                Toast.makeText(this, a + " wins!", Toast.LENGTH_LONG).show();
                roomRef.removeValue(); // delete room
                finish(); // exit activity
                return;
            }
        }

        if (!board.contains("-")) {
            Toast.makeText(this, "It's a Draw!", Toast.LENGTH_LONG).show();
            roomRef.removeValue();
            finish();
        }
    }
}
