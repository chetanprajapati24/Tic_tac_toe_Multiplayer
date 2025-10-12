package com.example.tic_tac_toe_m.tictactoegame.AI;

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
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tic_tac_toe_m.R;
import com.example.tic_tac_toe_m.tictactoegame.Setting.MyServices;
import com.example.tic_tac_toe_m.tictactoegame.OfflineGameMenuActivity;
import com.example.tic_tac_toe_m.tictactoegame.Setting.SettingsActivity;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class AiGameActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView Box_1;
    private ImageView Box_2;
    private ImageView Box_3;
    private ImageView Box_4;
    private ImageView Box_5;
    private ImageView Box_6;
    private ImageView Box_7;
    private ImageView Box_8;
    private ImageView Box_9;

    private GifImageView settingsGifView;
    private ImageView[] Boxes;

    private String difficultyLevel; // "Easy", "Medium", "Hard"

    Vibrator vibrator;
    private TextView playerOneWins, playerTwoWins;

    Dialog dialog, drawdialog, robotdialog, quitdialog;

    int playerOneWinCount = 0;
    int playerTwoWinCount = 0;

    int PICK_SIDE;

    // Initialize the player X and O with 0 and 1 respectively
    int Player_X = 0;
    int Player_0 = 1;

    int storeActivePlayer;
    int ActivePlayer;

    // No player wins the game the isGameActive is true when the player X or O wins it will be false
    boolean isGameActive = true;

    // Initialize array with -1 when Player X or O fill click on the box it turn 0 and 1 respectively
    int[] filledPos = {-1, -1, -1, -1, -1, -1, -1, -1, -1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_ai_game);

        dialog = new Dialog(this);
        drawdialog = new Dialog(this);
        robotdialog = new Dialog(this);
        quitdialog = new Dialog(this);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // link all the Boxes with Design (boxes in the activity_game.Xml has the id so link with each Box)
        Box_1 = findViewById(R.id.img_1);
        Box_2 = findViewById(R.id.img_2);
        Box_3 = findViewById(R.id.img_3);
        Box_4 = findViewById(R.id.img_4);
        Box_5 = findViewById(R.id.img_5);
        Box_6 = findViewById(R.id.img_6);
        Box_7 = findViewById(R.id.img_7);
        Box_8 = findViewById(R.id.img_8);
        Box_9 = findViewById(R.id.img_9);

        Boxes = new ImageView[]{Box_1, Box_2, Box_3, Box_4, Box_5, Box_6, Box_7, Box_8, Box_9};

        settingsGifView = findViewById(R.id.ai_game_seting_gifview);

        CircularImageView playerOneImg = findViewById(R.id.player_one_img);
        TextView playerOneName = findViewById(R.id.player_one_name_txt);
        playerOneWins = findViewById(R.id.player_one_win_count_txt);
        playerTwoWins = findViewById(R.id.player_two_won_txt);

        // if user click on particular Box the tag basically value of box (Box_1 has value 1, Box_2 has value 2, ...) send to the onClick function
        for (ImageView box : Boxes) {
            box.setOnClickListener(this);
        }

        playerOneWins.setText(String.valueOf(playerOneWinCount));
        playerTwoWins.setText(String.valueOf(playerTwoWinCount));

        difficultyLevel = getIntent().getStringExtra("level"); // passed from previous activity
        if (difficultyLevel == null) difficultyLevel = "Hard"; // fallback


        String playerOne = getIntent().getStringExtra("p1");
        PICK_SIDE = getIntent().getIntExtra("ps", 0);
        playerOneName.setText(playerOne);

        ActivePlayer = PICK_SIDE;
        storeActivePlayer = PICK_SIDE;

        Drawable drawable = settingsGifView.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).stop();
        }

        if (PICK_SIDE == 0) {
            playerOneImg.setBorderWidth(10f);
            playerOneImg.setBorderColorStart(Color.parseColor("#EB469A"));
            playerOneImg.setBorderColorEnd(Color.parseColor("#7251DF"));
            playerOneImg.setBorderColorDirection(CircularImageView.GradientDirection.TOP_TO_BOTTOM);

            storeActivePlayer = 0;
            ActivePlayer = 0;
        } else if (PICK_SIDE == 1) {
            // Set Border
            playerOneImg.setBorderWidth(10f);
            playerOneImg.setBorderColorStart(Color.parseColor("#F7A27B"));
            playerOneImg.setBorderColorEnd(Color.parseColor("#FF3D00"));
            playerOneImg.setBorderColorDirection(CircularImageView.GradientDirection.TOP_TO_BOTTOM);

            storeActivePlayer = 1;
            ActivePlayer = 1;
        }

        settingsGifView.setOnClickListener(v -> {
            Drawable drawable1 = settingsGifView.getDrawable();
            if (drawable1 instanceof Animatable) {
                ((Animatable) drawable1).start();
            }
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                Drawable drawable2 = settingsGifView.getDrawable();
                if (drawable2 instanceof Animatable) {
                    ((Animatable) drawable2).stop();
                }
                Intent intent = new Intent(AiGameActivity.this, SettingsActivity.class);
                startActivity(intent);
            }, 750);
        });
        // Backpress handle
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back behavior or just close activity
                quitDialogfun();
            }
        });


    }

    @Override
    public void onClick(View view) {
        if (!isGameActive) return;

        ImageView clickImg = findViewById(view.getId());
        int gettingTag = Integer.parseInt(view.getTag().toString());

        if (ActivePlayer == Player_X && filledPos[gettingTag - 1] == -1 && PICK_SIDE == Player_X) {
            if (MyServices.SOUND_CHECK) {
                final MediaPlayer mp = MediaPlayer.create(this, R.raw.x);
                mp.start();
            }

            // Vibrate when Player X moves
            MyServices.vibrate(this, 500);

            clickImg.setImageResource(R.drawable.xbg);
            storeActivePlayer = ActivePlayer;
            ActivePlayer = Player_0;
            int value = gettingTag - 1;
            filledPos[value] = Player_X;

            checkForWin();
            if (isGameActive) checkdraw();
            if (isGameActive) AI();
        }

        else if (ActivePlayer == Player_0 && filledPos[gettingTag - 1] == -1 && PICK_SIDE == Player_0) {
            if (MyServices.SOUND_CHECK) {
                final MediaPlayer mp = MediaPlayer.create(this, R.raw.o);
                mp.start();
            }

            //  Vibrate when Player O moves
            MyServices.vibrate(this, 500);

            clickImg.setImageResource(R.drawable.obg);
            storeActivePlayer = ActivePlayer;
            ActivePlayer = Player_X;
            int value = gettingTag - 1;
            filledPos[value] = Player_0;

            checkForWin();
            if (isGameActive) checkdraw();
            if (isGameActive) AI();
        }
    }


   /* private void AI() {
        if (!isGameActive) return; // Fix: Prevent AI move if game already over

        char board[][] = {{' ', ' ', ' '},
                {' ', ' ', ' '},
                {' ', ' ', ' '}};

        // Convert filledPos to board[][]
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            if (filledPos[i] == -1) board[row][col] = '_';
            else if (filledPos[i] == 0) board[row][col] = 'x';
            else if (filledPos[i] == 1) board[row][col] = 'o';
        }

        Minmax.Move bestMove = findBestMove(board);
        int index = bestMove.row * 3 + bestMove.col;

        // Delay AI move for realism
        new Handler().postDelayed(() -> {
            if (!isGameActive || filledPos[index] != -1) return; // Double check

            // Play sound/vibration for AI
            if (MyServices.SOUND_CHECK) {
                final MediaPlayer mp = MediaPlayer.create(this, PICK_SIDE == 0 ? R.raw.o : R.raw.x);
                mp.start();
            }
            if (MyServices.VIBRATION_CHECK) {
                if (Build.VERSION.SDK_INT >= 26)
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                else
                    vibrator.vibrate(200);
            }

            if (PICK_SIDE == 0) { // user = X, AI = O
                Boxes[index].setImageResource(R.drawable.obg);
                filledPos[index] = Player_0;
                ActivePlayer = Player_X;
                storeActivePlayer = Player_0;
            } else {
                Boxes[index].setImageResource(R.drawable.xbg);
                filledPos[index] = Player_X;
                ActivePlayer = Player_0;
                storeActivePlayer = Player_X;
            }

            checkForWin();
            if (isGameActive) checkdraw();
        }, 500); // 0.5s delay for realism
    }*/

    private void AI() {
        if (!isGameActive) return; // Prevent AI move if game over

        new Handler().postDelayed(() -> {
            if (!isGameActive) return;

            int index = -1;

            // Choose move based on difficulty level
            switch (difficultyLevel) {
                case "Easy":
                    index = getRandomMove();
                    break;

                case "Medium":
                    // 50% chance of best move, 50% random
                    if (Math.random() < 0.5) {
                        index = getRandomMove();
                    } else {
                        index = getBestMove();
                    }
                    break;

                case "Hard":
                default:
                    index = getBestMove();
                    break;
            }
            Log.d("AI_DEBUG", "Difficulty: " + difficultyLevel);

            if (index == -1 || filledPos[index] != -1) return; // invalid or occupied

            // Play sound/vibration
            if (MyServices.SOUND_CHECK) {
                final MediaPlayer mp = MediaPlayer.create(this, PICK_SIDE == 0 ? R.raw.o : R.raw.x);
                mp.start();
            }
            if (MyServices.VIBRATION_CHECK) {
                if (Build.VERSION.SDK_INT >= 26)
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                else
                    vibrator.vibrate(200);
            }

            // Set AI move
            if (PICK_SIDE == 0) { // user is X, AI is O
                Boxes[index].setImageResource(R.drawable.obg);
                filledPos[index] = Player_0;
                ActivePlayer = Player_X;
                storeActivePlayer = Player_0;
            } else {
                Boxes[index].setImageResource(R.drawable.xbg);
                filledPos[index] = Player_X;
                ActivePlayer = Player_0;
                storeActivePlayer = Player_X;
            }

            checkForWin();
            if (isGameActive) checkdraw();
        }, 500); // 0.5s delay for realism
    }

    private int getRandomMove() {
        java.util.List<Integer> available = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (filledPos[i] == -1) {
                available.add(i);
            }
        }
        if (available.isEmpty()) return -1;
        return available.get((int) (Math.random() * available.size()));
    }

    private int getBestMove() {
        char[][] board = new char[3][3];
           /*
    * char[][] board = {
    {'x', 'o', 'x'},
    {'_', 'o', '_'},
    {'_', '_', '_'}
};
*/

        // Convert filledPos[] to 2D char array board[][]
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            if (filledPos[i] == -1) board[row][col] = '_';
            else if (filledPos[i] == Player_X) board[row][col] = 'x';
            else board[row][col] = 'o';
        }

        // Set AI and opponent symbols based on PICK_SIDE
        // PICK_SIDE == 0 → Player chose X → AI is O
        // PICK_SIDE == 1 → Player chose O → AI is X
        if (PICK_SIDE == 0) {
            Minmax.player = 'o';
            Minmax.opponent = 'x';
        } else {
            Minmax.player = 'x';
            Minmax.opponent = 'o';
        }

        // Get best move from Minimax
        Minmax.Move bestMove = Minmax.findBestMove(board);

        // Convert (row, col) back to 1D index
        if (bestMove == null || bestMove.row == -1
                             || bestMove.col == -1) return -1;
        return bestMove.row * 3 + bestMove.col;
    }



    private void checkForWin() {
        int[][] winningPos = {
                {1, 2, 3}, {4, 5, 6}, {7, 8, 9},
                {1, 4, 7}, {2, 5, 8}, {3, 6, 9},
                {1, 5, 9}, {3, 5, 7}
        };

        for (int[] pos : winningPos) {
            int val0 = pos[0], val1 = pos[1], val2 = pos[2];

            if (filledPos[val0 - 1] != -1 &&
                    filledPos[val0 - 1] == filledPos[val1 - 1] &&
                    filledPos[val1 - 1] == filledPos[val2 - 1])
            {

                int winner = filledPos[val0 - 1]; // 0 = O, 1 = X
                boolean humanWon;

                // Log the winning condition
                Log.d("WIN_CHECK", "Winning positions: " + val0 + ", " + val1 + ", " + val2);
                Log.d("WIN_CHECK", "Winner value: " + winner);
                if (winner == Player_X) {
                    humanWon = (PICK_SIDE == 0); // Human is X
                    if (humanWon) {
                        playerOneWinCount++;
                        playerOneWins.setText(String.valueOf(playerOneWinCount));
                    } else {
                        playerTwoWinCount++;
                        playerTwoWins.setText(String.valueOf(playerTwoWinCount));
                    }
                    highlightWinningBoxes(val0, val1, val2, R.drawable.cross_background);

                } else { // winner == Player_0
                    humanWon = (PICK_SIDE == 1); // Human is O
                    if (humanWon) {
                        playerOneWinCount++;
                        playerOneWins.setText(String.valueOf(playerOneWinCount));
                    } else {
                        playerTwoWinCount++;
                        playerTwoWins.setText(String.valueOf(playerTwoWinCount));
                    }
                    highlightWinningBoxes(val0, val1, val2, R.drawable.circle_background);
                }

              //  Pass winner + human/bot info
                showWinDialog(humanWon, winner);

                isGameActive = false;
                break;

            }
        }
    }


    private void showWinDialog(boolean isHumanWinner, int winnerSymbol) {
        Handler handler = new Handler();
        if (MyServices.SOUND_CHECK) {
            final MediaPlayer mp = MediaPlayer.create(this, R.raw.click);
            mp.start();
        }
        handler.postDelayed(() -> {
            if (isHumanWinner) {
                celebrateDialog(winnerSymbol); // show correct symbol (X or O)
            } else {
                robotDialogfun(); // robot win
            }
        }, 750);
    }




    void checkdraw() {
        boolean check = true;
        for (int i = 0; i <= 8; i++) {
            if (filledPos[i] == -1) {
                check = false;
            }
        }
        if (check) {
            isGameActive = false;
            if (MyServices.SOUND_CHECK) {
                final MediaPlayer mp = MediaPlayer.create(this, R.raw.click);
                mp.start();
            }
            DrawDialogfun();
        }
    }


    private void highlightWinningBoxes(int val0, int val1, int val2, int backgroundResource) {
        if (val0 == 1 && val1 == 2 && val2 == 3) {
            Box_1.setBackgroundResource(backgroundResource);
            Box_2.setBackgroundResource(backgroundResource);
            Box_3.setBackgroundResource(backgroundResource);
        } else if (val0 == 4 && val1 == 5 && val2 == 6) {
            Box_4.setBackgroundResource(backgroundResource);
            Box_5.setBackgroundResource(backgroundResource);
            Box_6.setBackgroundResource(backgroundResource);
        } else if (val0 == 7 && val1 == 8 && val2 == 9) {
            Box_7.setBackgroundResource(backgroundResource);
            Box_8.setBackgroundResource(backgroundResource);
            Box_9.setBackgroundResource(backgroundResource);
        } else if (val0 == 1 && val1 == 4 && val2 == 7) {
            Box_1.setBackgroundResource(backgroundResource);
            Box_4.setBackgroundResource(backgroundResource);
            Box_7.setBackgroundResource(backgroundResource);
        } else if (val0 == 2 && val1 == 5 && val2 == 8) {
            Box_2.setBackgroundResource(backgroundResource);
            Box_5.setBackgroundResource(backgroundResource);
            Box_8.setBackgroundResource(backgroundResource);
        } else if (val0 == 3 && val1 == 6 && val2 == 9) {
            Box_3.setBackgroundResource(backgroundResource);
            Box_6.setBackgroundResource(backgroundResource);
            Box_9.setBackgroundResource(backgroundResource);
        } else if (val0 == 1 && val1 == 5 && val2 == 9) {
            Box_1.setBackgroundResource(backgroundResource);
            Box_5.setBackgroundResource(backgroundResource);
            Box_9.setBackgroundResource(backgroundResource);
        } else if (val0 == 3 && val1 == 5 && val2 == 7) {
            Box_3.setBackgroundResource(backgroundResource);
            Box_5.setBackgroundResource(backgroundResource);
            Box_7.setBackgroundResource(backgroundResource);
        }
    }

