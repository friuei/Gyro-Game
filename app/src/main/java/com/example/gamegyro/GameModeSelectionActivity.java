package com.example.gamegyro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GameModeSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_screen);

        Button shapeGameButton = findViewById(R.id.buttonShapeGame);
        Button colorGameButton = findViewById(R.id.buttonColorGame);
        Button mathGameButton = findViewById(R.id.buttonMathGame);

        colorGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GameModeSelectionActivity.this, ColorlvlselectActivity.class);
                startActivity(intent);
            }
        });
        shapeGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GameModeSelectionActivity.this, ShapeGameActivity.class);
                startActivity(intent);
            }
        });

        mathGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GameModeSelectionActivity.this, MathGameActivity.class);
                startActivity(intent);
            }
        });
    }
}

