package com.ayushxp.pedalcityapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectedBicycleFragment extends Fragment {

    private int bicycleLogoId, bicycleImageId;
    private String bicycleType, bicycleName, priceText, perMinText;

    //RecyclerView & Adapters
    private RecyclerView recyclerView;
    private BicycleNumbersListAdapter adapter;
    private List<BicycleNumber> bicycleList;

    //Firebase
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference bicycleRef, rentalRef, userRentalRef;
    private String bicycleId, userId, bicycleRate, bicyclePerMin;
    private int bicycleImageID;

    //Bluetooth Request
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1002;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selected_bicycle, container, false);

        // Set onClickListener to back button
        ImageButton backButton = view.findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });


        // Retrieve data from arguments
        if (getArguments() != null) {
            bicycleLogoId = getArguments().getInt("bicycleLogoId");
            bicycleImageId = getArguments().getInt("bicycleImageId");
            bicycleType = getArguments().getString("bicycleType");
            bicycleName = getArguments().getString("bicycleName");
            priceText = getArguments().getString("priceText");
            perMinText = getArguments().getString("perMinText");
            bicycleId = getArguments().getString("bicycle_id");
            Log.d("bicycleId", bicycleId);
        }

        // Initialize views
        ImageView bicycleLogoView = view.findViewById(R.id.selected_bicycle_logo);
        ImageView bicycleImageView = view.findViewById(R.id.selected_bicycle_image);
        TextView bicycleTypeText = view.findViewById(R.id.selected_bicycle_type);
        TextView modelName = view.findViewById(R.id.model_name);
        TextView bicycleNameText = view.findViewById(R.id.selected_bicycle_name);
        TextView priceTextView = view.findViewById(R.id.selected_bicycle_price);
        TextView perMinTextView = view.findViewById(R.id.selected_bicycle_per_min);

        // Set data to views
        bicycleLogoView.setImageResource(bicycleLogoId);
        bicycleImageView.setImageResource(bicycleImageId);
        bicycleTypeText.setText(bicycleType);
        priceTextView.setText(priceText);
        perMinTextView.setText(perMinText);

        if (bicycleName != null) {
            bicycleNameText.setText(bicycleName);
        } else {
            modelName.setVisibility(View.GONE);
            bicycleNameText.setVisibility(View.GONE);

            // Reduce the width and height of the selected_bicycle_image by 30dp
            ViewGroup.LayoutParams layoutParams = bicycleImageView.getLayoutParams();
            float scale = getResources().getDisplayMetrics().density;
            int newWidth = (int) (layoutParams.width - 30 * scale + 0.5f);
            int newHeight = (int) (layoutParams.height - 30 * scale + 0.5f);
            layoutParams.width = newWidth;
            layoutParams.height = newHeight;
            bicycleImageView.setLayoutParams(layoutParams);
        }


        //RecyclerViews Initialization & Adapter
        recyclerView = view.findViewById(R.id.recycler_view_bicycles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bicycleList = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        bicycleRef = mDatabase.getReference("bicycleData").child(bicycleId).child("BicycleNumbers");
        rentalRef = mDatabase.getReference("rentalData");
        userId = mAuth.getCurrentUser().getUid();
        bicycleRate = priceText;
        bicyclePerMin = perMinText;
        bicycleImageID = bicycleImageId;
        adapter = new BicycleNumbersListAdapter(bicycleList, bicycleRef, rentalRef, userId, bicycleId, bicycleRate, bicyclePerMin, bicycleImageID);
        recyclerView.setAdapter(adapter);


        fetchBicycleNumbers();

        userRentalRef = rentalRef.child(userId);
        userRentalRef.child("ongoingRide").child("startTime").removeValue();


        userRentalRef.child("ongoingRide").child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    userRentalRef.child("ongoingRide").child("startTime").removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }

    private void fetchBicycleNumbers() {

        bicycleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bicycleList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String number = snapshot.getKey();
                    Log.d("firebase bicycle number", number);
                    String status = snapshot.child("Status").getValue(String.class);
                    Log.d("firebase bicycle status", status);

                    if (number != null && status != null) {
                        bicycleList.add(new BicycleNumber(number, status));
                    } else {
                        Log.w("fetchBicycleNumbers", "Null value encountered: number=" + number + ", status=" + status);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Firebase", "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 || requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the operation
            } else {
                Toast.makeText(getContext(), "All Permissions are required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth enabled, proceed with the operation
            } else {
                Toast.makeText(getContext(), "Bluetooth is required to Unlock Bicycle", Toast.LENGTH_SHORT).show();
            }
        }
    }

}