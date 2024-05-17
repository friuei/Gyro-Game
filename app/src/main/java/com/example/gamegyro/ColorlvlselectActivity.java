package com.example.gamegyro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ColorlvlselectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_level);

        Button colorlvl1Button = findViewById(R.id.buttonlv1color);
        Button colorlvl2Button = findViewById(R.id.buttonlv2color);
        Button colorlvl3Button = findViewById(R.id.buttonlv3color);

        colorlvl1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ColorlvlselectActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        colorlvl2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ColorlvlselectActivity.this, Colorlvl2Activity.class);
                startActivity(intent);
            }
        });

        colorlvl3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ColorlvlselectActivity.this, Colorlvl3Activity.class);
                startActivity(intent);
            }
        });
    }
}


