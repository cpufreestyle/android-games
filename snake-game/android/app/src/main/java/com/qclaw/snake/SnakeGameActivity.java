package com.qclaw.snake;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;

public class SnakeGameActivity extends Activity {
    
    private SnakeView snakeView;
    private TextView scoreText;
    private TextView statusText;
    private Button startBtn;
    private Button upBtn, downBtn, leftBtn, rightBtn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake);
        
        snakeView = findViewById(R.id.snakeView);
        scoreText = findViewById(R.id.scoreText);
        statusText = findViewById(R.id.statusText);
        startBtn = findViewById(R.id.startBtn);
        upBtn = findViewById(R.id.upBtn);
        downBtn = findViewById(R.id.downBtn);
        leftBtn = findViewById(R.id.leftBtn);
        rightBtn = findViewById(R.id.rightBtn);
        
        snakeView.setGameCallback(new SnakeView.GameCallback() {
            @Override
            public void onScoreChanged(int score) {
                runOnUiThread(() -> scoreText.setText("分数: " + score));
            }
            
            @Override
            public void onGameOver() {
                runOnUiThread(() -> {
                    statusText.setText("游戏结束!");
                    statusText.setTextColor(Color.RED);
                    startBtn.setEnabled(true);
                });
            }
        });
        
        startBtn.setOnClickListener(v -> {
            snakeView.startGame();
            statusText.setText("游戏中...");
            statusText.setTextColor(Color.parseColor("#4CAF50"));
            startBtn.setEnabled(false);
        });
        
        upBtn.setOnClickListener(v -> snakeView.setDirection(0, -1));
        downBtn.setOnClickListener(v -> snakeView.setDirection(0, 1));
        leftBtn.setOnClickListener(v -> snakeView.setDirection(-1, 0));
        rightBtn.setOnClickListener(v -> snakeView.setDirection(1, 0));
    }
}
