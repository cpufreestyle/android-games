package com.qclaw.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class SnakeView extends View {
    
    private static final int GRID_SIZE = 20;
    private static final int GAME_SPEED = 150; // ms per frame
    
    private int[][] grid;
    private int gridWidth, gridHeight;
    private ArrayList<int[]> snake;
    private int[] food;
    private int[] direction = {1, 0}; // right
    private int[] nextDirection = {1, 0};
    private boolean isRunning = false;
    private int score = 0;
    
    // Worm wiggle animation
    private float wigglePhase = 0;
    private float wiggleSpeed = 0.3f;
    
    // Paints
    private Paint wormPaint, wormHeadPaint, wormStripePaint;
    private Paint foodPaint, foodLeafPaint;
    private Paint bgPaint, soilPaint, grassPaint, rootPaint;
    private Paint dirtParticlePaint;
    
    private Handler handler;
    private Runnable gameLoop;
    private GameCallback callback;
    private Random random;
    
    // Soil particles for background texture
    private ArrayList<float[]> soilParticles;
    private ArrayList<float[]> roots;
    
    public interface GameCallback {
        void onScoreChanged(int score);
        void onGameOver();
    }
    
    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Worm body - earthy flesh tone
        wormPaint = new Paint();
        wormPaint.setColor(Color.parseColor("#D4A574")); // Sandy brown
        wormPaint.setStyle(Paint.Style.FILL);
        wormPaint.setAntiAlias(true);
        
        // Worm head - slightly pinker
        wormHeadPaint = new Paint();
        wormHeadPaint.setColor(Color.parseColor("#E8B894")); // Lighter flesh
        wormHeadPaint.setStyle(Paint.Style.FILL);
        wormHeadPaint.setAntiAlias(true);
        
        // Worm stripe/clitellum (the band on earthworms)
        wormStripePaint = new Paint();
        wormStripePaint.setColor(Color.parseColor("#C49A6C")); // Darker band
        wormStripePaint.setStyle(Paint.Style.FILL);
        wormStripePaint.setAntiAlias(true);
        
        // Food - leaf/vegetable
        foodPaint = new Paint();
        foodPaint.setColor(Color.parseColor("#228B22")); // Forest green
        foodPaint.setStyle(Paint.Style.FILL);
        foodPaint.setAntiAlias(true);
        
        foodLeafPaint = new Paint();
        foodLeafPaint.setColor(Color.parseColor("#32CD32")); // Lime green
        foodLeafPaint.setStyle(Paint.Style.FILL);
        foodLeafPaint.setAntiAlias(true);
        
        // Background - dark soil
        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#3D2914")); // Dark brown soil
        bgPaint.setStyle(Paint.Style.FILL);
        
        // Soil texture paint
        soilPaint = new Paint();
        soilPaint.setColor(Color.parseColor("#5C4033")); // Medium brown
        soilPaint.setStyle(Paint.Style.FILL);
        
        // Grass hints at surface
        grassPaint = new Paint();
        grassPaint.setColor(Color.parseColor("#2E8B57")); // Sea green
        grassPaint.setStyle(Paint.Style.FILL);
        grassPaint.setAntiAlias(true);
        
        // Roots
        rootPaint = new Paint();
        rootPaint.setColor(Color.parseColor("#8B7355")); // Root color
        rootPaint.setStyle(Paint.Style.STROKE);
        rootPaint.setStrokeWidth(2);
        rootPaint.setAntiAlias(true);
        
        // Dirt particles
        dirtParticlePaint = new Paint();
        dirtParticlePaint.setStyle(Paint.Style.FILL);
        
        handler = new Handler();
        random = new Random();
        
        soilParticles = new ArrayList<>();
        roots = new ArrayList<>();
        
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    update();
                    wigglePhase += wiggleSpeed;
                    invalidate();
                    handler.postDelayed(this, GAME_SPEED);
                }
            }
        };
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gridWidth = w / GRID_SIZE;
        gridHeight = h / GRID_SIZE;
        grid = new int[gridWidth][gridHeight];
        
        // Generate soil particles
        soilParticles.clear();
        for (int i = 0; i < 80; i++) {
            soilParticles.add(new float[]{
                random.nextFloat() * w,
                random.nextFloat() * h,
                1 + random.nextFloat() * 3, // size
                random.nextInt(3) // color variant
            });
        }
        
        // Generate roots
        roots.clear();
        for (int i = 0; i < 5; i++) {
            float startX = random.nextFloat() * w;
            float startY = random.nextFloat() * h * 0.3f;
            float endX = startX + (random.nextFloat() - 0.5f) * 150;
            float endY = startY + 50 + random.nextFloat() * 100;
            roots.add(new float[]{startX, startY, endX, endY});
        }
    }
    
    public void setGameCallback(GameCallback cb) {
        this.callback = cb;
    }
    
    public void startGame() {
        snake = new ArrayList<>();
        // Start with 5 segments
        int startX = gridWidth / 2;
        int startY = gridHeight / 2;
        for (int i = 0; i < 5; i++) {
            snake.add(new int[]{startX - i, startY});
        }
        direction = new int[]{1, 0};
        nextDirection = new int[]{1, 0};
        score = 0;
        wigglePhase = 0;
        spawnFood();
        isRunning = true;
        if (callback != null) callback.onScoreChanged(0);
        handler.post(gameLoop);
    }
    
    public void setDirection(int dx, int dy) {
        // Prevent 180 degree turn
        if (direction[0] != -dx || direction[1] != -dy) {
            nextDirection = new int[]{dx, dy};
        }
    }
    
    private void spawnFood() {
        int fx, fy;
        do {
            fx = random.nextInt(gridWidth);
            fy = random.nextInt(gridHeight);
        } while (isSnakeAt(fx, fy));
        food = new int[]{fx, fy};
    }
    
    private boolean isSnakeAt(int x, int y) {
        for (int[] seg : snake) {
            if (seg[0] == x && seg[1] == y) return true;
        }
        return false;
    }
    
    private void update() {
        direction = nextDirection;
        int[] head = snake.get(0);
        int newX = head[0] + direction[0];
        int newY = head[1] + direction[1];
        
        // Check collision with walls
        if (newX < 0 || newX >= gridWidth || newY < 0 || newY >= gridHeight) {
            gameOver();
            return;
        }
        
        // Check collision with self
        if (isSnakeAt(newX, newY)) {
            gameOver();
            return;
        }
        
        // Move worm
        snake.add(0, new int[]{newX, newY});
        
        // Check food
        if (newX == food[0] && newY == food[1]) {
            score += 10;
            if (callback != null) callback.onScoreChanged(score);
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }
    
    private void gameOver() {
        isRunning = false;
        if (callback != null) callback.onGameOver();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int w = getWidth();
        int h = getHeight();
        
        // Draw underground soil background
        canvas.drawRect(0, 0, w, h, bgPaint);
        
        // Draw soil texture patches
        for (int i = 0; i < 15; i++) {
            float px = random.nextFloat() * w;
            float py = random.nextFloat() * h;
            float size = 30 + random.nextFloat() * 50;
            canvas.drawOval(px - size/2, py - size/4, px + size/2, py + size/4, soilPaint);
        }
        
        // Draw grass at top (surface level)
        for (int i = 0; i < w; i += 8) {
            float grassHeight = 5 + random.nextFloat() * 10;
            canvas.drawRect(i, 0, i + 4, grassHeight, grassPaint);
        }
        
        // Draw roots
        for (float[] root : roots) {
            canvas.drawLine(root[0], root[1], root[2], root[3], rootPaint);
        }
        
        // Draw dirt particles
        for (float[] particle : soilParticles) {
            int variant = (int) particle[3];
            switch (variant) {
                case 0: dirtParticlePaint.setColor(Color.parseColor("#6B4423")); break;
                case 1: dirtParticlePaint.setColor(Color.parseColor("#8B6914")); break;
                default: dirtParticlePaint.setColor(Color.parseColor("#4A3728")); break;
            }
            canvas.drawCircle(particle[0], particle[1], particle[2], dirtParticlePaint);
        }
        
        // Draw food (leaf/vegetable)
        if (food != null) {
            float fx = food[0] * GRID_SIZE + GRID_SIZE / 2f;
            float fy = food[1] * GRID_SIZE + GRID_SIZE / 2f;
            
            // Draw leaf shape
            canvas.save();
            canvas.rotate(30, fx, fy);
            canvas.drawOval(fx - 8, fy - 4, fx + 8, fy + 4, foodPaint);
            canvas.drawOval(fx - 6, fy - 3, fx + 6, fy + 3, foodLeafPaint);
            // Leaf vein
            foodLeafPaint.setColor(Color.parseColor("#1B5E20"));
            canvas.drawLine(fx - 5, fy, fx + 5, fy, foodLeafPaint);
            canvas.restore();
        }
        
        // Draw worm with wiggle animation
        if (snake != null && snake.size() > 0) {
            for (int i = snake.size() - 1; i >= 0; i--) {
                int[] seg = snake.get(i);
                
                // Base position
                float cx = seg[0] * GRID_SIZE + GRID_SIZE / 2f;
                float cy = seg[1] * GRID_SIZE + GRID_SIZE / 2f;
                
                // Add wiggle offset based on segment index and animation phase
                float wiggleAmount = (float) Math.sin(wigglePhase + i * 0.8f) * 3f;
                float sizeWiggle = (float) Math.sin(wigglePhase * 2 + i * 0.5f) * 1f;
                
                // Apply perpendicular wiggle based on movement direction
                if (Math.abs(direction[0]) > 0) {
                    // Moving horizontally - wiggle up/down
                    cy += wiggleAmount;
                } else {
                    // Moving vertically - wiggle left/right
                    cx += wiggleAmount;
                }
                
                // Segment size decreases toward tail
                float baseSize = GRID_SIZE / 2f - 1;
                float segSize = baseSize - (i * 0.3f) + sizeWiggle;
                if (segSize < 4) segSize = 4;
                
                // Draw segment
                if (i == 0) {
                    // Head - slightly larger, pinker
                    float headSize = segSize + 2;
                    
                    // Glow effect
                    Paint glowPaint = new Paint();
                    glowPaint.setColor(Color.parseColor("#FFE4C4"));
                    glowPaint.setAlpha(50);
                    canvas.drawCircle(cx, cy, headSize + 3, glowPaint);
                    
                    // Main head
                    canvas.drawCircle(cx, cy, headSize, wormHeadPaint);
                    
                    // Eyes (small dark dots)
                    Paint eyePaint = new Paint();
                    eyePaint.setColor(Color.parseColor("#2C1810"));
                    eyePaint.setAntiAlias(true);
                    float eyeOffset = headSize * 0.4f;
                    float eyeSize = 2.5f;
                    
                    // Position eyes based on direction
                    float eyeX1 = cx + direction[0] * eyeOffset + direction[1] * eyeOffset * 0.8f;
                    float eyeY1 = cy + direction[1] * eyeOffset - direction[0] * eyeOffset * 0.8f;
                    float eyeX2 = cx + direction[0] * eyeOffset - direction[1] * eyeOffset * 0.8f;
                    float eyeY2 = cy + direction[1] * eyeOffset + direction[0] * eyeOffset * 0.8f;
                    
                    canvas.drawCircle(eyeX1, eyeY1, eyeSize, eyePaint);
                    canvas.drawCircle(eyeX2, eyeY2, eyeSize, eyePaint);
                    
                } else {
                    // Body segment
                    canvas.drawCircle(cx, cy, segSize, wormPaint);
                    
                    // Add clitellum (band) on segment 3-5
                    if (i >= 3 && i <= 5) {
                        canvas.drawCircle(cx, cy, segSize * 0.7f, wormStripePaint);
                    }
                    
                    // Add subtle segment ring
                    Paint ringPaint = new Paint();
                    ringPaint.setColor(Color.parseColor("#C49A6C"));
                    ringPaint.setStyle(Paint.Style.STROKE);
                    ringPaint.setStrokeWidth(1);
                    ringPaint.setAntiAlias(true);
                    ringPaint.setAlpha(100);
                    canvas.drawCircle(cx, cy, segSize * 0.6f, ringPaint);
                }
            }
        }
    }
}
