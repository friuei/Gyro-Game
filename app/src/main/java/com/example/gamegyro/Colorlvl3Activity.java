package com.example.gamegyro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Colorlvl3Activity extends Activity {

    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private ImageView movableObject;
    private RelativeLayout gameContainer;
    private ArrayList<View> fallingObjects = new ArrayList<>();
    private Handler gameHandler = new Handler();
    private boolean isGameOver = false;
    private int score = 0;
    private TextView scoreText;
    private Random random = new Random();
    private int screenWidth;
    private int screenHeight;
    private int lives = 3;
    private TextView livesText;
    private int timeLeft = 35;
    private TextView timerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameContainer = findViewById(R.id.game_container);
        movableObject = findViewById(R.id.movable_object);
        scoreText = findViewById(R.id.score_text);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        livesText = findViewById(R.id.lives_text);
        updateLives();
        timerText = findViewById(R.id.timer_text);
        startTimer();

        if (gyroSensor == null) {
            Toast.makeText(this, "Gyroscope sensor not available", Toast.LENGTH_SHORT).show();
            finish();
        }
        startGame();
    }
    private void updateLives() {
        if (!isGameOver) {
            livesText.setText("Lives: " + lives);
        }
    }
    private void updateScore() {
        if (!isGameOver) {
            scoreText.setText("Score: " + score);
        }
    }
    private void startTimer() {
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isGameOver) {
                            timeLeft--;
                            timerText.setText("Time: " + timeLeft + "s");
                            if (timeLeft <= 0) {
                                isGameOver = true;
                                timer.cancel();
                                gameHandler.removeCallbacks(gameLoop);
                                showGameOverDialog();
                            }
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    private void displayTargetColor(int color) {
        TextView targetColorDisplay = findViewById(R.id.target_color_display);
        targetColorDisplay.setBackgroundColor(color);
        targetColorDisplay.setText("Match This Color");
    }

    private int targetColor;
    private void startGame() {
        isGameOver = false;
        score = 0;
        targetColor = getRandomColor();
        updateScore();
        displayTargetColor(targetColor);
        gameHandler.postDelayed(gameLoop, 500);
    }
    private final SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!isGameOver) {
                float movement = event.values[1];
                float currentX = movableObject.getX();
                float newX = currentX + movement * 50;

                newX = Math.max(newX, 0);
                newX = Math.min(newX, screenWidth - movableObject.getWidth());

                movableObject.setX(newX);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private int spawnCounter = 0;
    private final int spawnRate = 50;
    private Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            if (!isGameOver) {
                if (spawnCounter >= spawnRate) {
                    spawnFallingObject();
                    spawnCounter = 0;
                }

                moveFallingObjects();
                checkCollision();

                spawnCounter++;
                updateScore();
                gameHandler.postDelayed(this, 100);
            }
        }
    };
    private void spawnFallingObject() {
        ImageView newObject = new ImageView(this);
        newObject.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        newObject.setImageResource(R.drawable.circle_shape);

        int objectColor = getRandomColor();
        newObject.setColorFilter(objectColor, PorterDuff.Mode.SRC_IN);
        gameContainer.addView(newObject);

        objectColors.put(newObject, objectColor);

        newObject.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                newObject.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int objectWidth = newObject.getWidth();
                newObject.setX(random.nextInt(screenWidth - objectWidth));
                newObject.setY(0);
                fallingObjects.add(newObject);
            }
        });
    }
    private int getRandomColor() {
        int[] colors = {
                Color.RED,
                Color.GREEN,
                Color.BLUE,
                Color.YELLOW,
                Color.CYAN,
                Color.MAGENTA
        };
        return colors[random.nextInt(colors.length)];
    }
    private void moveFallingObjects() {
        List<View> toRemove = new ArrayList<>();
        for (View obj : fallingObjects) {
            float newY = obj.getY() + 150;
            if (newY > screenHeight) {
                toRemove.add(obj);
            } else {
                obj.setY(newY);
            }
        }
        for (View obj : toRemove) {
            gameContainer.removeView(obj);
            fallingObjects.remove(obj);
        }
    }

    private Map<View, Integer> objectColors = new HashMap<>();
    private void checkCollision() {
        int playerRadius = movableObject.getWidth() / 2;
        int playerCenterX = (int) movableObject.getX() + playerRadius;
        int playerCenterY = (int) movableObject.getY() + playerRadius;

        List<View> toRemove = new ArrayList<>();
        for (View obj : fallingObjects) {
            int objectRadius = obj.getWidth() / 2;
            int objectCenterX = (int) obj.getX() + objectRadius;
            int objectCenterY = (int) obj.getY() + objectRadius;

            double distance = Math.sqrt(Math.pow(playerCenterX - objectCenterX, 2) + Math.pow(playerCenterY - objectCenterY, 2));

            if (distance < playerRadius + objectRadius) {
                Integer objectColor = objectColors.get(obj);
                if (objectColor != null && objectColor.equals(targetColor)){
                    score+=20;
                    updateScore();
                    targetColor = getRandomColor();
                    displayTargetColor(targetColor);
                }
                else{
                    lives--;
                    updateLives();
                    if(lives<=0){
                        isGameOver = true;
                        gameHandler.removeCallbacks(gameLoop);
                        showGameOverDialog();
                    }
                }
                toRemove.add(obj);
            }
        }
        for (View obj : toRemove) {
            gameContainer.removeView(obj);
            fallingObjects.remove(obj);
            objectColors.remove(obj);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(gyroListener);
    }
    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over")
                .setMessage("You lost! Want to play again?")
                .setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Colorlvl3Activity.this, GameModeSelectionActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .setCancelable(false);
        builder.show();
    }
}

