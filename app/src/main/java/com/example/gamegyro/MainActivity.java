package com.example.gamegyro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {

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

        if (gyroSensor == null) {
            Toast.makeText(this, "Gyroscope sensor not available", Toast.LENGTH_SHORT).show();
            finish();
        }
        startGame();
    }
    private void updateScore() {
        if (!isGameOver) {
            score++;
            scoreText.setText("Score: " + score);
        }
    }
    private void startGame() {
        isGameOver = false;
        score = 0;
        updateScore();
        gameHandler.postDelayed(gameLoop, 1000);
    }
    private final SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!isGameOver) {
                float movement = event.values[1];
                float currentX = movableObject.getX();
                float newX = currentX + movement * 10;

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
        gameContainer.addView(newObject);

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
    private void moveFallingObjects() {
        List<View> toRemove = new ArrayList<>();
        for (View obj : fallingObjects) {
            float newY = obj.getY() + 10;
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
    private void checkCollision() {
        int playerRadius = movableObject.getWidth() / 2; // Half width or height, whichever is smaller
        int playerCenterX = (int) movableObject.getX() + playerRadius;
        int playerCenterY = (int) movableObject.getY() + playerRadius;

        for (View obj : fallingObjects) {
            int objectRadius = obj.getWidth() / 2; // Assuming a roughly circular shape
            int objectCenterX = (int) obj.getX() + objectRadius;
            int objectCenterY = (int) obj.getY() + objectRadius;

            double distance = Math.sqrt(Math.pow(playerCenterX - objectCenterX, 2) + Math.pow(playerCenterY - objectCenterY, 2));

            if (distance < playerRadius + objectRadius) {
                isGameOver = true;
                gameHandler.removeCallbacks(gameLoop);
                showGameOverDialog();
                //break;
            }
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
                        resetGame();
                    }
                })
                .setCancelable(false);
        builder.show();
    }
    private void resetGame() {
        score = 0;
        updateScore();
        isGameOver = false;
        fallingObjects.clear();
        gameContainer.removeAllViewsInLayout();
        gameContainer.addView(movableObject);
        gameContainer.addView(scoreText);
        startGame();
    }
}

