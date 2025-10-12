package com.example.tic_tac_toe_m.tictactoegame.AI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;

public class AIGetPlayerNameActivity  extends AppCompatActivity implements View.OnTouchListener {

    private String playerName;
    private EditText playerNameTxt;
    private Button playerButton;
   // private ImageView BackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_aiget_player_name);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }



      //  BackBtn = (ImageView) findViewById(R.id.ai_player_names_back_btn);
        playerNameTxt = (EditText) findViewById(R.id.ai_player_name_edttxt);
        playerButton = (Button) findViewById(R.id.ai_player_name_btn);

        playerButton.setOnTouchListener(this);
        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(playerNameTxt.getText().toString())) {
                    Toast.makeText(getBaseContext(), "Enter Name", Toast.LENGTH_LONG).show();
                } else {

                    playerName = playerNameTxt.getText().toString();
                    Intent intent = new Intent(AIGetPlayerNameActivity.this,AiChooseSymbolActivity.class);
                    intent.putExtra("p1",playerName);
                    startActivity(intent);
                }
            }
        });
        // Inside onCreate()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back behavior or just close activity
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });


      /*  BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });*/

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

            if (v == playerButton) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.5f);
                }   else {
                    v.setAlpha(1f);
                }
            }
        return false;
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
