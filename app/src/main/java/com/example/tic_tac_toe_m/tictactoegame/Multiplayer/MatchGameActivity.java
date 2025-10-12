package com.example.tic_tac_toe_m.tictactoegame.Multiplayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
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
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tic_tac_toe_m.R;
import com.example.tic_tac_toe_m.tictactoegame.OfflineGameMenuActivity;
import com.example.tic_tac_toe_m.tictactoegame.Setting.MyServices;
import com.example.tic_tac_toe_m.tictactoegame.Setting.SettingsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class MatchGameActivity extends AppCompatActivity {

    private String gameId, playerId, playerName;
    private String mySymbol, opponentSymbol;
    private boolean isMyTurn = false;
    private boolean isDialogShown = false;


    private DatabaseReference gameRef;
    private final ImageView[] cells = new ImageView[9];
    private String currentBoard = "---------";

    // UI
    private TextView playerOneNameTxt, playerTwoNameTxt;
    private TextView playerOneScoreTxt, playerTwoScoreTxt;
    private TextView timerTxtLeft, timerTxtRight;
    private ImageView playerOneImg, playerTwoImg;
    private TextView waitingTxt;
    private LinearLayout waitingLayout;
    private GifImageView settingsGifView;

    // Scores
    private int playerOneScore = 0, playerTwoScore = 0;

    // Dialogs
    private Dialog celebrateDialog, drawDialog,quitdialog;

    // Timer
    private CountDownTimer turnTimer;
    private final long TURN_DURATION_MS = 20000; // 20s
    private long timeLeft;
    private long serverTimeOffset = 0;
    private boolean roundActive = true;
    private Handler timerHandler = new Handler();
    private Runnable turnRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_match_game);

        // Get intent extras
        gameId = getIntent().getStringExtra("gameId");
        playerId = getIntent().getStringExtra("playerId");
        playerName = getIntent().getStringExtra("playerName");
        if (playerName == null || playerName.isEmpty()) playerName = "Player" + (int)(Math.random() * 1000);

        gameRef = FirebaseDatabase.getInstance().getReference("games").child(gameId);

        // Bind UI
        playerOneNameTxt = findViewById(R.id.player_one_name_txt);
        playerTwoNameTxt = findViewById(R.id.player_two_name_txt);
        playerOneScoreTxt = findViewById(R.id.player_one_win_count_txt);
        playerTwoScoreTxt = findViewById(R.id.player_two_won_txt);
        timerTxtLeft = findViewById(R.id.textViewTimer1);
        timerTxtRight = findViewById(R.id.textViewTimer);
        playerOneImg = findViewById(R.id.player_one_img);
        playerTwoImg = findViewById(R.id.player_two_img);
        waitingTxt = findViewById(R.id.waiting_text);
        waitingLayout = findViewById(R.id.waiting_layout);
        settingsGifView=findViewById(R.id.multiplyer_game_seting_gifview);

        settingsGifView.setOnClickListener(v -> {
            startGif(settingsGifView);
            new Handler().postDelayed(() -> {
                stopGif(settingsGifView);
                startActivity(new Intent(this, SettingsActivity.class));
            }, 750);
        });

        // Link cells
        for (int i = 0; i < 9; i++) {
            int id = getResources().getIdentifier("img_" + (i + 1), "id", getPackageName());
            cells[i] = findViewById(id);
            final int finalI = i;
            cells[i].setOnClickListener(v -> onCellClicked(finalI));
        }

        celebrateDialog = new Dialog(this);
        drawDialog = new Dialog(this);
        quitdialog=new Dialog(this);

        // Next round listener
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean p1Ready = snapshot.child("player1_readyForNextRound").getValue(Boolean.class);
                Boolean p2Ready = snapshot.child("player2_readyForNextRound").getValue(Boolean.class);
                if (Boolean.TRUE.equals(p1Ready) && Boolean.TRUE.equals(p2Ready)) {
                    // Clear flags
                    gameRef.child("player1_readyForNextRound").removeValue();
                    gameRef.child("player2_readyForNextRound").removeValue();

                    startNextRound();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Add player
        gameRef.child("players").child(playerId).setValue(playerName);

        // Listen players
        gameRef.child("players").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                if (count == 2) startPreGameCountdown();

                if (count > 0) {
                    DataSnapshot first = snapshot.getChildren().iterator().next();
                    String firstKey = first.getKey();
                    String firstName = first.getValue(String.class);

                    playerOneNameTxt.setText(firstName != null ? firstName : "Player");
                    if (count > 1) {
                        for (DataSnapshot s : snapshot.getChildren()) {
                            if (!s.getKey().equals(firstKey)) {
                                playerTwoNameTxt.setText(s.getValue(String.class));
                                break;
                            }
                        }
                    } else playerTwoNameTxt.setText("Waiting...");

                    // Assign symbols
                    if (playerId.equals(firstKey)) {
                        mySymbol = "X";
                        opponentSymbol = "O";
                    } else {
                        mySymbol = "O";
                        opponentSymbol = "X";
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Initialize board
        gameRef.child("board").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    for (int i = 0; i < 9; i++) gameRef.child("board").child(String.valueOf(i)).setValue("");
                    gameRef.child("turn").setValue("X");
                    gameRef.child("turnStartTime").setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Board updates
        gameRef.child("board").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder sb = new StringBuilder("---------");
                for (int i = 0; i < 9; i++) {
                    String v = snapshot.child(String.valueOf(i)).getValue(String.class);
                    if (v != null && !v.isEmpty()) sb.setCharAt(i, v.charAt(0));
                }
                currentBoard = sb.toString();
                updateBoard(currentBoard);
                checkWinner(currentBoard);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Turn updates
        gameRef.child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String turn = snapshot.getValue(String.class);
                isMyTurn = (turn != null && turn.equals(mySymbol));
                updateTimerTexts();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Turn start time
        gameRef.child("turnStartTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long serverStartTime = snapshot.getValue(Long.class);
                if (serverStartTime != null && !celebrateDialog.isShowing() && !drawDialog.isShowing())
                    startTurnTimer(serverStartTime);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Winner
       /* gameRef.child("winner").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String winner = snapshot.getValue(String.class);
                if (winner == null || isDialogShown) return; // <-- prevent double show

                isDialogShown = true; // mark dialog as showing

                if (winner.equals("draw")) showDrawDialog();
                else handleWin(winner.charAt(0));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });*/
        gameRef.child("winner").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String winner = snapshot.getValue(String.class);
                if (winner == null) return;

                if (isDialogShown) return; // only show once per round
                isDialogShown = true;

                stopTurnTimer();

                if (winner.equals("draw")) showDrawDialog();
                else handleWin(winner.charAt(0));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

// Example in checkWinner
        gameRef.child("currentRound").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long round = currentData.getValue(Long.class);
                if (round == null) round = 0L;
                gameRef.child("winnerRound").setValue(round);
                currentData.setValue(round + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {}
        });

        // Status
        gameRef.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if ("left".equals(status)) {
                    Toast.makeText(MatchGameActivity.this, "Opponent left the game", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Scores
        gameRef.child("scores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long x = snapshot.child("X").getValue(Long.class);
                Long o = snapshot.child("O").getValue(Long.class);
                playerOneScore = x != null ? x.intValue() : 0;
                playerTwoScore = o != null ? o.intValue() : 0;
                playerOneScoreTxt.setText(String.valueOf(playerOneScore));
                playerTwoScoreTxt.setText(String.valueOf(playerTwoScore));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Server time offset
        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long offset = snapshot.getValue(Long.class);
                if (offset != null) serverTimeOffset = offset;
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        //  Back press handle onCreate()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back behavior or just close activity
                quitDialogfun();
            }
        });


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
   /* @SuppressLint("SetTextI18n")
    private void updateTimerTexts() {
        if (turnTimer != null) turnTimer.cancel();
        if (isMyTurn) {
            timerTxtLeft.setText("Time left: " + (int)(timeLeft/1000) + "s");
            timerTxtRight.setText("Wait Your Turn");
            enableBoard();
            startTurnTimer(System.currentTimeMillis() + serverTimeOffset - (TURN_DURATION_MS - timeLeft));
        } else {
            timerTxtRight.setText("Time left: " + (int)(timeLeft/1000) + "s");
            timerTxtLeft.setText("Wait Your Turn");
            disableBoard();
            stopTurnTimer();
        }
    }
*/
   @SuppressLint("SetTextI18n")
   private void updateTimerTexts() {
       if (isMyTurn) {
           timerTxtLeft.setText("Time left: " + (int)(timeLeft / 1000) + "s");
           timerTxtRight.setText("Wait Your Turn");
           enableBoard();
       } else {
           timerTxtLeft.setText("Wait Your Turn");
           timerTxtRight.setText("Time left: " + (int)(timeLeft / 1000) + "s");
           disableBoard();
       }
   }

    private void startPreGameCountdown() {
        stopTurnTimer();
        final int countdownSeconds = 3;
        final TextView countdownTxt = findViewById(R.id.textViewCountdown);
        countdownTxt.setVisibility(View.VISIBLE);

        new CountDownTimer(countdownSeconds * 1000, 1000) {
            int secondsLeft = countdownSeconds;

            @Override
            public void onTick(long millisUntilFinished) {
                countdownTxt.setText(String.valueOf(secondsLeft));
                secondsLeft--;
            }

            @Override
            public void onFinish() {
                countdownTxt.setVisibility(View.GONE);
                isMyTurn = mySymbol.equals("X");
                gameRef.child("turnStartTime").setValue(ServerValue.TIMESTAMP);
            }
        }.start();
    }

    private void onCellClicked(int position) {
        if (!isMyTurn || currentBoard.charAt(position) != '-') return;
        gameRef.child("board").child(String.valueOf(position)).setValue(mySymbol);
        gameRef.child("turn").setValue(opponentSymbol);
        gameRef.child("turnStartTime").setValue(ServerValue.TIMESTAMP);
    }

    private void updateBoard(String board) {
        for (int i = 0; i < 9; i++) {
            char cell = board.charAt(i);
            if (cell == 'X') {
                if (cells[i].getDrawable() == null) {
                    cells[i].setImageResource(R.drawable.xbg);
                    if (MyServices.SOUND_CHECK) {
                        MediaPlayer mp = MediaPlayer.create(this, R.raw.x);
                        mp.setOnCompletionListener(MediaPlayer::release);
                        mp.start();
                    }
                    MyServices.vibrate(this, 200);
                }
                cells[i].setEnabled(false);
            } else if (cell == 'O') {
                if (cells[i].getDrawable() == null) {
                    cells[i].setImageResource(R.drawable.obg);
                    if (MyServices.SOUND_CHECK) {
                        MediaPlayer mp = MediaPlayer.create(this, R.raw.o);
                        mp.setOnCompletionListener(MediaPlayer::release);
                        mp.start();
                    }
                    MyServices.vibrate(this, 200);
                }
                cells[i].setEnabled(false);
            } else {
                cells[i].setImageDrawable(null);
                cells[i].setBackgroundResource(0);
                cells[i].setEnabled(true);
            }
        }
    }

    private void checkWinner(String board) {
        int[][] winPositions = {
                {0,1,2},{3,4,5},{6,7,8},
                {0,3,6},{1,4,7},{2,5,8},
                {0,4,8},{2,4,6}
        };
        for (int[] pos : winPositions) {
            char a = board.charAt(pos[0]);
            char b = board.charAt(pos[1]);
            char c = board.charAt(pos[2]);
            if (a != '-' && a == b && b == c) {
                highlightWinningCells(pos, a);
                gameRef.child("winner").setValue(String.valueOf(a));
                return;
            }
        }
        if (!board.contains("-")) {
            gameRef.child("winner").setValue("draw");
        }
    }

    private void highlightWinningCells(int[] pos, char symbol) {
        int bg = (symbol == 'X') ? R.drawable.cross_background : R.drawable.circle_background;
        for (int index : pos) cells[index].setBackgroundResource(bg);
    }

/*    private void handleWin(char winner) {
        // Only let the winner's own device increment their score
        if ((winner == 'X' && mySymbol.equals("X")) || (winner == 'O' && mySymbol.equals("O"))) {
            if (winner == 'X') {
                gameRef.child("scores").child("X").setValue(playerOneScore + 1);
            } else {
                gameRef.child("scores").child("O").setValue(playerTwoScore + 1);
            }
        }

        stopTurnTimer();
        showCelebrateDialog(String.valueOf(winner));
    }*/

    private void handleWin(char winner) {
        // Only update score if dialog wasn't already shown
        if ((winner == 'X' && mySymbol.equals("X")) || (winner == 'O' && mySymbol.equals("O"))) {
            gameRef.child("scores").child(String.valueOf(winner))
                    .runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Long currentScore = currentData.getValue(Long.class);
                            if (currentScore == null) currentScore = 0L;
                            currentData.setValue(currentScore + 1);
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {}
                    });
        }

        showCelebrateDialog(String.valueOf(winner));
    }


    private void showCelebrateDialog(String winner) {
        stopTurnTimer();
        setReadyForNextRound();
        celebrateDialog.setContentView(R.layout.celebrate_dialog);
        Objects.requireNonNull(celebrateDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        }, 2000);

        quitBtn.setOnClickListener(v -> {
            gameRef.removeValue();
            celebrateDialog.dismiss();
            startActivity(new Intent(this, OfflineGameMenuActivity.class));
            finish();
        });

        continueBtn.setOnClickListener(v -> {
            continueRoundFromDialog();
            celebrateDialog.dismiss();
        });

        celebrateDialog.show();
    }

    private void showDrawDialog() {
        stopTurnTimer();
        drawDialog.setContentView(R.layout.draw_dialog);
        Objects.requireNonNull(drawDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        drawDialog.setCanceledOnTouchOutside(false);

        Button quitBtn = drawDialog.findViewById(R.id.offline_game_draw_quit_btn);
        Button continueBtn = drawDialog.findViewById(R.id.offline_game_draw_continue_btn);

        quitBtn.setOnClickListener(v -> {
            gameRef.removeValue();
            drawDialog.dismiss();
            startActivity(new Intent(this, OfflineGameMenuActivity.class));
            finish();
        });

        continueBtn.setOnClickListener(v -> {
            continueRoundFromDialog();
            drawDialog.dismiss();
        });

        drawDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void resetBoard() {
        for (int i = 0; i < 9; i++) gameRef.child("board").child(String.valueOf(i)).setValue("");
        currentBoard = "---------";
        for (ImageView cell : cells) {
            cell.setImageDrawable(null);
            cell.setBackgroundResource(0);
            cell.setEnabled(true);
        }
        gameRef.child("turn").setValue("X");
        gameRef.child("turnStartTime").setValue(ServerValue.TIMESTAMP);
        stopTurnTimer();
    }

    private void setReadyForNextRound() {
        if (mySymbol.equals("X")) gameRef.child("player1_readyForNextRound").setValue(true);
        else gameRef.child("player2_readyForNextRound").setValue(true);
    }

    private void startNextRound() {
        isDialogShown = false;
        gameRef.child("winner").removeValue();
        resetBoard();

        gameRef.child("player1_readyForNextRound").setValue(false);
        gameRef.child("player2_readyForNextRound").setValue(false);
        waitingLayout.setVisibility(View.GONE);
    }

    private void continueRoundFromDialog() {
        setReadyForNextRound();
        disableBoard();
        waitingLayout.setVisibility(View.VISIBLE);
    }

    private void disableBoard() {
        for (ImageView cell : cells) cell.setEnabled(false);
    }
    private void enableBoard() {
        for (int i = 0; i < 9; i++) {
            if(currentBoard.charAt(i) == '-') {
                cells[i].setEnabled(true);
            } else {
                cells[i].setEnabled(false);
            }
        }
    }

   /* @SuppressLint("SetTextI18n")
    private void startTurnTimer(long turnStartTimeFromFirebase) {
        stopTurnTimer(); // cancel any existing timer

        if (!roundActive) return;

        // figure out how much time is left from Firebase timestamp
        long currentServerTime = System.currentTimeMillis() + serverTimeOffset;
        long elapsed = currentServerTime - turnStartTimeFromFirebase;
        long durationMillis = TURN_DURATION_MS - elapsed;

        if (durationMillis <= 0) {
            // time already expired → auto win for opponent
            char winner = mySymbol.equals("X") ? 'O' : 'X';
            gameRef.child("winner").setValue(String.valueOf(winner));
            return;
        }

        final TextView myTimer = "X".equals(mySymbol) ? timerTxtLeft : timerTxtRight;
        final TextView oppTimer = "X".equals(mySymbol) ? timerTxtRight : timerTxtLeft;

        if (!isMyTurn) {
            // Not my turn → opponent's timer is running
            myTimer.setText("Wait Your Turn");
            oppTimer.setText("Opponent's move...");
            return;
        }

        // My turn → start countdown
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
                char winner = mySymbol.equals("X") ? 'O' : 'X';
                gameRef.child("winner").setValue(String.valueOf(winner));
            }
        }.start();
    }*/

/*   @SuppressLint("SetTextI18n")
   private void startTurnTimer(long turnStartTimeFromFirebase) {
       stopTurnTimer(); // cancel existing

       if (!roundActive) return;

       // calculate remaining time from Firebase
       long currentServerTime = System.currentTimeMillis() + serverTimeOffset;
       long elapsed = currentServerTime - turnStartTimeFromFirebase;
       long durationMillis = TURN_DURATION_MS - elapsed;

       if (durationMillis <= 0) {
           // timer already expired → opponent wins
           char winner = mySymbol.equals("X") ? 'O' : 'X';
           gameRef.child("winner").setValue(String.valueOf(winner));
           return;
       }

       final TextView myTimer = "X".equals(mySymbol) ? timerTxtLeft : timerTxtRight;
       final TextView oppTimer = "X".equals(mySymbol) ? timerTxtRight : timerTxtLeft;

       if (isMyTurn) {
           // My turn → my timer decreases, opponent timer shows fixed full time
           turnTimer = new CountDownTimer(durationMillis, 1000) {
               @Override
               public void onTick(long millisUntilFinished) {
                   if (!roundActive) return;
                   int seconds = (int) (millisUntilFinished / 1000);

                   myTimer.setText("Your Time: " + seconds + "s");
                   oppTimer.setText("Opponent Time: " + (TURN_DURATION_MS / 1000) + "s");
               }

               @Override
               public void onFinish() {
                   if (!roundActive) return;
                   stopTurnTimer();
                   char winner = mySymbol.equals("X") ? 'O' : 'X';
                   gameRef.child("winner").setValue(String.valueOf(winner));
               }
           }.start();

       } else {
           // Opponent’s turn → opponent timer decreases, my timer shows fixed full time
           turnTimer = new CountDownTimer(durationMillis, 1000) {
               @Override
               public void onTick(long millisUntilFinished) {
                   if (!roundActive) return;
                   int seconds = (int) (millisUntilFinished / 1000);

                   oppTimer.setText("Opponent Time: " + seconds + "s");
                   myTimer.setText("Your Time: " + (TURN_DURATION_MS / 1000) + "s");
               }

               @Override
               public void onFinish() {
                   if (!roundActive) return;
                   stopTurnTimer();
                   char winner = mySymbol.equals("X") ? 'O' : 'X';
                   gameRef.child("winner").setValue(String.valueOf(winner));
               }
           }.start();
       }
   }*/

    @SuppressLint("SetTextI18n")
    private void startTurnTimer(long turnStartTimeFromFirebase) {
        if (!roundActive || isDialogShown) return;
        stopTurnTimer(); // cancel existing

        // compute remaining time
        long currentServerTime = System.currentTimeMillis() + serverTimeOffset;
        long elapsed = currentServerTime - turnStartTimeFromFirebase;
        long timeLeftMs = TURN_DURATION_MS - elapsed;

        if (timeLeftMs <= 0) {
            // timer expired → opponent wins immediately
            char winner = mySymbol.equals("X") ? 'O' : 'X';
            gameRef.child("winner").setValue(String.valueOf(winner));
            return;
        }

        final TextView myTimer = "X".equals(mySymbol) ? timerTxtLeft : timerTxtRight;
        final TextView oppTimer = "X".equals(mySymbol) ? timerTxtRight : timerTxtLeft;

        // initialize UI
        if (isMyTurn) {
            myTimer.setText("Your Time: " + (timeLeftMs / 1000) + "s");
            oppTimer.setText("Opponent Time: " + (TURN_DURATION_MS / 1000) + "s");
        } else {
            myTimer.setText("Your Time: " + (TURN_DURATION_MS / 1000) + "s");
            oppTimer.setText("Opponent Time: " + (timeLeftMs / 1000) + "s");
        }

        // create runnable loop
        turnRunnable = new Runnable() {
            @Override
            public void run() {
                if (!roundActive) return;

                long now = System.currentTimeMillis() + serverTimeOffset;
                long left = TURN_DURATION_MS - (now - turnStartTimeFromFirebase);

                if (left <= 0) {
                    // time expired
                    char winner = mySymbol.equals("X") ? 'O' : 'X';
                    gameRef.child("winner").setValue(String.valueOf(winner));
                    return;
                }

                int seconds = (int) (left / 1000);
                if (isMyTurn) myTimer.setText("Your Time: " + seconds + "s");
                else oppTimer.setText("Opponent Time: " + seconds + "s");

                // schedule next tick only if more than 1 second left
                if (left > 1000) timerHandler.postDelayed(this, 500);
            }
        };

        timerHandler.post(turnRunnable);
    }


    @SuppressLint("SetTextI18n")
    private void stopTurnTimer() {
        if (timerHandler != null && turnRunnable != null) {
            timerHandler.removeCallbacks(turnRunnable);
        }
        if (turnTimer != null) {
            try { turnTimer.cancel(); } catch (Exception ignored) {}
            turnTimer = null;
        }
        // Reset timers to neutral text
        timerTxtLeft.setText("Wait Your Turn");
        timerTxtRight.setText("Wait Your Turn");
    }

    private void    quitDialogfun() {

        quitdialog.setContentView(R.layout.quit_dialog);
        Objects.requireNonNull(quitdialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        quitdialog.setCanceledOnTouchOutside(false);

        Button quitBtn = quitdialog.findViewById(R.id.quit_btn);
        Button continueBtn = quitdialog.findViewById(R.id.continue_btn);

        quitBtn.setOnClickListener(v -> {
            quitdialog.dismiss();
            if (gameRef != null) gameRef.child("status").setValue("left");
            Intent intent = new Intent(this, OfflineGameMenuActivity.class);
            startActivity(intent);
            finish();
        });

        continueBtn.setOnClickListener(v -> quitdialog.dismiss());
        quitdialog.show();
    }
    @SuppressLint({"GestureBackNavigation", "MissingSuperCall"})
    @Override
    public void onBackPressed() {
        quitDialogfun();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTurnTimer();
        if (turnTimer != null) turnTimer.cancel();
        if (gameRef != null) gameRef.child("status").setValue("left");
    }
}
