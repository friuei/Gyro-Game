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
import android.view.Gravity;
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

public class MathGameActivity extends Activity {
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
    private TextView equationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mathgame);

        gameContainer = findViewById(R.id.game_math_container);
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
            scoreText.setText("Score: " + score);
        }
    }

    private void displayNewEquation(String equation) {
        if(equationText == null) equationText = findViewById(R.id.equation_text);
        equationText.setText(equation);
    }

    private void startGame() {
        isGameOver = false;
        score = 0;
        generateEquation();
        updateScore();
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

    private int currentAnswer;

    private void generateEquation() {
        int a = random.nextInt(10);
        int b = random.nextInt(10);
        currentAnswer = a + b;
        String equation = a + " + " + b + " = ?";
        displayNewEquation(equation);
    }
    private void spawnFallingObject() {
        TextView newNumber = new TextView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        newNumber.setLayoutParams(params);
        newNumber.setBackgroundResource(R.drawable.circle_shape);
        int numberToShow = random.nextBoolean() ? currentAnswer : random.nextInt(20);
        newNumber.setText(String.valueOf(numberToShow));
        newNumber.setGravity(Gravity.CENTER);
        newNumber.setTag(numberToShow);
        gameContainer.addView(newNumber);

        newNumber.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                newNumber.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int objectWidth = newNumber.getWidth();
                newNumber.setX(random.nextInt(screenWidth - objectWidth));
                newNumber.setY(0);
                fallingObjects.add(newNumber);
            }
        });
    }

    private void moveFallingObjects() {
        List<View> toRemove = new ArrayList<>();
        for (View obj : fallingObjects) {
            float newY = obj.getY() + 50;
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
                int number = (Integer) obj.getTag();
                if (number == currentAnswer){
                    score+=20;
                    updateScore();
                    generateEquation();
                }
                else{
                    isGameOver = true;
                    gameHandler.removeCallbacks(gameLoop);
                    showGameOverDialog();
                }
                toRemove.add(obj);
            }
        }
        for (View obj : toRemove) {
            gameContainer.removeView(obj);
            fallingObjects.remove(obj);
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
                        Intent intent = new Intent(MathGameActivity.this, GameModeSelectionActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
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
