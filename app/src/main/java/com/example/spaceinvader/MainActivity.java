package com.example.spaceinvader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText playerNameEditText = findViewById(R.id.playerNameEditText);
        Button startButton = findViewById(R.id.startButton);

        startButton.setOnClickListener(v -> {
            String playerName = playerNameEditText.getText().toString();
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("PLAYER_NAME", playerName);
            startActivity(intent);
        });
    }
}