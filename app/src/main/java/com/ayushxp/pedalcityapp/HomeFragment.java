package com.ayushxp.pedalcityapp;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.Manifest.permission;
import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PinConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

//imports for weather
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class HomeFragment extends Fragment implements OnMyLocationButtonClickListener,
        OnMyLocationClickListener, OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    /**
     * Request code for location permission request.
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    /**
     * Flag indicating whether a requested permission has been denied after returning in {@link
     * #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;
    private GoogleMap map;
    private LatLng current_Location;
    private LatLng ruparel_location, ruparel_location2, shivaji_park, dadar_west, dadar_east, siddhi_vinayak, grant_road, churchgate, csmt;
    LocationRequest locationRequest;
    View mapView;
    ImageButton mapType;
    private Button rent;
    private boolean isSatellite;
    Dialog dialog, dialog2;
    BottomSheetDialog sheetDialog;
    private DatabaseReference qrCodesRef;

    //weather instance variables
    private ImageView weatherIcon;
    private TextView cityCondition, cityTemperature;
    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    private static final String API_KEY = "fc0aeeff0eb12df4b1da100cefcd500e";
    private static final String CITY_NAME = "Mumbai";

    //Ongoing Ride Widget Layout
    private LinearLayout ongoingRideLayout;
    private ImageView bicycleImg;
    private TextView rentalId, rideAmount;
    private Chronometer duration;
    private ImageButton headlightBtn;
    private boolean isOn = false;
    private MaterialButton endRideBtn;
    private LinearLayout allDetails;

    //Ongoing Rental Firebase Reference
    private String userId, bicycleID, bicycleNumberValue, RateValue, perMinValue;
    private FirebaseDatabase mDatabase;
    private DatabaseReference rentalRef, bicycleNumberRef, walletRef;

    // Create a list to hold the tasks
    private List<Task<Void>> tasks = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Get a handle to the fragment and register the callback.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        //Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        // Initialize QR Code reference
        qrCodesRef = mDatabase.getReference().child("qrCodes");

        // Initialize Weather Views
        weatherIcon = view.findViewById(R.id.img_city_condition);
        cityCondition = view.findViewById(R.id.txt_city_condition);
        cityTemperature = view.findViewById(R.id.txt_city_temp);

        // Fetch weather data
        fetchWeatherData(CITY_NAME);

        //Ongoing Rental Firebase Reference
        rentalRef = mDatabase.getReference("rentalData").child(userId);
        walletRef = mDatabase.getReference("userWallet").child(userId);
        fetchFirebaseRentalData();

        //Initialize OngoingRide Widget
        ongoingRideLayout = view.findViewById(R.id.ongoing_ride_home);
        bicycleImg = view.findViewById(R.id.bicycle_img);
        rentalId = view.findViewById(R.id.rental_id_tv);
        duration = view.findViewById(R.id.duration_tv);
        rideAmount = view.findViewById(R.id.amount_tv);
        headlightBtn = view.findViewById(R.id.headlight_btn);
        endRideBtn = view.findViewById(R.id.end_ride_btn);
        allDetails = view.findViewById(R.id.all_details);

        //Headlight button
        headlightBtn.setOnClickListener(new View.OnClickListener() {
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

        //All details button
        allDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, new OngoingRideFragment());
//                transaction.addToBackStack(null); // Add to back stack to allow back navigation
                transaction.commit();
            }
        });

        Dialog EndRideDialog = new Dialog(requireContext());
        EndRideDialog.setContentView(R.layout.end_ride_dialog);
        EndRideDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button EndYes = EndRideDialog.findViewById(R.id.end_yes_btn);
        Button EndNo = EndRideDialog.findViewById(R.id.end_no_btn);

        //End Ride Button
        endRideBtn.setOnClickListener(new View.OnClickListener() {
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
                        endRide(EndRideDialog);
                    }
                });
            }
        });


        return view;
    } // onCreateView <-

    private void endRide(Dialog EndRideDialog) {

        // Update Wallet
        Task<Void> updateWalletTask = updateWallet(rideAmount.getText().toString(), walletRef);
        tasks.add(updateWalletTask);

        // Set Selected bicycle status to "Available"
        if (bicycleNumberRef != null) {
            bicycleNumberRef.child("Status").setValue("Available");
            Log.d("HomeFragment", "Bicycle status set to 'Available'");
        } else {
            Log.e("HomeFragment", "bicycleNumberRef is null, cannot set status");
        }

        // Set anyOngoingRide? - 'false'
        rentalRef.child("anyOngoingRide?").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d("HomeFragment", "anyOngoingRide value set to 'false'");
                } else {
                    Log.e("HomeFragment", "Failed to set value of anyOngoingRide to 'false'");
                }
            }
        });

        // Set value of rideEndTime
        TimeStampApi.getCurrentTimestamp(new TimeStampApi.TimeStampCallback() {
            @Override
            public void onSuccess(String timestamp) {
                Log.d("TimeStampApi_Home", "rideEndTime value is set with timestamp");

                Task<Void> rideEndTimeTask = rentalRef.child("ongoingRide").child("rideEndTime").setValue(timestamp)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("HomeFragment", "rideEndTime value is set");
                                } else {
                                    Log.e("HomeFragment", "Failed to set value of rideEndTime");
                                }
                            }
                        });
                tasks.add(rideEndTimeTask);
            }
            @Override
            public void onFailure(String errorMsg) {
                Log.e("TimeStampApi_Home", "Failed to fetch timestamp: " + errorMsg);
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
                            Log.d("HomeFragment", "rideDuration value is set");
                        } else {
                            Log.e("HomeFragment", "Failed to set value of rideDuration");
                        }
                    }
                });
        tasks.add(rideDurationTask);

        // Set ride Amount to firebase
        Task<Void> rideAmountTask = rentalRef.child("ongoingRide").child("rideAmount").setValue(rideAmount.getText().toString())
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

//        // Remove startTime value
//        Log.d("OngoingRideFragment", "Attempting to remove startTime from Firebase");
//        rentalRef.child("ongoingRide").child("startTime").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    Log.d("OngoingRideFragment", "startTime successfully removed from Firebase");
//
//                    // Check if startTime still exists
//                    rentalRef.child("ongoingRide").child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (snapshot.exists()) {
//                                Log.e("OngoingRideFragment", "Unexpected startTime value found after removal attempt: " + snapshot.getValue(Long.class));
//                            } else {
//                                Log.d("OngoingRideFragment", "Confirmed startTime removal from Firebase");
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                            Log.e("OngoingRideFragment", "Error checking startTime after removal attempt", error.toException());
//                        }
//                    });
//                } else {
//                    Log.e("OngoingRideFragment", "Failed to remove startTime from Firebase", task.getException());
//                }
//            }
//        });

        //startTime Removal
        rentalRef.child("ongoingRide").child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Task<Void> startTimeRemove = rentalRef.child("ongoingRide").child("startTime").removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("HomeFragment", "startTime value of ongoingRide is removed");
                            } else {
                                Log.e("HomeFragment", "Failed to remove startTime value of ongoingRide", task.getException());
                            }
                        }
                    });
                    tasks.add(startTimeRemove);
                    Log.d("HomeFragment", "startTime value of ongoingRide is removed");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Error removing startTime", error.toException());
            }
        });


        // Wait for all tasks to complete
        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                if (task.isSuccessful()) {
                    Log.d("HomeFragment", "All tasks completed successfully");
                    EndRideDialog.dismiss();

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.frameLayout, new RideSummaryFragment())
                            .commit();
                } else {
                    Log.e("HomeFragment", "One or more tasks failed");
                }
            }
        });

    } //endRide Method <-

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
                                Log.d("HomeFragment", "New Balance updated in wallet");

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

    private void fetchFirebaseRentalData() {
        rentalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //Extract data
                    Boolean anyOngoingRide = snapshot.child("anyOngoingRide?").getValue(Boolean.class);

                    if (anyOngoingRide == null){
                        ongoingRideLayout.setVisibility(View.GONE);
                    }

                    //Update UI
                    if (anyOngoingRide){
                        rent.setVisibility(View.GONE);
                        ongoingRideLayout.setVisibility(View.VISIBLE);
                        //moveCameraToCurrentLocation(); //Move camera to user's current location

                        int bicycleImgID = snapshot.child("ongoingRide").child("bicycleImageId").getValue(Integer.class);
                        String rentalIdValue = String.valueOf(snapshot.child("ongoingRide").child("rentalId").getValue(Integer.class));
                        bicycleID = snapshot.child("ongoingRide").child("bicycleId").getValue(String.class);
                        bicycleNumberValue = snapshot.child("ongoingRide").child("bicycleNumber").getValue(String.class);
                        RateValue = snapshot.child("ongoingRide").child("bicycleRate").child("rate").getValue(String.class);
                        perMinValue = snapshot.child("ongoingRide").child("bicycleRate").child("mins").getValue(String.class);

                        // Initialize bicycleNumberRef only if bicycleNumberValue is not null
                        if (bicycleNumberValue != null) {
                            bicycleNumberRef = mDatabase.getReference("bicycleData").child(bicycleID).child("BicycleNumbers").child(bicycleNumberValue);
                        } else {
                            Log.e("HomeFragment", "bicycleNumberValue is null, cannot initialize bicycleNumberRef");
                        }

                        bicycleImg.setImageResource(bicycleImgID);
                        rentalId.setText(rentalIdValue);

                        setupChronometer();

                    } else {
                        rent.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching data", error.toException());
            }
        });
    }

    //Set Up Chronometer
    private void setupChronometer() {
        rentalRef.child("ongoingRide").child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long startTime = snapshot.getValue(Long.class);
                    fetchCurrentTime(new TimeCallback() {
                        @Override
                        public void onTimeFetched(long currentTime) {
                            long elapsedTime = currentTime - startTime;
                            if (elapsedTime < 0) {
                                Log.w("HomeFragment", "Negative elapsedTime detected: " + elapsedTime + ", adjusting chronometer base.");
                                duration.setBase(SystemClock.elapsedRealtime());
                            } else {
                                duration.setBase(SystemClock.elapsedRealtime() - elapsedTime);
                            }
                            duration.start();
                        }
                    });
                } else {
                    Log.d("HomeFragment", "startTime does not exist, setting new startTime");
                    long startTime = getCurrentTimeFromAPI();
                    rentalRef.child("ongoingRide").child("startTime").setValue(startTime);
                    duration.setBase(SystemClock.elapsedRealtime());
                    duration.start();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Failed to read startTime from Firebase", error.toException());
            }
        });

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
        rideAmount.setText(String.format(Locale.getDefault(), "₹%d", amountValueInt));
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
                    Log.d("HomeFragment", "Fetched current time: " + currentTime);
                    callback.onTimeFetched(currentTime);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("HomeFragment", "Failed to parse time from API", e);
                    callback.onTimeFetched(System.currentTimeMillis());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Log.e("HomeFragment", "Failed to fetch time from API", error);
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


    //Fetch Weather Data
    private void fetchWeatherData(String city) {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = WEATHER_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject main = response.getJSONObject("main");
                    JSONObject weather = response.getJSONArray("weather").getJSONObject(0);

                    String icon = weather.getString("icon");
                    double tempDouble = main.getDouble("temp");
                    int temp = (int) Math.round(tempDouble);  // Round off the temperature
                    String condition = weather.getString("description");
                    //Capitalize 1st letter of condition
                    String capital_condition = condition.substring(0, 1).toUpperCase() + condition.substring(1).toLowerCase();

                    // Update UI with fetched data
                    //Set Weather Icon
                    Log.d("Weather Icon", icon);
                    int resID = getResources().getIdentifier("icon_" + icon, "drawable", requireContext().getPackageName());
                    weatherIcon.setImageResource(resID);

                    //Set Temperature Text
                    cityTemperature.setText(temp + "°C");

                    //Set Condition Text
                    if (condition.length() > 4){

                        //Remove Align End parameter of the textview & add 20dp padding to temperature textview
                        RelativeLayout.LayoutParams cityConditionParam = (RelativeLayout.LayoutParams) cityCondition.getLayoutParams();
                        cityConditionParam.removeRule(RelativeLayout.ALIGN_END);
                        cityCondition.setLayoutParams(cityConditionParam);
                        cityTemperature.setPadding(cityTemperature.getPaddingLeft(), cityTemperature.getPaddingTop(), cityTemperature.getPaddingRight(), dpToPx(20));

                        cityCondition.setText(capital_condition);
                    } else {
                        cityCondition.setText(capital_condition);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // Handle the failure and alert the user to retry
            }
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        //setting location button position
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            locationButton.setVisibility(View.GONE);
        }

        mapType = requireView().findViewById(R.id.mapType);
        Button customLocationButton = requireView().findViewById(R.id.customLocationButton);

        ruparel_location = new LatLng(19.02580635596756, 72.84419511234479);
        BitmapDescriptor customMarker = getCustomMarker(Color.TRANSPARENT, R.drawable.marker);
        map.addMarker(new MarkerOptions()
                .position(ruparel_location)
                .title("Ruparel College").snippet("Back Gate")
                .icon(customMarker));
        map.setBuildingsEnabled(true);
        map.setPadding(30, 200, 50, 450);
        map.moveCamera(CameraUpdateFactory.newLatLng(ruparel_location));
        map.moveCamera(CameraUpdateFactory.zoomTo(18));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(ruparel_location, 18));
        enableMyLocation();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        ruparel_location2 = new LatLng(19.027886, 72.845909);
        map.addMarker(new MarkerOptions()
                .position(ruparel_location2)
                .title("Ruparel College").snippet("Front Gate")
                .icon(customMarker));

        shivaji_park = new LatLng(19.026928, 72.839528);
        map.addMarker(new MarkerOptions()
                .position(shivaji_park)
                .title("Shivaji Park").snippet("near Open Gym")
                .icon(customMarker));

        dadar_west = new LatLng(19.020927, 72.842472);
        map.addMarker(new MarkerOptions()
                .position(dadar_west)
                .title("Dadar West").snippet("near Veer Kotwal Udyan")
                .icon(customMarker));

        dadar_east = new LatLng(19.017497, 72.844232);
        map.addMarker(new MarkerOptions()
                .position(dadar_east)
                .title("Dadar East").snippet("near Swaminarayan Temple")
                .icon(customMarker));

        siddhi_vinayak = new LatLng(19.017442, 72.830784);
        map.addMarker(new MarkerOptions()
                .position(siddhi_vinayak)
                .title("Siddhi Vinayak").snippet("near Gate")
                .icon(customMarker));

        grant_road = new LatLng(18.962122, 72.813928);
        map.addMarker(new MarkerOptions()
                .position(grant_road)
                .title("Grant Road West").snippet("near Signal")
                .icon(customMarker));

        churchgate = new LatLng(18.933254, 72.827800);
        map.addMarker(new MarkerOptions()
                .position(churchgate)
                .title("Churchgate Station").snippet("end of the Station")
                .icon(customMarker));

        csmt = new LatLng(18.939846, 72.835035);
        map.addMarker(new MarkerOptions()
                .position(csmt)
                .title("CSMT Terminus").snippet("Headquarter Gate")
                .icon(customMarker));


        if (ActivityCompat.checkSelfPermission(requireContext(), permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);


        //dialog for Rent Bicycle Option, - Not using this now as I've used Bottom Sheet for Rent button ☻
//        dialog = new Dialog(requireContext());
//        dialog.setContentView(R.layout.rent_bicycle_popup);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        Button scan_qr_code = dialog.findViewById(R.id.scan_QR_btn);
//        Button enter_bicycle_number = dialog.findViewById(R.id.bicycle_number_btn);

        // Set click listener for the Rent a Bicycle button
        rent = requireView().findViewById(R.id.rent_bicycle_btn);
        rent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetStyle);
                View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_bottom_sheet,
                        (LinearLayout) requireView().findViewById(R.id.bottom_sheet));
                sheetDialog.setContentView(sheetView);
                sheetDialog.show();

                MaterialCardView scan_qr = sheetView.findViewById(R.id.scan_qr);
                MaterialCardView enter_bicycle_number = sheetView.findViewById(R.id.enter_bicycle_number);
                MaterialCardView choose_bicycle = sheetView.findViewById(R.id.choose_bicycle);


                scan_qr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        IntentIntegrator integrator = new IntentIntegrator(requireActivity());
                        integrator.setCaptureActivity(CaptureAct.class);
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                        integrator.setPrompt("Scan a QR Code");
                        integrator.setBeepEnabled(true);
                        integrator.setOrientationLocked(true);
                        integrator.initiateScan();
                    }
                });

                //dialog Enter Bicycle Number
                dialog2 = new Dialog(requireContext());
                dialog2.setContentView(R.layout.bicycle_number_popup);
                dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                EditText bicycle_number = dialog2.findViewById(R.id.bicycle_number);
                Button submit_btn = dialog2.findViewById(R.id.submit_btn);

                enter_bicycle_number.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog2.show();
                        bicycle_number.setText("");

                        submit_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String bicycle_num = bicycle_number.getText().toString();
                                if (!bicycle_num.isEmpty()){

                                    fetchQRCodeData(bicycle_num);
                                    dialog2.dismiss();
                                    sheetDialog.dismiss();
                                } else {
                                    Toast.makeText(requireContext(), "Please Enter bicycle number", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                choose_bicycle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        sheetDialog.dismiss();

                        // Create an instance of the ChooseBicycleFragment
                        ChooseBicycleFragment chooseBicycleFragment = new ChooseBicycleFragment();
                        // Replace the current fragment with ChooseBicycleFragment
                        FragmentManager fragmentManager = getParentFragmentManager();
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                        android.R.anim.fade_in, android.R.anim.fade_out)
                                .replace(R.id.frameLayout, chooseBicycleFragment)
                                .addToBackStack(null) // Optional: add this transaction to the back stack so the user can navigate back
                                .commit();
                    }
                });

            }
        });



        // Set click listener for the custom location button
        customLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle custom location button click
                checkGps();
                moveCameraToCurrentLocation();
            }
        });

        //Code for setting Type of Map
        mapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSatellite){
                    isSatellite = true;
                    mapType.setImageDrawable(getResources().getDrawable(R.drawable.normalmap));
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else {
                    isSatellite = false;
                    mapType.setImageDrawable(getResources().getDrawable(R.drawable.satellite));
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });


        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return null; // Use default info window
            }

            @Nullable
            @Override
            public View getInfoContents(@NonNull Marker marker) {
                // Inflate custom info window layout
                View view = getLayoutInflater().inflate(R.layout.custom_info_window, null);

                ImageView infoImage = view.findViewById(R.id.info_image);
                TextView title = view.findViewById(R.id.info_window_title);
                TextView desc = view.findViewById(R.id.info_window_description);

                if (marker.getPosition().equals(ruparel_location)) {
                    infoImage.setImageDrawable(getResources().getDrawable(R.drawable.backgate));
                    title.setText("Ruparel College");
                    desc.setText("Back Gate");
                } else if (marker.getPosition().equals(ruparel_location2)) {
                    infoImage.setImageDrawable(getResources().getDrawable(R.drawable.frontgate));
                    title.setText("Ruparel College");
                    desc.setText("Front Gate");
                } else if (marker.getPosition().equals(shivaji_park)) {
                    title.setText("Shivaji Park");
                    desc.setText("near Open Gym");
                } else if (marker.getPosition().equals(dadar_west)) {
                    title.setText("Dadar West");
                    desc.setText("near Veer Kotwal Udyan");
                } else if (marker.getPosition().equals(dadar_east)) {
                    title.setText("Dadar East");
                    desc.setText("near Swaminarayan Temple");
                } else if (marker.getPosition().equals(siddhi_vinayak)) {
                    title.setText("Siddhi Vinayak");
                    desc.setText("near Gate");
                } else if (marker.getPosition().equals(grant_road)) {
                    title.setText("Grant Road West");
                    desc.setText("near Signal");
                } else if (marker.getPosition().equals(churchgate)) {
                    title.setText("Churchgate Station");
                    desc.setText("end of the Station");
                } else if (marker.getPosition().equals(csmt)) {
                    title.setText("CSMT Terminus");
                    desc.setText("Headquarter Gate");
                }


                return view;
            }
        });

        // Set OnInfoWindowClickListener to handle clicks on the info window
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                    // Check if location permission is granted
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    map.setMyLocationEnabled(true);

                    checkGps();
                // Get the user's current location
                Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(requireActivity()).getLastLocation();
                locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            current_Location = new LatLng(location.getLatitude(), location.getLongitude());
                            // Open Google Maps with directions
                            LatLng destinationLatLng = marker.getPosition();
                            Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(getContext(), "Current location not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                sheetDialog.dismiss();

                Log.d("Qr Scan", "Scanned QR Code: " + result.getContents());

                if (result.getContents().matches("\\d{4}")) {
                    String scannedResult = result.getContents();
                    // Handle the scanned result
                    fetchQRCodeData(scannedResult);
                    Toast.makeText(requireContext(), "Scanned Result: " + scannedResult, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Scanned result is null", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == 101){
            if (resultCode == RESULT_OK){
                Toast.makeText(requireContext(), "GPS is enabled", Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(requireContext(), "GPS disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to fetch data from Firebase based on the scanned QR code value
    private void fetchQRCodeData(String scannedResult) {
        Log.d("Qr Scan", "Scanned QR Code: " + scannedResult);

        qrCodesRef.child(scannedResult).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the QR code exists in the database
                if (dataSnapshot.exists()) {
                    // Retrieve the data from the database
                    Long number = dataSnapshot.child("number").getValue(Long.class);
                    String type = dataSnapshot.child("type").getValue(String.class);
                    Boolean available = dataSnapshot.child("available").getValue(Boolean.class);

                    String qrCodeData = "Number: " + number + "\nType: " + type + "\nAvailable: " + available;
                    Log.d("Qr Scanner", "Number: " + number + ", Type: " + type + ", Available: " + available);
                    Toast.makeText(requireContext(), qrCodeData, Toast.LENGTH_SHORT).show();
                } else {
                    // QR code does not exist in the database
                    Toast.makeText(requireContext(), "QR code data not found in Firebase", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(requireContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private BitmapDescriptor getCustomMarker(int color, int iconResource) {
        // Load the icon drawable
        Drawable iconDrawable = ContextCompat.getDrawable(requireContext(), iconResource);
        if (iconDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        // Set the size of the bitmap
        int width = 120; // Set your desired width
        int height = 150; // Set your desired height

        // Set the bounds for the icon
        iconDrawable.setBounds(0, 0, width, height);

        // Create a bitmap with the icon and background color
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawCircle(width / 2f, height / 2f, width / 2f, paint);
        iconDrawable.draw(canvas);

        // Return the custom bitmap descriptor
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private void moveCameraToCurrentLocation() {
        if (map != null) {
            // Check if location permission is granted
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);
            // Get the user's current location
            Task<Location> locationTask = LocationServices.getFusedLocationProviderClient(requireActivity()).getLastLocation();
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Move the camera to the user's current location
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                    } else {
                        Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void checkGps() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        Task<LocationSettingsResponse>locationSettingsResponseTask = LocationServices.getSettingsClient(requireContext())
                .checkLocationSettings(builder.build());

        locationSettingsResponseTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED){
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        try {
                            resolvableApiException.startResolutionForResult(requireActivity(),101);
                        } catch (IntentSender.SendIntentException sendIntentException) {
                            sendIntentException.printStackTrace();
                        }
                    }
                    if (e.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE){
                        Toast.makeText(requireContext(), "Setting not available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            return;
        }
        // 2. Otherwise, request location permissions from the user.
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE); //requestLocationPermission
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation(); // Enable the my location layer if the permission has been granted.
        } else {
            // Permission was denied. Display an error message.
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
    }
}