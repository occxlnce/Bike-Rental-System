package com.ayushxp.pedalcityapp;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    FrameLayout frameLayout;
    TextView nameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemid = item.getItemId();

                if (itemid == R.id.home){
                    loadFragment(new HomeFragment(), false);
                } else if (itemid == R.id.wallet) {
                    loadFragment(new WalletFragment(), false);
                } else if (itemid == R.id.help) {
                    loadFragment(new HelpFragment(), false);
                } else if (itemid == R.id.account) {
                    loadFragment(new AccountFragment(), false);
                }

                return true;
            }
        });

        loadFragment(new HomeFragment(), true);

    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Intent intent = getIntent();
        String nameData = intent.getStringExtra("name");
        String numberData = intent.getStringExtra("number");
        String dateData = intent.getStringExtra("date");

        Bundle bundle = new Bundle();
        bundle.putString("nameData",nameData);
        bundle.putString("numberData",numberData);
        bundle.putString("dateData",dateData);
        fragment.setArguments(bundle);

        if (isAppInitialized){
            fragmentTransaction.add(R.id.frameLayout, fragment);
        } else {
            fragmentTransaction.replace(R.id.frameLayout, fragment);
        }
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);

        Dialog ExitAppDialog = new Dialog(HomeActivity.this);
        ExitAppDialog.setContentView(R.layout.exit_app_dialog);
        ExitAppDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        MaterialButton ExitNo = ExitAppDialog.findViewById(R.id.exit_no_btn);
        MaterialButton ExitYes = ExitAppDialog.findViewById(R.id.exit_yes_btn);
        

        if (fragment instanceof HomeFragment) {
            ExitAppDialog.show();
            ExitNo.setOnClickListener(v -> ExitAppDialog.dismiss());

            ExitYes.setOnClickListener(v -> {
                ExitAppDialog.dismiss();
                finish();
            });

        } else if (fragment instanceof WalletFragment) {
            ExitAppDialog.show();
            ExitNo.setOnClickListener(v -> ExitAppDialog.dismiss());

            ExitYes.setOnClickListener(v -> {
                ExitAppDialog.dismiss();
                finish();
            });

        } else if (fragment instanceof HelpFragment) {
            ExitAppDialog.show();
            ExitNo.setOnClickListener(v -> ExitAppDialog.dismiss());

            ExitYes.setOnClickListener(v -> {
                ExitAppDialog.dismiss();
                finish();
            });

        } else if (fragment instanceof AccountFragment) {
            ExitAppDialog.show();
            ExitNo.setOnClickListener(v -> ExitAppDialog.dismiss());

            ExitYes.setOnClickListener(v -> {
                ExitAppDialog.dismiss();
                finish();
            });

        } else if (fragment instanceof OnBackPressedListener) {
            ((OnBackPressedListener) fragment).onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}