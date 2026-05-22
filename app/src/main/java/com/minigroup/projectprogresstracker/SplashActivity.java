package com.minigroup.projectprogresstracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.minigroup.projectprogresstracker.data.local.AppDatabaseHelper;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // TASK 1: FORCE DATABASE INITIALIZATION
        initializeDatabase();

        // Delay for 2 seconds then go to LoginActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    /**
     * Initialize SQLite database at app startup
     * This ensures database is created and visible in Database Inspector
     */
    private void initializeDatabase() {
        try {
            AppDatabaseHelper dbHelper = AppDatabaseHelper.getInstance(this);
            // Force database creation by calling getWritableDatabase()
            dbHelper.getWritableDatabase();
            Log.d(TAG, "✅ DATABASE INITIALIZED SUCCESSFULLY");
            Log.d(TAG, "Database: ProjectTracker.db");
            Log.d(TAG, "Tables: users, groups, tasks");
        } catch (Exception e) {
            Log.e(TAG, "❌ DATABASE INITIALIZATION FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}