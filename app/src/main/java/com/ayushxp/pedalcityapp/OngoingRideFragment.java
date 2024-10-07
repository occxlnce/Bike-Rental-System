package com.ayushxp.pedalcityapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;


public class OngoingRideFragment extends Fragment implements OnBackPressedListener {

    //Firebase
    private FirebaseDatabase mDatabase;
    private DatabaseReference rentalRef, bicycleNumberRef, walletRef;
    private int bicycleImgID;
    private String userId, bicycleID, rentalIdValue, bicycleNumberValue, RateValue, perMinValue;

    //Views
    private ImageView bicycleImg;
    private Chronometer duration;
    private TextView amount, rentalId, bicycleNumber, rideRate, pauseRate;
    private ImageButton viewMapButton, pauseButton, unlockButton, replaceButton, headLightButton;
    private boolean isOn = false;
    private MaterialButton endRideButton;
    private ValueEventListener startTimeListener;
    public boolean isFragmentDestroy = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ongoing_ride, container, false);

        //Initialize views
        //Image View
        bicycleImg = view.findViewById(R.id.bicycle_img);
        //Text views
        duration = view.findViewById(R.id.duration_tv);
        amount = view.findViewById(R.id.amount_tv);
        rentalId = view.findViewById(R.id.rental_id_tv);
        bicycleNumber = view.findViewById(R.id.bicycle_number_tv);
        rideRate = view.findViewById(R.id.ride_rate_tv);
        pauseRate = view.findViewById(R.id.pause_rate_tv);
        //Image Button views
        viewMapButton = view.findViewById(R.id.view_map_btn);
        pauseButton = view.findViewById(R.id.pause_btn);
        unlockButton = view.findViewById(R.id.unlock_btn);
        replaceButton = view.findViewById(R.id.replace_btn);
        headLightButton = view.findViewById(R.id.headlight_btn);
        //Material button view
        endRideButton = view.findViewById(R.id.end_ride_btn);


        //Firebase initialize
        mDatabase = FirebaseDatabase.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rentalRef = mDatabase.getReference("rentalData").child(userId);
        walletRef = mDatabase.getReference("userWallet").child(userId);


        fetchDataFromFirebase(); //Fetch all rental data from firebase

        isFragmentDestroy = false;

        //Pause button
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireContext(), "This feature is coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        //Unlock button
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireContext(), "Your bicycle is already unlocked", Toast.LENGTH_SHORT).show();
            }
        });

        //Replace button
        replaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireContext(), "This feature is still under development", Toast.LENGTH_SHORT).show();
            }
        });

        //Headlight button
        headLightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOn) {
                    Toast.makeText(requireContext(), "HeadLight turned ON", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "HeadLight turned OFF", Toast.LENGTH_SHORT).show();
                }
                isOn = !isOn;
            }
        });

        //View Map button
        viewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Clear all back stack & get to the home fragment (root fragment)
