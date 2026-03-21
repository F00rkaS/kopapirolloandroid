package com.example.gyakszi1;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView playerImage, botImage;
    private TextView resultText, scoreText, highScoreText;
    private Button resetButton, resetHighScoreButton;
    private ImageButton btnKo, btnPapir, btnOllo;

    private int playerWins = 0;
    private int botWins = 0;
    private int maxDiffRecord = 0;
    private int selectedPlayerChoice = -1;

    private SharedPreferences preferences;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration;
    private float currentAcceleration, lastAcceleration;
    private long lastShakeTime = 0;
    private static final int SHAKE_THRESHOLD = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerImage = findViewById(R.id.playerImage);
        botImage = findViewById(R.id.botImage);
        resultText = findViewById(R.id.resultText);
        scoreText = findViewById(R.id.scoreText);
        highScoreText = findViewById(R.id.highScoreText);
        resetButton = findViewById(R.id.resetButton);
        resetHighScoreButton = findViewById(R.id.resetHighScoreButton);

        btnKo = findViewById(R.id.btnKo);
        btnPapir = findViewById(R.id.btnPapir);
        btnOllo = findViewById(R.id.btnOllo);

        preferences = getSharedPreferences("GameStats", MODE_PRIVATE);
        maxDiffRecord = preferences.getInt("maxDiff", 0);
        updateScoreUI();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        btnKo.setOnClickListener(v -> selectChoice(0));
        btnPapir.setOnClickListener(v -> selectChoice(1));
        btnOllo.setOnClickListener(v -> selectChoice(2));

        resetButton.setOnClickListener(v -> resetCurrentGame());

        resetHighScoreButton.setOnClickListener(v -> {
            maxDiffRecord = 0;
            saveRecord();
            updateScoreUI();
            Toast.makeText(this, "Rekord törölve!", Toast.LENGTH_SHORT).show();
        });
    }

    private void selectChoice(int choice) {
        selectedPlayerChoice = choice;
        setChoiceImage(choice, playerImage);
        resultText.setText("Kész! Most rázd meg!");
        botImage.setImageResource(R.drawable.bot);
    }

    private void finalizeBattle() {
        if (selectedPlayerChoice == -1) {
            Toast.makeText(this, "Válassz előbb!", Toast.LENGTH_SHORT).show();
            return;
        }

        int botChoice = new Random().nextInt(3);
        setChoiceImage(botChoice, botImage);


        if (selectedPlayerChoice == botChoice) {
            resultText.setText("DÖNTETLEN!");
        } else if ((selectedPlayerChoice == 0 && botChoice == 2) ||
                (selectedPlayerChoice == 1 && botChoice == 0) ||
                (selectedPlayerChoice == 2 && botChoice == 1)) {
            resultText.setText("NYERTÉL! 🎉");
            playerWins++;
        } else {
            resultText.setText("A BOT NYERT! 🤖");
            botWins++;
        }

        int currentDiff = playerWins - botWins;
        if (currentDiff > maxDiffRecord) {
            maxDiffRecord = currentDiff;
            saveRecord();
        }

        updateScoreUI();
        selectedPlayerChoice = -1;
    }

    private void saveRecord() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("maxDiff", maxDiffRecord);
        editor.apply();
    }

    private void updateScoreUI() {
        scoreText.setText("Te: " + playerWins + " | Bot: " + botWins);
        highScoreText.setText("Legnagyobb W: " + maxDiffRecord +" pontal");
    }

    private void setChoiceImage(int choice, ImageView imageView) {
        if (choice == 0) imageView.setImageResource(R.drawable.ko);
        else if (choice == 1) imageView.setImageResource(R.drawable.papir);
        else if (choice == 2) imageView.setImageResource(R.drawable.ollo);
    }

    private void resetCurrentGame() {
        playerWins = 0;
        botWins = 0;
        selectedPlayerChoice = -1;
        updateScoreUI();
        resultText.setText("Válassz, majd rázd meg!");
        playerImage.setImageResource(R.drawable.te);
        botImage.setImageResource(R.drawable.bot);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0], y = event.values[1], z = event.values[2];
        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;

        long currentTime = System.currentTimeMillis();
        if (acceleration > SHAKE_THRESHOLD && (currentTime - lastShakeTime > 1500)) {
            lastShakeTime = currentTime;
            finalizeBattle();
        }
    }

    @Override protected void onResume() {
        super.onResume(); sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }
    @Override protected void onPause() {
        super.onPause(); sensorManager.unregisterListener(this);
    }
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}