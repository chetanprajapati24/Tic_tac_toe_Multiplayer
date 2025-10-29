package com.example.tic_tac_toe_m.tictactoegame.TwoPlayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tic_tac_toe_m.R;
import com.example.tic_tac_toe_m.tictactoegame.OfflineGameMenuActivity;
import com.example.tic_tac_toe_m.tictactoegame.Setting.MyServices;
import com.example.tic_tac_toe_m.tictactoegame.Setting.SettingsActivity;

import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class OfflineGameActivity extends AppCompatActivity {

    private final ImageView[] cells = new ImageView[9];
    private final int[] gameState = new int[9]; // 0 = empty, 1 = X, 2 = O
    private boolean playerOneTurn = true;

    private TextView playerOneScoreTxt, playerTwoScoreTxt;
    private TextView timerOneTxt;
    private TextView timerTwoTxt;
    private int playerOneScore = 0, playerTwoScore = 0;

    private GifImageView settingsGifView;
    private Dialog celebrateDialog, drawDialog, quitDialog;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 20000; // 10 sec per turn
    private boolean isTimerRunning = false;

    private int playerOneSymbol, playerTwoSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_offline_game);

        // Find Views
        playerOneScoreTxt = findViewById(R.id.player_one_win_count_txt);
        playerTwoScoreTxt = findViewById(R.id.player_two_won_txt);
        timerOneTxt = findViewById(R.id.textViewTimer1);
        timerTwoTxt = findViewById(R.id.textViewTimer);
        TextView playerOneNameTxt = findViewById(R.id.player_one_name_txt);
        TextView playerTwoNameTxt = findViewById(R.id.player_two_name_txt);
        settingsGifView = findViewById(R.id.offline_game_seting_gifview);

        playerOneScoreTxt.setText("0");
        playerTwoScoreTxt.setText("0");
        Drawable drawable = settingsGifView.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).stop();
        }

        // Get Intent data
        String playerOneName = getIntent().getStringExtra("p1");
        String playerTwoName = getIntent().getStringExtra("p2");
        int pickSide = getIntent().getIntExtra("ps", 0); // 0 = X, 1 = O

        // Assign symbols
        if (pickSide == 0) {
            playerOneSymbol = 1; // X
            playerTwoSymbol = 2; // O
        } else {
            playerOneSymbol = 2; // O
            playerTwoSymbol = 1; // X
        }

        // Show names in UI
        playerOneNameTxt.setText(playerOneName);
        playerTwoNameTxt.setText(playerTwoName);

        settingsGifView.setOnClickListener(v -> {
            pauseTimer(); // ⏸ pause when opening settings
            Drawable drawable2 = settingsGifView.getDrawable();
            if (drawable2 instanceof Animatable) {
                ((Animatable) drawable2).start();
            }
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                Drawable drawable1 = settingsGifView.getDrawable();
                if (drawable1 instanceof Animatable) {
                    ((Animatable) drawable1).stop();
                }
                Intent intent = new Intent(OfflineGameActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 200); // use onActivityResult to resume
            }, 750);
        });

        // Link cells
        for (int i = 0; i < 9; i++) {
            @SuppressLint("DiscouragedApi") int id = getResources().getIdentifier("img_" + (i + 1), "id", getPackageName());
            cells[i] = findViewById(id);
            final int finalI = i;
            cells[i].setOnClickListener(v -> makeMove(finalI));
        }

        celebrateDialog = new Dialog(this);
        drawDialog = new Dialog(this);
        quitDialog = new Dialog(this);

        resetBoardOnly();
        startNewTurnTimer();

        // Inside onCreate()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back behavior or just close activity
                quitDialogFun();

            }
        });


    }


  /*  private void playMoveSound(int playerSymbol) {
        int soundRes = (playerSymbol == playerOneSymbol) ? R.raw.x : R.raw.o;
        MediaPlayer mp = MediaPlayer.create(this, soundRes);
        mp.setOnCompletionListener(MediaPlayer::release);
        mp.start();
    }*/
   /* private void vibrateDevice(int duration) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(duration);
            }
        }
    }*/


    private void makeMove(int index) {
        if (gameState[index] != 0) return;

        int currentSymbol = playerOneTurn ? playerOneSymbol : playerTwoSymbol;
        gameState[index] = currentSymbol;
        cells[index].setImageResource(currentSymbol == 1 ? R.drawable.xbg : R.drawable.obg);
        cells[index].setEnabled(false);

        // Play sound & vibration
        if (MyServices.SOUND_CHECK) {
            int soundRes = (currentSymbol == 1) ? R.raw.x : R.raw.o;
            MediaPlayer mp = MediaPlayer.create(this, soundRes);
            mp.setOnCompletionListener(MediaPlayer::release); // release after play
            mp.start();
        }

      //  MyServices.vibrate(this, 200);  // 200ms vibration

        if (checkWinner()) {
            if (playerOneTurn) {
                playerOneScore++;
                celebrateDialogFun(playerOneSymbol);
            } else {
                playerTwoScore++;
                celebrateDialogFun(playerTwoSymbol);
            }
            updateScores();
            return;
        }

        if (isDraw()) {
            drawDialogFun();
            return;
        }

        playerOneTurn = !playerOneTurn;
        startNewTurnTimer();
    }


    private boolean checkWinner() {
        int[][] winPos = {
                {0,1,2},{3,4,5},{6,7,8},
                {0,3,6},{1,4,7},{2,5,8},
                {0,4,8},{2,4,6}
        };

        for (int[] pos : winPos) {
            int a = gameState[pos[0]];
            int b = gameState[pos[1]];
            int c = gameState[pos[2]];
            if (a != 0 && a == b && b == c) {
                highlightWinningCells(pos, a);
                return true;
            }
        }
        return false;
    }

    private boolean isDraw() {
        for (int state : gameState) {
            if (state == 0) return false;
        }
        return true;
    }

    private void highlightWinningCells(int[] pos, int symbol) {
        int bg = (symbol == 1) ? R.drawable.cross_background : R.drawable.circle_background;
        for (int index : pos) {
            cells[index].setBackgroundResource(bg);
        }
    }

    // === Celebrate Dialog ===
    private void celebrateDialogFun(int playerSymbol) {
        pauseTimer(); // stop timer while dialog shown

        celebrateDialog.setContentView(R.layout.celebrate_dialog);
        Objects.requireNonNull(celebrateDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        celebrateDialog.setCanceledOnTouchOutside(false);

        LottieAnimationView animationView = celebrateDialog.findViewById(R.id.celebrate_animationView);
        LinearLayout container = celebrateDialog.findViewById(R.id.container_1);
        ImageView winnerImg = celebrateDialog.findViewById(R.id.offline_game_player_img);
        Button quitBtn = celebrateDialog.findViewById(R.id.offline_game_quit_btn);
        Button continueBtn = celebrateDialog.findViewById(R.id.offline_game_continue_btn);
        MyServices.vibrate(this, 500);

        new Handler().postDelayed(() -> {
            animationView.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            winnerImg.setImageResource(playerSymbol == 1 ? R.drawable.xbg : R.drawable.obg);
        }, 2000);

        quitBtn.setOnClickListener(v -> {
            celebrateDialog.dismiss();
            Intent intent = new Intent(OfflineGameActivity.this, OfflineGameMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        continueBtn.setOnClickListener(v -> {
            celebrateDialog.dismiss();
            resetBoardOnly();
            startNewTurnTimer(); // restart timer after continue
        });

        celebrateDialog.show();
    }

    // === Draw Dialog ===
    private void drawDialogFun() {
        pauseTimer();

        drawDialog.setContentView(R.layout.draw_dialog);
        Objects.requireNonNull(drawDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        drawDialog.setCanceledOnTouchOutside(false);

        Button quitBtn = drawDialog.findViewById(R.id.offline_game_draw_quit_btn);
        Button continueBtn = drawDialog.findViewById(R.id.offline_game_draw_continue_btn);

        quitBtn.setOnClickListener(v -> {
            drawDialog.dismiss();
            Intent intent = new Intent(OfflineGameActivity.this, OfflineGameMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        continueBtn.setOnClickListener(v -> {
            drawDialog.dismiss();
            resetBoardOnly();
            startNewTurnTimer();
        });

        drawDialog.show();

    }

    // === Quit Dialog ===
    private void quitDialogFun() {
        pauseTimer();

        quitDialog.setContentView(R.layout.quit_dialog);
        Objects.requireNonNull(quitDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        quitDialog.setCanceledOnTouchOutside(false);

        Button quitBtn = quitDialog.findViewById(R.id.quit_btn);
        Button continueBtn = quitDialog.findViewById(R.id.continue_btn);

        quitBtn.setOnClickListener(v -> {
            quitDialog.dismiss();
            Intent intent = new Intent(OfflineGameActivity.this, OfflineGameMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        continueBtn.setOnClickListener(v -> {
            quitDialog.dismiss();
            resumeTimer(); // resume when cancel quit
        });

        quitDialog.show();
    }

    private void updateScores() {
        playerOneScoreTxt.setText(String.valueOf(playerOneScore));
        playerTwoScoreTxt.setText(String.valueOf(playerTwoScore));
    }

    private void resetBoardOnly() {
        for (int i = 0; i < 9; i++) {
            gameState[i] = 0;
            cells[i].setImageDrawable(null);
            cells[i].setBackgroundResource(0);
            cells[i].setEnabled(true);
        }
        playerOneTurn = true;
    }

    private void startTimer(long millis) {
        if (countDownTimer != null) countDownTimer.cancel();

        timeLeftInMillis = millis;
        updateTimerText();

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }
            public void onFinish() {
                isTimerRunning = false;
                if (playerOneTurn) {
                    playerTwoScore++;
                    celebrateDialogFun(playerTwoSymbol);
                } else {
                    playerOneScore++;
                    celebrateDialogFun(playerOneSymbol);
                }
                updateScores();
            }
        }.start();
        isTimerRunning = true;
    }

    // Start new turn → always full 20s
    private void startNewTurnTimer() {
        startTimer(20000);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
        }
    }

    private void resumeTimer() {
        if (!isTimerRunning && timeLeftInMillis > 0) {
            startTimer(timeLeftInMillis); // resume with leftover time
        }
    }


    @SuppressLint("SetTextI18n")
    private void updateTimerText() {
        int seconds = (int) (timeLeftInMillis / 1000);
        if (playerOneTurn) {
            timerOneTxt.setText("Time left: " + seconds + "s");
            timerTwoTxt.setText("");
        } else {
            timerTwoTxt.setText("Time left: " + seconds + "s");
            timerOneTxt.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    // Handle back press → Quit confirmation
    @SuppressLint({"GestureBackNavigation", "MissingSuperCall"})
    @Override
    public void onBackPressed() {
        quitDialogFun();
    }

    // Resume after Settings closed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            resumeTimer();
        }
    }
}
