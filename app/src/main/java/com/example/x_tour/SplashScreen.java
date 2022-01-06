package com.example.x_tour;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class SplashScreen extends AppCompatActivity {

    private ImageView splashScreen;
    int LOADING_TIME = 5000;
    private MediaPlayer splashJingle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        splashJingle = MediaPlayer.create(this, R.raw.splash_jingle);
        splashJingle.start();

        splashScreen = findViewById(R.id.splashScreen);
        Glide.with(this).load(R.drawable.splashscreen).into(splashScreen);

        Thread timer = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(LOADING_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        };

        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        splashJingle.release();
        finish();
    }
}