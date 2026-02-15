package com.example.armariocamara;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_splash);

            new Handler().postDelayed(() -> {
                try {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } catch (Exception e) {
                    Log.e("SplashActivity", "Error al abrir MainActivity: " + e.getMessage());
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }, 2000);

        } catch (Exception e) {
            Log.e("SplashActivity", "Error en onCreate: " + e.getMessage());
            // Si falla, ir directo a MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}