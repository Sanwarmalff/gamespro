package com.miniverse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvLogo = findViewById(R.id.tvLogo);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Smooth fade-in animation for the logo and progress bar
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);

        tvLogo.startAnimation(fadeIn);
        progressBar.startAnimation(fadeIn);

        // Delay for 2.5 seconds, then transition to MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            // Smooth cross-fade transition between activities
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 2500);
    }
}
