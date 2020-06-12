package com.example.carl.ui_loadingview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final LoadingView loadingView = findViewById(R.id.loading_view);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingView.disappear();
            }
        }, 3000);
    }
}
