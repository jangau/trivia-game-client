package com.hella.christmasquiz;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AnswerQuestion extends Activity {
    String selectedAnswerText = null;
    Integer selectedAnswerID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_question);

        Intent intent = getIntent();
        final List<String> answers = intent.getStringArrayListExtra("Answers");
        String questionText = intent.getStringExtra("Question");

        TextView questionView = findViewById(R.id.questionView);
        questionView.setText(questionText);

        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add((Button) findViewById(R.id.buttonAnswer1));
        buttons.add((Button) findViewById(R.id.buttonAnswer2));
        buttons.add((Button) findViewById(R.id.buttonAnswer3));
        buttons.add((Button) findViewById(R.id.buttonAnswer4));

        final Button confirmButton = findViewById(R.id.buttonConfirmAnswer);

        int idx = 0;
        for (final Button btn: buttons) {
            btn.setText(answers.get(idx));
            idx ++;

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedAnswerID == null){
                        confirmButton.setEnabled(true);
                    } else {
                        Button oldSelectedAnswer = findViewById(selectedAnswerID);
                        oldSelectedAnswer.setBackground(getResources().getDrawable(R.drawable.categorybuttons));
                        if (oldSelectedAnswer.getId() == btn.getId()){
                            confirmButton.setEnabled(false);
                            confirmButton.setTextColor(getResources().getColor(R.color.hellaYellow));
                            selectedAnswerID = null;
                            selectedAnswerText = null;
                            return;
                        }
                    }
                    selectedAnswerText = btn.getText().toString();
                    selectedAnswerID = btn.getId();
                    btn.setBackground(getResources().getDrawable(R.drawable.categorybuttonselected));
                }
            });
        }
        final ProgressBar progressBar = findViewById(R.id.timerAnswer);
        progressBar.setMax(30);
        progressBar.setProgress(30);
        progressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        final TextView timerText = findViewById(R.id.timerAsText);
        final int timerLimit = 30 * 1000;  // 30 seconds timer
        timerText.setText(Integer.toString(30));
        timerText.setTextColor(getResources().getColor(R.color.colorPrimary));
        CountDownTimer timer = new CountDownTimer(timerLimit + 100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int sec = (int) Math.ceil(millisUntilFinished / 1000);
                progressBar.setProgress(sec);
                timerText.setText(Integer.toString(sec));
                if (millisUntilFinished < 10000){
                    timerText.setTextColor(getResources().getColor(R.color.danger));
                    progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.danger), PorterDuff.Mode.MULTIPLY);
                }
            }

            @Override
            public void onFinish() {
                timerText.setText(Integer.toString(0));
                progressBar.setProgress(0);
            }
        };
        timer.start();
    }
}