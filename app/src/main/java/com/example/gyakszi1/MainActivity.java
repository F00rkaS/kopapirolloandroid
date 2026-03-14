package com.example.gyakszi1;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView playerImage, botImage;
    private TextView resultText, scoreText;
    private Button resetButton;

    private int playerWins = 0;
    private int botWins = 0;


    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;

    private long lastShakeTime = 0;
    private static final int SHAKE_THRESHOLD = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerImage = findViewById(R.id.playerImage);
        botImage = findViewById(R.id.botImage);
        resultText = findViewById(R.id.resultText);
        scoreText = findViewById(R.id.scoreText);
        resetButton = findViewById(R.id.resetButton);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        acceleration = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        resetButton.setOnClickListener(v -> resetGame());
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;

        long currentTime = System.currentTimeMillis();
        if (acceleration > SHAKE_THRESHOLD && (currentTime - lastShakeTime > 1000)) {
            lastShakeTime = currentTime;
            playGame();
        }
    }

    private void playGame() {
        Random random = new Random();
        int playerChoice = random.nextInt(3);
        int botChoice = random.nextInt(3);

        setChoiceImage(playerChoice, playerImage);
        setChoiceImage(botChoice, botImage);

        if (playerChoice == botChoice) {
            resultText.setText("DÖNTETLEN!");
        } else if ((playerChoice == 0 && botChoice == 2) ||
                (playerChoice == 1 && botChoice == 0) ||
                (playerChoice == 2 && botChoice == 1)) {
            resultText.setText("NYERTÉL! 🎉");
            playerWins++;
        } else {
            resultText.setText("A BOT NYERT! 🤖");
            botWins++;
        }

        scoreText.setText("Te: " + playerWins + " | Bot: " + botWins);
    }

    private void setChoiceImage(int choice, ImageView imageView) {
        if (choice == 0) imageView.setImageResource(R.drawable.ko);
        else if (choice == 1) imageView.setImageResource(R.drawable.papir);
        else if (choice == 2) imageView.setImageResource(R.drawable.ollo);
    }

    private void resetGame() {
        playerWins = 0;
        botWins = 0;
        scoreText.setText("Te: 0 | Bot: 0");
        resultText.setText("Rázd meg a játékhoz!");
        resultText.setTextColor(Color.BLACK);
        playerImage.setImageResource(R.drawable.te);
        botImage.setImageResource(R.drawable.bot);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}