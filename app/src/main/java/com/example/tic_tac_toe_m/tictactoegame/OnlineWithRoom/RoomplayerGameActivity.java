package com.example.tic_tac_toe_m.tictactoegame.OnlineWithRoom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tic_tac_toe_m.R;
import com.example.tic_tac_toe_m.tictactoegame.AI.AiGameActivity;
import com.example.tic_tac_toe_m.tictactoegame.OfflineGameMenuActivity;
import com.example.tic_tac_toe_m.tictactoegame.Setting.SettingsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class RoomplayerGameActivity extends AppCompatActivity {

    private String playerSymbol;
    private Dialog quitdialog;
    private boolean isLeaving = false; // new flag
    private TextView playerOneNameTxt, playerTwoNameTxt;

    private DatabaseReference roomRef;
    private final ImageView[] cells = new ImageView[9];
    private boolean isMyTurn = false;
    private String currentBoard = "---------";
    private GifImageView settingsGifView;
    private TextView timerTxtPlayerOne, timerTxtPlayerTwo;
    private TextView playerOneScoreTxt, playerTwoScoreTxt;
    private TextView nextRoundCountdown;

    private int playerOneScore = 0, playerTwoScore = 0;
    private Dialog celebrateDialog, drawDialog;
    private LinearLayout waitingLayout;
    private CountDownTimer turnTimer;
    private final long turnDuration = 20000; // 20 sec
    private boolean roundActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_room_player_game);

        String playerName = getIntent().getStringExtra("Player_Name");
        String roomCode = getIntent().getStringExtra("roomCode");
        playerSymbol = getIntent().getStringExtra("playerSymbol");
        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);

        Toast.makeText(this, "You are " + playerName + " (" + playerSymbol + ")", Toast.LENGTH_LONG).show();

        playerOneNameTxt = findViewById(R.id.player_one_name_txt);
        playerTwoNameTxt = findViewById(R.id.player_two_name_txt);

        timerTxtPlayerOne = findViewById(R.id.textViewTimer1);
        timerTxtPlayerTwo = findViewById(R.id.textViewTimer);
        playerOneScoreTxt = findViewById(R.id.player_one_win_count_txt);
        playerTwoScoreTxt = findViewById(R.id.player_two_won_txt);
        settingsGifView = findViewById(R.id.room_game_seting_gifview);
        waitingLayout = findViewById(R.id.waiting_layout);
        nextRoundCountdown = findViewById(R.id.next_round_countdown);
        quitdialog = new Dialog(this);



       // Drawable drawable = settingsGifView.getDrawable();
      //  if (drawable instanceof Animatable) ((Animatable) drawable).stop();

        for (int i = 0; i < 9; i++) {
            int id = getResources().getIdentifier("img_" + (i + 1), "id", getPackageName());
            cells[i] = findViewById(id);
            int finalI = i;
            cells[i].setOnClickListener(v -> makeMove(finalI));
        }

        celebrateDialog = new Dialog(this);
        drawDialog = new Dialog(this);

        settingsGifView.setOnClickListener(v -> {
            startGif(settingsGifView);
            new Handler().postDelayed(() -> {
                stopGif(settingsGifView);
                startActivity(new Intent(this, SettingsActivity.class));
            }, 750);
        });


        setupFirebaseListeners();
    }

    private void startGif(GifImageView gifView) {
        Drawable drawable = gifView.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    private void stopGif(GifImageView gifView) {
        Drawable drawable = gifView.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).stop();
        }
    }

    private void setupFirebaseListeners() {
        // Players joined
       /* roomRef.child("player1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("X") && snapshot.hasChild("O")) {
                    waitingLayout.setVisibility(View.GONE);
                    enableBoard();
                    startTurnTimer();
                } else {
                    waitingLayout.setVisibility(View.VISIBLE);
                    stopTurnTimer();
                    disableBoard();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });*/

        //Player join & names -----
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean player1Joined = snapshot.child("player1").hasChild("X");
                boolean player2Joined = snapshot.child("player2").hasChild("O");
                // Get player names
                String player1Name = player1Joined ? snapshot.child("player1").child("X").getValue(String.class) : "Waiting...";
                String player2Name = player2Joined ? snapshot.child("player2").child("O").getValue(String.class) : "Waiting...";

                // Set the TextViews
                playerOneNameTxt.setText(player1Name);
                playerTwoNameTxt.setText(player2Name);

                if (player1Joined && player2Joined) {
                    waitingLayout.setVisibility(View.GONE);
                    enableBoard();
                    startTurnTimer();
                } else {
                    waitingLayout.setVisibility(View.VISIBLE);
                    stopTurnTimer();
                    disableBoard();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        // Board updates
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

        // Firebase listener for turn changes
        roomRef.child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String turn = snapshot.getValue(String.class);
                if (turn == null) return;

                boolean wasMyTurn = isMyTurn;           // save previous state
                isMyTurn = playerSymbol.equals(turn);   // update current state

                if (!roundActive) return;

                // Only start timer if turn just changed to me
                if (isMyTurn && !wasMyTurn) {
                    startTurnTimer();
                } else if (!isMyTurn && wasMyTurn) {
                    // Stop my timer when turn changes to opponent
                    stopTurnTimer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Ready flags for next round
        roomRef.addValueEventListener(new ValueEventListener() {
            private boolean roundStarting = false;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean hostReady = snapshot.child("hostReady").getValue(Boolean.class);
                Boolean guestReady = snapshot.child("guestReady").getValue(Boolean.class);
                if (Boolean.TRUE.equals(hostReady) && Boolean.TRUE.equals(guestReady) && !roundStarting) {
                    roundStarting = true;
                    disableBoard();
                    nextRoundCountdown.setVisibility(View.VISIBLE);

                    new CountDownTimer(3000, 1000) {
                        int count = 3;
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onTick(long millisUntilFinished) {
                            nextRoundCountdown.setText("Next round starts in " + count-- + "s...");
                        }
                        @Override
                        public void onFinish() {
                            nextRoundCountdown.setVisibility(View.GONE);
                            startNextRound();
                            roundStarting = false;
                        }
                    }.start();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Scores
        roomRef.child("scores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long p1Score = snapshot.child("X").getValue(Long.class);
                Long p2Score = snapshot.child("O").getValue(Long.class);
                if (p1Score != null) playerOneScore = p1Score.intValue();
                if (p2Score != null) playerTwoScore = p2Score.intValue();
                playerOneScoreTxt.setText(String.valueOf(playerOneScore));
                playerTwoScoreTxt.setText(String.valueOf(playerTwoScore));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Status
        roomRef.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if ("left".equals(status) && !isLeaving) { // ignore if it's me leaving
                    Toast.makeText(RoomplayerGameActivity.this, "Opponent left!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RoomplayerGameActivity.this, OfflineGameMenuActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Inside onCreate()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back behavior or just close activity
                quitDialogfun();
            }
        });

    }

    private void makeMove(int index) {
        if (!isMyTurn || !roundActive) {
            Toast.makeText(this, "Wait for your turn", Toast.LENGTH_SHORT).show();
            return;
        }
        roomRef.child("board").get().addOnSuccessListener(snapshot -> {
            String board = snapshot.getValue(String.class);
            if (board == null || board.charAt(index) != '-') {
                Toast.makeText(this, "Invalid move", Toast.LENGTH_SHORT).show();
                return;
            }
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
                cells[i].setImageResource(R.drawable.xbg);
                cells[i].setEnabled(false);
            } else if (cell == 'O') {
                cells[i].setImageResource(R.drawable.obg);
                cells[i].setEnabled(false);
            } else {
                cells[i].setImageDrawable(null);
                cells[i].setBackgroundResource(0);
                cells[i].setEnabled(true);
            }
        }
    }

    private void checkWinner(String board) {
        int[][] winPositions = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] pos : winPositions) {
            char a = board.charAt(pos[0]), b = board.charAt(pos[1]), c = board.charAt(pos[2]);
            if (a != '-' && a == b && b == c) {
                highlightWinningCells(pos, a);
                handleWin(a);
                return;
            }
        }
        if (!board.contains("-")) showDrawDialog();
    }

    private void highlightWinningCells(int[] pos, char symbol) {
        int bg = (symbol == 'X') ? R.drawable.cross_background : R.drawable.circle_background;
        for (int index : pos) cells[index].setBackgroundResource(bg);
    }

    private void handleWin(char winner) {
        if (!roundActive) return;
        roundActive = false;
        stopTurnTimer();

        if (playerSymbol.equals(String.valueOf(winner))) {
            DatabaseReference scoreRef = roomRef.child("scores").child(String.valueOf(winner));
            scoreRef.get().addOnSuccessListener(snapshot -> {
                Long current = snapshot.getValue(Long.class);
                int newScore = (current != null ? current.intValue() : 0) + 1;
                scoreRef.setValue(newScore);
            });
        }
        showCelebrateDialog(String.valueOf(winner));
    }

    private void showCelebrateDialog(String winner) {
        if (isFinishing() || isDestroyed()) return; // Prevent showing dialog if activity is gone

        roundActive = false;
        stopTurnTimer();


        celebrateDialog = new Dialog(this);
        celebrateDialog.setContentView(R.layout.celebrate_dialog);
        celebrateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        celebrateDialog.setCanceledOnTouchOutside(false);
        LottieAnimationView animationView = celebrateDialog.findViewById(R.id.celebrate_animationView);
        LinearLayout container = celebrateDialog.findViewById(R.id.container_1);
        ImageView winnerImg = celebrateDialog.findViewById(R.id.offline_game_player_img);
        Button quitBtn = celebrateDialog.findViewById(R.id.offline_game_quit_btn);
        Button continueBtn = celebrateDialog.findViewById(R.id.offline_game_continue_btn);

        new Handler().postDelayed(() -> {
            animationView.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            winnerImg.setImageResource(winner.equals("X") ? R.drawable.xbg : R.drawable.obg);
            TextView winnerNameTxt = celebrateDialog.findViewById(R.id.playerNameSet);
            String winnerName = winner.equals("X") ? playerOneNameTxt.getText().toString()
                    : playerTwoNameTxt.getText().toString();
            winnerNameTxt.setText(winnerName + " Wins!");
        }, 2000);

        quitBtn.setOnClickListener(v -> {
            roomRef.child("status").setValue("left");
            celebrateDialog.dismiss();
            Intent intent = new Intent(this, OfflineGameMenuActivity.class);
            startActivity(intent);
            finish();
        });

        continueBtn.setOnClickListener(v -> {
            celebrateDialog.dismiss();
            if (playerSymbol.equals("X")) roomRef.child("hostReady").setValue(true);
            else roomRef.child("guestReady").setValue(true);
            disableBoard();
            Toast.makeText(this, "Waiting for opponent...", Toast.LENGTH_SHORT).show();
        });

        celebrateDialog.show();
    }

    private void showDrawDialog() {
        roundActive = false;
        stopTurnTimer();
        drawDialog.setContentView(R.layout.draw_dialog);
        Objects.requireNonNull(drawDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        drawDialog.setCanceledOnTouchOutside(false);
        Button quitBtn = drawDialog.findViewById(R.id.offline_game_draw_quit_btn);
        Button continueBtn = drawDialog.findViewById(R.id.offline_game_draw_continue_btn);

        quitBtn.setOnClickListener(v -> {
            roomRef.child("status").setValue("left");
            roomRef.removeValue();
            drawDialog.dismiss();
            Intent intent = new Intent(this, OfflineGameMenuActivity.class);
            startActivity(intent);
            finish();
        });

        continueBtn.setOnClickListener(v -> {
            drawDialog.dismiss();
            if (playerSymbol.equals("X")) roomRef.child("hostReady").setValue(true);
            else roomRef.child("guestReady").setValue(true);
            disableBoard();
            Toast.makeText(this, "Waiting for opponent...", Toast.LENGTH_SHORT).show();
        });

        drawDialog.show();
    }

    private void resetBoardOnly() {
        currentBoard = "---------";
        roomRef.child("board").setValue(currentBoard);
        for (ImageView cell : cells) {
            cell.setImageDrawable(null);
            cell.setBackgroundResource(0);
            cell.setEnabled(true);
        }
    }

    private void startNextRound() {
        roundActive = true;              // allow moves again
        resetBoardOnly();                // reset cells and board
        stopTurnTimer();                 // stop any old timer

        // Reset ready flags
        roomRef.child("hostReady").setValue(false);
        roomRef.child("guestReady").setValue(false);

        // Start with player X's turn if X
        if ("X".equals(playerSymbol)) {
            roomRef.child("turn").setValue("X");
            roomRef.child("turnStartTime").setValue(System.currentTimeMillis()); // reset timer start
        }

        enableBoard();

        // Immediately start timer if it is your turn
        if (isMyTurn) startTurnTimer();
    }

    private void disableBoard() {
        for (ImageView cell : cells) cell.setEnabled(false);
    }

    private void enableBoard() {
        for (int i = 0; i < 9; i++) {
            cells[i].setEnabled(currentBoard.charAt(i) == '-');
        }
    }

/*
    private void startTurnTimer() {
        stopTurnTimer(); // cancel previous timer

        if (!roundActive || !isMyTurn) return; // safety check

        // Map the timer TextViews dynamically
        TextView myTimer = playerSymbol.equals("X") ? timerTxtPlayerOne : timerTxtPlayerTwo;
        TextView oppTimer = playerSymbol.equals("X") ? timerTxtPlayerTwo : timerTxtPlayerOne;

        myTimer.setText("Time left: " + (turnDuration / 1000) + "s");
        oppTimer.setText("");

        turnTimer = new CountDownTimer(turnDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                myTimer.setText("Time left: " + seconds + "s");
            }

            @Override
            public void onFinish() {
                if (!roundActive) return;
                // Auto-lose if time runs out
                char winner = playerSymbol.equals("X") ? 'O' : 'X';
                handleWin(winner);
            }
        }.start();
    }

    private void stopTurnTimer() {
        if (turnTimer != null) turnTimer.cancel();
        timerTxtPlayerOne.setText("");
        timerTxtPlayerTwo.setText("");
    }
*/


    private void leaveGame() {
        isLeaving = true; // mark that I am leaving
        if (celebrateDialog != null && celebrateDialog.isShowing()) celebrateDialog.dismiss();
        if (drawDialog != null && drawDialog.isShowing()) drawDialog.dismiss();

        if (roomRef != null) {
            roomRef.child("status").setValue("left"); // notify Firebase
        }
        Intent intent = new Intent(this, OfflineGameMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void    quitDialogfun() {


        quitdialog.setContentView(R.layout.quit_dialog);
        Objects.requireNonNull(quitdialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        quitdialog.setCanceledOnTouchOutside(false);


        Button quitBtn = quitdialog.findViewById(R.id.quit_btn);
        Button continueBtn = quitdialog.findViewById(R.id.continue_btn);

        quitBtn.setOnClickListener(v -> {
            leaveGame();
            quitdialog.dismiss();
            Intent intent = new Intent(this, OfflineGameMenuActivity.class);
            startActivity(intent);
            finish();
        });

        continueBtn.setOnClickListener(v -> quitdialog.dismiss());
        quitdialog.show();
    }

    @SuppressLint({"MissingSuperCall", "GestureBackNavigation"})
    @Override
    public void onBackPressed() {
       quitDialogfun();
    }

    @SuppressLint("SetTextI18n")
    private void startTurnTimer(long durationMillis) {
        stopTurnTimer(); // cancel any existing timer

        if (!roundActive) return;

        final TextView myTimer = "X".equals(playerSymbol) ? timerTxtPlayerOne : timerTxtPlayerTwo;
        final TextView oppTimer = "X".equals(playerSymbol) ? timerTxtPlayerTwo : timerTxtPlayerOne;

        if (!isMyTurn) {
            // Not my turn → opponent's timer is running elsewhere
            myTimer.setText("Wait Your Turn");
            oppTimer.setText("");
            return;
        }

        myTimer.setText("Time left: " + (durationMillis / 1000) + "s");
        oppTimer.setText("Wait Your Turn");

        turnTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!roundActive) return;
                int seconds = (int) (millisUntilFinished / 1000);
                myTimer.setText("Time left: " + seconds + "s");
                oppTimer.setText("Wait Your Turn");
            }

            @Override
            public void onFinish() {
                if (!roundActive) return;
                stopTurnTimer();
                char winner = "X".equals(playerSymbol) ? 'O' : 'X';
                handleWin(winner);
            }
        }.start();
    }
    private void startTurnTimer() {
        startTurnTimer(turnDuration); // full duration for new turn
    }
    @SuppressLint("SetTextI18n")
    private void stopTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
        // Clear safely → default both to "Wait Your Turn"
        if (timerTxtPlayerOne != null) timerTxtPlayerOne.setText("Wait Your Turn");
        if (timerTxtPlayerTwo != null) timerTxtPlayerTwo.setText("Wait Your Turn");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the running timer to avoid background work & leaks
        stopTurnTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!roundActive) return;

        if (isMyTurn) {
            // Get turn start time from Firebase
            roomRef.child("turnStartTime").get().addOnSuccessListener(snapshot -> {
                Long startTime = snapshot.getValue(Long.class);
                if (startTime != null) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    long remaining = turnDuration - elapsed;
                    if (remaining > 0) {
                        startTurnTimer(remaining); // resume timer with remaining time
                    } else {
                        // Time already expired → auto lose
                        char winner = playerSymbol.equals("X") ? 'O' : 'X';
                        handleWin(winner);
                    }
                } else {
                    // fallback, start full timer if no timestamp
                    startTurnTimer();
                }
            });
        } else {
            // Opponent turn → just show "Wait Your Turn"
            TextView myTimer = "X".equals(playerSymbol) ? timerTxtPlayerOne : timerTxtPlayerTwo;
            myTimer.setText("Wait Your Turn");
            TextView oppTimer = "X".equals(playerSymbol) ? timerTxtPlayerTwo : timerTxtPlayerOne;
            oppTimer.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTurnTimer();
        if (roomRef != null) roomRef.child("status").setValue("left");
    }
}
