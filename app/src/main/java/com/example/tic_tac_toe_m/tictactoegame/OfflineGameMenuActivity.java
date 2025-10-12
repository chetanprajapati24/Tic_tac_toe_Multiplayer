package com.example.tic_tac_toe_m.tictactoegame;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;

import com.example.tic_tac_toe_m.R;
import com.example.tic_tac_toe_m.tictactoegame.AI.AIGetPlayerNameActivity;
import com.example.tic_tac_toe_m.tictactoegame.Multiplayer.Get_Player_Multiplayer_Activity;
import com.example.tic_tac_toe_m.tictactoegame.Multiplayer.MultiplayerActivity;
import com.example.tic_tac_toe_m.tictactoegame.OnlineWithRoom.Get_PlayerName_RoomActivity;
import com.example.tic_tac_toe_m.tictactoegame.OnlineWithRoom.OnlineRoomActivity;
import com.example.tic_tac_toe_m.tictactoegame.Setting.MyServices;
import com.example.tic_tac_toe_m.tictactoegame.Setting.SettingsActivity;
import com.example.tic_tac_toe_m.tictactoegame.TwoPlayer.OfflineGetPlayersNamesActivity;

import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class OfflineGameMenuActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final int STARTUP_DELAY = 300;
    private static final int ANIM_ITEM_DURATION = 1000;
    private static final int ITEM_DELAY = 300;


    private int SET_TRANSLATE;
    private boolean animationStarted = false;

    private GifImageView settingsGifView;
    private Button withAFriendBtn, withAiBtn,withOnlineBtn,withMultipleBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make status bar transparent

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_offline_game_menu);
        MyServices.startBackgroundMusic(this, R.raw.gotheme);

        // View Binding
        settingsGifView = findViewById(R.id.seting_gifview_offline_menu);
        withAFriendBtn = findViewById(R.id.btn_choice2_offline_menu);
        withAiBtn = findViewById(R.id.btn_choice1_offline_menu);
        withOnlineBtn = findViewById(R.id.btn_choice3_online_menu);
        withMultipleBtn = findViewById(R.id.btn_choice4_online_menu);
        withOnlineBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Get_PlayerName_RoomActivity.class);
            startActivity(intent);
        });
        withMultipleBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Get_Player_Multiplayer_Activity.class);
            startActivity(intent);
        });


        // Stop GIF if running
        stopGif(settingsGifView);

        // Translate logic based on screen height
        int screenHeight = getScreenHeight(this);
        SET_TRANSLATE = (screenHeight > 1500) ? -560 : -300;

        // Set Listeners
        withAFriendBtn.setOnTouchListener(this);
        withAiBtn.setOnTouchListener(this);

        withAFriendBtn.setOnClickListener(v ->
                startActivity(new Intent(this, OfflineGetPlayersNamesActivity.class)));

        withAiBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AIGetPlayerNameActivity.class)));

        settingsGifView.setOnClickListener(v -> {
            startGif(settingsGifView);
            new Handler().postDelayed(() -> {
                stopGif(settingsGifView);
                startActivity(new Intent(this, SettingsActivity.class));
            }, 750);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showQuitDialog(); // show exit dialog
            }
        });

    }

    private int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return 0;
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels;
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus || animationStarted) return;
        animate();
        animationStarted = true;
    }

    private void animate() {
        ImageView logoImageView = findViewById(R.id.img_logo_offline_menu);
        ViewGroup container = findViewById(R.id.container_offline_menu);

        ViewCompat.animate(logoImageView)
                .translationY(SET_TRANSLATE)
                .setStartDelay(STARTUP_DELAY)
                .setDuration(ANIM_ITEM_DURATION)
                .setInterpolator(new DecelerateInterpolator(1.2f))
                .start();

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            ViewPropertyAnimatorCompat viewAnimator;

            if (v instanceof Button) {
                viewAnimator = ViewCompat.animate(v)
                        .scaleY(1).scaleX(1)
                        .setStartDelay((ITEM_DELAY * i) + 500)
                        .setDuration(500);
            } else {
                viewAnimator = ViewCompat.animate(v)
                        .translationY(50).alpha(1)
                        .setStartDelay((ITEM_DELAY * i) + 500)
                        .setDuration(1000);
            }

            viewAnimator.setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setAlpha(0.5f);
        } else {
            v.setAlpha(1f);
        }
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        // Not used
    }

    private void showQuitDialog() {
        Dialog quitDialog = new Dialog(this);
        quitDialog.setContentView(R.layout.quit_dialog); // your layout
        Objects.requireNonNull(quitDialog.getWindow())
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        quitDialog.setCanceledOnTouchOutside(false);

        Button quitBtn = quitDialog.findViewById(R.id.quit_btn);
        Button continueBtn = quitDialog.findViewById(R.id.continue_btn);

        quitBtn.setOnClickListener(v -> {
            quitDialog.dismiss();
            finishAffinity(); // closes all activities and exits app
        });

        continueBtn.setOnClickListener(v ->
                quitDialog.dismiss());
        quitDialog.show();
    }



    @SuppressLint({"MissingSuperCall", "GestureBackNavigation"})
    @Override
    public void onBackPressed() {
        showQuitDialog();
    }
}
