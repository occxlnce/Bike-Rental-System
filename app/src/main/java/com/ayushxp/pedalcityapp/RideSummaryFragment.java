package com.ayushxp.pedalcityapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RideSummaryFragment extends Fragment implements OnBackPressedListener {

    //Firebase
    private FirebaseDatabase mDatabase;
    private DatabaseReference rentalRef, walletRef;
    private String userId;

    //Views
    private ImageButton backButton;
    private ImageView bicycleImage;
    private TextView rentalID, bicycleNum, startTime, endTime, rideDuration, amount, existingBalance, rentalAmount, currentBalance;
    private MaterialCardView likeButton, dislikeButton;
    private boolean isLikeButtonClicked, isDislikeButtonClicked;
    private MaterialButton doneButton;


    //Variables
    private int bicycleImgID;
    private String rentalIdValue, bicycleNumberValue, startTimeValue, endTimeValue;
    private String rideDurationValue, amountValue, existingBalanceValue, currentBalanceValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_summary, container, false);

        //Initialize Views
        //Image Button
        backButton = view.findViewById(R.id.back_btn);
        //Image View
        bicycleImage = view.findViewById(R.id.bicycle_img);
        //Text Views
        rentalID = view.findViewById(R.id.rental_id_tv);
        bicycleNum = view.findViewById(R.id.bicycle_number_tv);
        startTime = view.findViewById(R.id.start_time_tv);
        endTime = view.findViewById(R.id.end_time_tv);
        rideDuration = view.findViewById(R.id.duration_tv);
        amount = view.findViewById(R.id.amount_tv1);
        rentalAmount = view.findViewById(R.id.amount_tv2);
        existingBalance = view.findViewById(R.id.existing_balance_tv);
        currentBalance = view.findViewById(R.id.current_balance_tv);
        //Buttons
        likeButton = view.findViewById(R.id.thumb_up);
        dislikeButton = view.findViewById(R.id.thumb_down);
        //set boolean for like/dislike button clicks
        isLikeButtonClicked = false;
        isDislikeButtonClicked = false;
        //Material Button
        doneButton = view.findViewById(R.id.done_btn);


        //Firebase initialize
        mDatabase = FirebaseDatabase.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rentalRef = mDatabase.getReference("rentalData").child(userId).child("ongoingRide");
        walletRef = mDatabase.getReference("userWallet").child(userId).child("balance");
        //Fetch Firebase Data
        fetchFirebaseRentalData();


        //Set on Click listener for Like Button Card
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLikeButtonClicked) {
                    // Deselect the like button
                    likeButton.setStrokeColor(getResources().getColor(R.color.gray));
                    likeButton.setStrokeWidth(dpToPx(1.5f));
                    isLikeButtonClicked = false;
                } else {
                    // Select the like button
                    likeButton.setStrokeColor(getResources().getColor(R.color.green));
                    likeButton.setStrokeWidth(dpToPx(3));
                    isLikeButtonClicked = true;

                    // and deselect the dislike button
                    dislikeButton.setStrokeColor(getResources().getColor(R.color.gray));
                    dislikeButton.setStrokeWidth(dpToPx(1.5f));
                    isDislikeButtonClicked = false;
                }
            }
        });

        //Set on Click listener for Dislike Button Card
        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDislikeButtonClicked) {
                    // Deselect the dislike button
                    dislikeButton.setStrokeColor(getResources().getColor(R.color.gray));
                    dislikeButton.setStrokeWidth(dpToPx(1.5f));
                    isDislikeButtonClicked = false;
                } else {
                    // Select the dislike button
                    dislikeButton.setStrokeColor(getResources().getColor(R.color.dark_red));
                    dislikeButton.setStrokeWidth(dpToPx(3));
                    isDislikeButtonClicked = true;

                    // and deselect the like button
                    likeButton.setStrokeColor(getResources().getColor(R.color.gray));
                    likeButton.setStrokeWidth(dpToPx(1.5f));
                    isLikeButtonClicked = false;
                }
            }
        });


        //Set on Click listener for Ui Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, new HomeFragment());
                transaction.commit();
            }
        });


        //Set on Click listener for Done Button
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//                transaction.replace(R.id.frameLayout, new HomeFragment());
//                transaction.commit();

                getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        return view;
    }

    private int dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void fetchFirebaseRentalData() {

        rentalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Extract data
                    bicycleImgID = snapshot.child("bicycleImageId").getValue(Integer.class);
                    rentalIdValue = snapshot.child("rentalId").getValue().toString();
                    bicycleNumberValue = snapshot.child("bicycleNumber").getValue().toString();
                    startTimeValue = snapshot.child("rideStartTime").getValue().toString();
                    endTimeValue = snapshot.child("rideEndTime").getValue().toString();
                    rideDurationValue = snapshot.child("rideDuration").getValue().toString();
                    amountValue = snapshot.child("rideAmount").getValue().toString();

                    //Update UI
                    bicycleImage.setImageResource(bicycleImgID);
                    rentalID.setText(rentalIdValue);
                    bicycleNum.setText(bicycleNumberValue);
                    startTime.setText(startTimeValue);
                    endTime.setText(endTimeValue);
                    rideDuration.setText(rideDurationValue);
                    amount.setText(amountValue);
                    rentalAmount.setText(amountValue);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        walletRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int balanceInt = snapshot.getValue(Integer.class);
                    int amountInt = Integer.parseInt(amountValue.replace("₹", "").trim());

                    //Set string values and calculate
                    existingBalanceValue = "₹" + (balanceInt + amountInt);
                    currentBalanceValue = "₹" + balanceInt;

                    //Update UI
                    existingBalance.setText(existingBalanceValue);
                    currentBalance.setText(currentBalanceValue);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }



    @Override
    public void onBackPressed(){
//        // Navigate directly to the HomeFragment
//        FragmentManager fragmentManager = getParentFragmentManager();
//        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // Clear the back stack
//        fragmentManager.beginTransaction()
//                .replace(R.id.frameLayout, new HomeFragment())
//                .commit();

        // Navigate to HomeFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, new HomeFragment());
        transaction.commit();
    }

}