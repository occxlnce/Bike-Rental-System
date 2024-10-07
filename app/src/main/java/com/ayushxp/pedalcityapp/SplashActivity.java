package com.ayushxp.pedalcityapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        Thread td = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(800);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    Intent intent = new Intent(SplashActivity.this , SplashBicycleActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }; td.start();
    }
}