private void celebrateDialog(int player_check) {


        dialog.setContentView(R.layout.celebrate_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);


        LottieAnimationView animationView = dialog.findViewById(R.id.celebrate_animationView);
        LinearLayout linearLayout = dialog.findViewById(R.id.container_1);
        Button quitBtn = dialog.findViewById(R.id.offline_game_quit_btn);
        Button continueBtn = dialog.findViewById(R.id.offline_game_continue_btn);
        ImageView playerImg = dialog.findViewById(R.id.offline_game_player_img);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            animationView.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
            if(player_check==0) {
                playerImg.setImageResource(R.drawable.xbg);
            } else  if(player_check==1) {
                playerImg.setImageResource(R.drawable.obg);
            }
        }, 2300);



        quitBtn.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(AiGameActivity.this, OfflineGameMenuActivity.class);
            startActivity(intent);
        });


        continueBtn.setOnClickListener(v -> {
            dialog.dismiss();
            Restart();
        });

        dialog.show();
    }

    private void    DrawDialogfun() {


        drawdialog.setContentView(R.layout.draw_dialog);
        Objects.requireNonNull(drawdialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        drawdialog.setCanceledOnTouchOutside(false);


        Button quitBtn = drawdialog.findViewById(R.id.offline_game_draw_quit_btn);
        Button continueBtn = drawdialog.findViewById(R.id.offline_game_draw_continue_btn);

        quitBtn.setOnClickListener(v -> {
            drawdialog.dismiss();
            Intent intent = new Intent(AiGameActivity.this, OfflineGameMenuActivity.class);
            startActivity(intent);
        });

        continueBtn.setOnClickListener(v -> {
            drawdialog.dismiss();
            Restart();
        });
        drawdialog.show();
    }



    private void    robotDialogfun() {


        robotdialog.setContentView(R.layout.robot_win_dialog);
        Objects.requireNonNull(robotdialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        robotdialog.setCanceledOnTouchOutside(false);


        Button quitBtn = robotdialog.findViewById(R.id.offline_game_draw_quit_btn);
        Button continueBtn = robotdialog.findViewById(R.id.offline_game_draw_continue_btn);

        quitBtn.setOnClickListener(v -> {
            robotdialog.dismiss();
            Intent intent = new Intent(AiGameActivity.this, OfflineGameMenuActivity.class);
            startActivity(intent);
        });

        continueBtn.setOnClickListener(v -> {
            robotdialog.dismiss();
            Restart();
        });
        robotdialog.show();
    }



    private void    quitDialogfun() {


        quitdialog.setContentView(R.layout.quit_dialog);
        Objects.requireNonNull(quitdialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        quitdialog.setCanceledOnTouchOutside(false);


        Button quitBtn = quitdialog.findViewById(R.id.quit_btn);
        Button continueBtn = quitdialog.findViewById(R.id.continue_btn);

        quitBtn.setOnClickListener(v -> {
            quitdialog.dismiss();
            Intent intent = new Intent(AiGameActivity.this, OfflineGameMenuActivity.class);
            startActivity(intent);
            finish();
        });

        continueBtn.setOnClickListener(v -> quitdialog.dismiss());
        quitdialog.show();
    }

    @SuppressLint({"GestureBackNavigation", "MissingSuperCall"})
    @Override
    public void onBackPressed() {
        // Instead of finishing the activity, show the quit confirmation dialog
        quitDialogfun();
    }


    private void Restart() {
        for (int i = 0; i <= 8; i++) {
            filledPos[i] = -1;
        }

        for (ImageView box : Boxes) {
            box.setBackgroundResource(0);
            box.setImageResource(0);
        }

        isGameActive = true;
        ActivePlayer = PICK_SIDE;
        storeActivePlayer = PICK_SIDE;

        if (ActivePlayer != PICK_SIDE) {
            AI(); // Call AI if it's AI's turn
        }
    }
}