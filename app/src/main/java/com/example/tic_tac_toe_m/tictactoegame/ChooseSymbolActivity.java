package com.example.tic_tac_toe_m.tictactoegame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tic_tac_toe_m.R;

public class ChooseSymbolActivity extends AppCompatActivity implements View.OnTouchListener {

    private ImageView backBtn, crossImg, crossRadioImg, circleImg, circleRadioImg;
    private Button continueBtn;

    private int pickSide = -1; // -1 means not selected
    private String playerOne;
    private String playerTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen setup
       /* requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
*/
        setContentView(R.layout.activity_choose_symbol);

        // Receive player names
        playerOne = getIntent().getStringExtra("p1");
        playerTwo = getIntent().getStringExtra("p2");

        // Find views
        backBtn = findViewById(R.id.pick_side_back_btn);
        crossImg = findViewById(R.id.pick_side_cross_img);
        circleImg = findViewById(R.id.pick_side_circle_img);
        crossRadioImg = findViewById(R.id.pick_side_cross_radio);
        circleRadioImg = findViewById(R.id.pick_side_circle_radio);
        continueBtn = findViewById(R.id.pick_side_continue_btn);

        // Back Button
        backBtn.setOnClickListener(v -> onBackPressed());

        // Cross selected
        crossRadioImg.setOnClickListener(v -> selectCross());

        // Circle selected
        circleRadioImg.setOnClickListener(v -> selectCircle());
        crossImg.setOnClickListener(v -> selectCross());
        circleImg.setOnClickListener(v -> selectCircle());


        // Continue Button
        continueBtn.setOnTouchListener(this);
        continueBtn.setOnClickListener(v -> {
            if (pickSide == -1) {
                Toast.makeText(this, "Please select a symbol!", Toast.LENGTH_SHORT).show();
                return;
            }


            Intent intent = new Intent(ChooseSymbolActivity.this, OfflineGameActivity.class);
            intent.putExtra("p1", playerOne);
            intent.putExtra("p2", playerTwo);
            intent.putExtra("ps", pickSide); // 0: X, 1: O
            startActivity(intent);
        });
    }

    private void selectCross() {
        pickSide = 0;

        crossRadioImg.setImageResource(R.drawable.radio_button_checked);
        circleRadioImg.setImageResource(R.drawable.radio_button_unchecked);

        crossImg.setAlpha(1.0f);
        circleImg.setAlpha(0.3f);
    }

    private void selectCircle() {
        pickSide = 1;

        circleRadioImg.setImageResource(R.drawable.radio_button_checked);
        crossRadioImg.setImageResource(R.drawable.radio_button_unchecked);

        circleImg.setAlpha(1.0f);
        crossImg.setAlpha(0.3f);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == continueBtn) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.5f);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
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