//                getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                // Navigate to HomeFragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, new HomeFragment());
                transaction.commit();
            }
        });


        Dialog EndRideDialog = new Dialog(requireContext());
        EndRideDialog.setContentView(R.layout.end_ride_dialog);
        EndRideDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button EndYes = EndRideDialog.findViewById(R.id.end_yes_btn);
        Button EndNo = EndRideDialog.findViewById(R.id.end_no_btn);

        //END RIDE button
        endRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //End Ride Dialog
                EndRideDialog.show();
                //End No button Dismisses the dialog
                EndNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EndRideDialog.dismiss();
                    }
                });

                //End Yes button ends the ride
                EndYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // Create a list to hold the tasks
                        List<Task<Void>> tasks = new ArrayList<>();

                        // Update Wallet
                        Task<Void> updateWalletTask = updateWallet(amount.getText().toString(), walletRef);
                        tasks.add(updateWalletTask);

                        // Set Selected bicycle status to "Available"
                        if (bicycleNumberRef != null) {
                            bicycleNumberRef.child("Status").setValue("Available");
                        } else {
                            Log.e("OngoingRideFragment", "bicycleNumberRef is null, cannot set status");
                        }

                        // Set anyOngoingRide? - 'false'
                        rentalRef.child("anyOngoingRide?").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("OngoingFragment", "anyOngoingRide value set to 'false'");
                                } else {
                                    Log.e("OngoingFragment", "Failed to set value of anyOngoingRide to 'false'");
                                }
                            }
                        });

                        // Set value of rideEndTime
                        TimeStampApi.getCurrentTimestamp(new TimeStampApi.TimeStampCallback() {
                            @Override
                            public void onSuccess(String timestamp) {
                                Log.d("TimeStampApi_OngoingRide", "rideEndTime value is set with timestamp");

                                Task<Void> rideEndTimeTask = rentalRef.child("ongoingRide").child("rideEndTime").setValue(timestamp)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("OngoingRideFragment", "rideEndTime value is set");
                                                } else {
                                                    Log.e("OngoingRideFragment", "Failed to set value of rideEndTime");
                                                }
                                            }
                                        });
                                tasks.add(rideEndTimeTask);

                            }
                            @Override
                            public void onFailure(String errorMsg) {
                                Log.e("TimeStampApi_OngoingRide", "Failed to fetch timestamp: " + errorMsg);
                            }
                        });

                        // Stop Chronometer
                        duration.stop();
                        long elapsedMillis = SystemClock.elapsedRealtime() - duration.getBase();
                        int hours = (int) (elapsedMillis / 3600000);
                        int minutes = (int) (elapsedMillis % 3600000) / 60000;
                        int seconds = (int) ((elapsedMillis % 3600000) % 60000) / 1000;
                        String totalDuration = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                        // Set rideDuration to firebase
                        Task<Void> rideDurationTask = rentalRef.child("ongoingRide").child("rideDuration").setValue(totalDuration)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("OngoingRideFragment", "rideDuration value is set");
                                        } else {
                                            Log.e("OngoingRideFragment", "Failed to set value of rideDuration");
                                        }
                                    }
                                });
                        tasks.add(rideDurationTask);


                        // Set ride Amount to firebase
                        Task<Void> rideAmountTask = rentalRef.child("ongoingRide").child("rideAmount").setValue(amount.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("OngoingRideFragment", "rideAmount value is set");
                                        } else {
                                            Log.e("OngoingRideFragment", "Failed to set value of rideAmount");
                                        }
                                    }
                                });
                        tasks.add(rideAmountTask);


                        // Wait for all tasks to complete
                        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                                if (task.isSuccessful()) {
                                    Log.d("OngoingFragment", "All tasks completed successfully");
                                    EndRideDialog.dismiss();

                                    // Navigate to RideSummaryFragment
                                    FragmentManager fragmentManager = getParentFragmentManager();
                                    // Find the OngoingRideFragment in the back stack
                                    Fragment ongoingRideFragment = fragmentManager.findFragmentById(R.id.frameLayout);
                                    // Remove the fragment
                                    fragmentManager.beginTransaction().remove(ongoingRideFragment).commit();

                                    if (ongoingRideFragment != null) {

                                        rentalRef.child("ongoingRide").child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    rentalRef.child("ongoingRide").child("startTime").removeValue();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        // Navigate to RideSummaryFragment
                                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                        transaction.replace(R.id.frameLayout, new RideSummaryFragment());
                                        transaction.commit();

                                    } else {
                                        Log.e("OngoingRideFragment", "OngoingRideFragment not found in the back stack");
                                    }

                                    Log.d("OngoingRideFragment", "Attempting to remove startTime from Firebase");
                                    rentalRef.child("ongoingRide").child("startTime").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("OngoingRideFragment", "startTime successfully removed from Firebase");

                                                // Check if startTime still exists
                                                rentalRef.child("ongoingRide").child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            Log.e("OngoingRideFragment", "Unexpected startTime value found after removal attempt: " + snapshot.getValue(Long.class));
                                                        } else {
                                                            Log.d("OngoingRideFragment", "Confirmed startTime removal from Firebase");
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.e("OngoingRideFragment", "Error checking startTime after removal attempt", error.toException());
                                                    }
                                                });
                                            } else {
                                                Log.e("OngoingRideFragment", "Failed to remove startTime from Firebase", task.getException());
                                            }
                                        }
                                    });

                                    rentalRef.child("ongoingRide").child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                rentalRef.child("ongoingRide").child("startTime").removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                } else {
                                    Log.e("OngoingRideFragment", "One or more tasks failed");
                                }
                            }
                        });

                    }
                });

            }
        });


        return view;
    }

    private Task<Void> updateWallet(String rideAmount, DatabaseReference walletRef) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        walletRef.child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int amountInt = Integer.parseInt(rideAmount.replace("₹", "").trim());
                    int balanceInt = snapshot.getValue(Integer.class);

                    walletRef.child("balance").setValue(balanceInt - amountInt).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("OngoingRideFragment", "New Balance updated in wallet");

                                // Log transaction in Firebase
                                String transactionType = "Bicycle Rental";
                                Transaction transaction = new Transaction(transactionType, -amountInt);
                                transaction.getTimestampTask().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> transactionTask) {
                                        if (transactionTask.isSuccessful()) {
                                            walletRef.child("transactions").push().setValue(transaction)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> pushTask) {
                                                            if (pushTask.isSuccessful()) {
                                                                taskCompletionSource.setResult(null);
                                                            } else {
                                                                taskCompletionSource.setException(pushTask.getException());
                                                            }
                                                        }
                                                    });
                                        } else {
                                            taskCompletionSource.setException(transactionTask.getException());
                                        }
                                    }
                                });
                            } else {
                                Log.e("OngoingRideFragment", "Failed to update new balance in wallet", task.getException());
                                taskCompletionSource.setException(task.getException());
                            }
                        }
                    });

                } else {
                    Log.e("OngoingRideFragment", "Wallet balance not found");
                    taskCompletionSource.setException(new Exception("Wallet balance not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                taskCompletionSource.setException(error.toException());
            }
        });

        return taskCompletionSource.getTask();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rentalRef != null) {
            rentalRef.child("ongoingRide").child("startTime").removeEventListener(startTimeListener);
        }
        //rentalRef.child("ongoingRide").child("startTime").removeValue();
        isFragmentDestroy = true;
        Log.d("destroyed", "onDestroyView: true");
    }


    private void fetchDataFromFirebase() {
        rentalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Extract data from the dataSnapshot
                    bicycleID = dataSnapshot.child("ongoingRide").child("bicycleId").getValue(String.class);
                    bicycleImgID = dataSnapshot.child("ongoingRide").child("bicycleImageId").getValue(Integer.class);
                    rentalIdValue = String.valueOf(dataSnapshot.child("ongoingRide").child("rentalId").getValue(Integer.class));
                    bicycleNumberValue = dataSnapshot.child("ongoingRide").child("bicycleNumber").getValue(String.class);
                    RateValue = dataSnapshot.child("ongoingRide").child("bicycleRate").child("rate").getValue(String.class);
                    perMinValue = dataSnapshot.child("ongoingRide").child("bicycleRate").child("mins").getValue(String.class);

                    String rideRateValue = RateValue + " " + perMinValue;

                    // Update UI
                    bicycleImg.setImageResource(bicycleImgID);
                    // duration.setText();
                    // amount.setText();
                    rentalId.setText(rentalIdValue);
                    bicycleNumber.setText(bicycleNumberValue);
                    rideRate.setText(rideRateValue);


                    // Initialize bicycleNumberRef only if bicycleNumberValue is not null
                    if (bicycleNumberValue != null) {
                        bicycleNumberRef = mDatabase.getReference("bicycleData").child(bicycleID).child("BicycleNumbers").child(bicycleNumberValue);
                    } else {
                        Log.e("OngoingRideFragment", "bicycleNumberValue is null, cannot initialize bicycleNumberRef");
                    }

                    setupChronometer();

                } else {
                    Log.d("OngoingRideFragment", "No ongoing ride found for the user");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("OngoingRideFragment", "Failed to read data from Firebase", databaseError.toException());
            }
        });
    }

    private void setupChronometer() {
        startTimeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!isFragmentDestroy) {
                        long startTime = snapshot.getValue(Long.class);
                        fetchCurrentTime(new TimeCallback() {
                            @Override
                            public void onTimeFetched(long currentTime) {
                                long elapsedTime = currentTime - startTime;
                                if (elapsedTime < 0) {
                                    Log.w("OngoingRideFragment", "Negative elapsedTime detected: " + elapsedTime + ", adjusting chronometer base.");
                                    duration.setBase(SystemClock.elapsedRealtime());
                                } else {
                                    duration.setBase(SystemClock.elapsedRealtime() - elapsedTime);
                                }
                                duration.start();
                            }
                        });
                    }
                } else {
                    if (!isFragmentDestroy) {
                        Log.d("OngoingRideFragment", "startTime does not exist, setting new startTime");
                        long startTime = getCurrentTimeFromAPI();
                        rentalRef.child("ongoingRide").child("startTime").setValue(startTime);
                        duration.setBase(SystemClock.elapsedRealtime());
                        duration.start();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OngoingRideFragment", "Failed to read startTime from Firebase", error.toException());
            }
        };

        rentalRef.child("ongoingRide").child("startTime").addListenerForSingleValueEvent(startTimeListener);

        // Format the Chronometer to display hours, minutes, and seconds
        duration.setOnChronometerTickListener(chronometer -> {
            long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
            int hours = (int) (elapsedMillis / 3600000);
            int minutes = (int) (elapsedMillis - hours * 3600000) / 60000;
            int seconds = (int) (elapsedMillis - hours * 3600000 - minutes * 60000) / 1000;
            chronometer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));

            // Calculate the amount based on RateValue and perMinValue
            calculateAmount(elapsedMillis);
        });
    }

    private void calculateAmount(long elapsedMillis) {

        // Parse RateValue to extract the numeric int value
        int rateValueInt = Integer.parseInt(RateValue.replace("₹", "").trim());

        // Parse perMinValue and check equals to 'per min' to extract the duration value in milliseconds
        int perMinValueInt;
        if (perMinValue.trim().equals("per min")) {
            perMinValueInt = 60000; // 1 minute in milliseconds
        } else {
            perMinValueInt = 30 * 60000; // 30 minutes in milliseconds
        }

        // Calculate the total amount in int
        int amountValueInt = ((int) (elapsedMillis / perMinValueInt) + 1) * rateValueInt;

        // Update the amount TextView
        amount.setText(String.format(Locale.getDefault(), "₹%d", amountValueInt));
    }

    private void fetchCurrentTime(TimeCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://worldtimeapi.org/api/timezone/Asia/Kolkata", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String response = new String(responseBody);
                    JSONObject jsonObject = new JSONObject(response);
                    long currentTime = jsonObject.getLong("unixtime") * 1000; // Convert to milliseconds
                    Log.d("OngoingRideFragment", "Fetched current time: " + currentTime);
                    callback.onTimeFetched(currentTime);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("OngoingRideFragment", "Failed to parse time from API", e);
                    callback.onTimeFetched(System.currentTimeMillis());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Log.e("OngoingRideFragment", "Failed to fetch time from API", error);
                callback.onTimeFetched(System.currentTimeMillis());
            }
        });
    }

    private long getCurrentTimeFromAPI() {
        // This method should return the current time fetched from an API or fallback to system time
        // For simplicity, it returns the system time as a fallback
        return System.currentTimeMillis();
    }

    interface TimeCallback {
        void onTimeFetched(long currentTime);
    }

    @Override
    public void onBackPressed() {
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
