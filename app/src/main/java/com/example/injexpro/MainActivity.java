package com.example.injexpro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Handler handler;
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            setTheme(R.style.AppTheme); // Set the app theme before calling super.onCreate
            super.onCreate(savedInstanceState);
            Log.d(TAG, "Starting MainActivity");
            setContentView(R.layout.activity_main);

            handler = new Handler();
            startSplashScreen();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            handleFatalError(e);
        }
    }

    private void startSplashScreen() {
        try {
            Log.d(TAG, "Starting splash screen with delay: " + SPLASH_DELAY + "ms");
            handler.postDelayed(() -> {
                navigateToLogin();
            }, SPLASH_DELAY);
        } catch (Exception e) {
            Log.e(TAG, "Error in startSplashScreen: ", e);
            handleFatalError(e);
        }
    }

    private void navigateToLogin() {
        try {
            Log.d(TAG, "Navigating to LoginActivity");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to LoginActivity: ", e);
            handleFatalError(e);
        }
    }

    private void handleFatalError(Exception e) {
        // In a production app, you might want to show an error dialog
        // or navigate to an error activity
        finish();
    }

    @Override
    protected void onDestroy() {
        try {
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            super.onDestroy();
            Log.d(TAG, "MainActivity destroyed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: ", e);
        }
    }
}