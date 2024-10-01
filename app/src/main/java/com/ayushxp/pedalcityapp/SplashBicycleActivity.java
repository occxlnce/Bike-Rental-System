package com.ayushxp.pedalcityapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashBicycleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_bicycle);
        getSupportActionBar().hide();

        Thread td = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    SharedPreferences preferences = getSharedPreferences("onboard", MODE_PRIVATE);
                    boolean checkSpf = preferences.getBoolean("flag", false);

                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                    if (firebaseUser != null) {
                        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("userdata")
                                .child(firebaseUser.getUid());

                        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    String name_data = dataSnapshot.child("name").getValue(String.class);
                                    String number_data = dataSnapshot.child("number").getValue(String.class);
                                    String date_data = dataSnapshot.child("date").getValue(String.class);

                                    Intent intent = new Intent(SplashBicycleActivity.this, HomeActivity.class);

                                    intent.putExtra("name", name_data);
                                    intent.putExtra("number", number_data);
                                    intent.putExtra("date", date_data);

                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(SplashBicycleActivity.this, GoogleSignin.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle database error if needed
                            }
                        });
                    } else {
                        Intent intent;
                        if (checkSpf) {
                            intent = new Intent(SplashBicycleActivity.this, GoogleSignin.class);
                        } else {
                            intent = new Intent(SplashBicycleActivity.this, OnboardingActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        td.start();
    }
}
