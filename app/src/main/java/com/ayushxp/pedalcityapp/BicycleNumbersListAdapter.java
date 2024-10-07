package com.ayushxp.pedalcityapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BicycleNumbersListAdapter extends RecyclerView.Adapter<BicycleNumbersListAdapter.BicycleViewHolder> {
    private List<BicycleNumber> bicycleList;
    private DatabaseReference bicycleRef, rentalRef;
    private String userId, bicycleId, bicycleRate, bicyclePerMin;
    private int bicycleImageID;
    private static final int REQUEST_ENABLE_BT = 1;

    public static class BicycleViewHolder extends RecyclerView.ViewHolder {
        public TextView bicycleNumber;
        public TextView bicycleStatus;
        public LinearLayout unlockButton;
        public ImageButton unlockButtonIcon;
        public TextView unlockButtonText;

        public BicycleViewHolder(View itemView) {
            super(itemView);
            bicycleNumber = itemView.findViewById(R.id.selected_bicycle_numbers);
            bicycleStatus = itemView.findViewById(R.id.selected_bicycle_status);
            unlockButton = itemView.findViewById(R.id.unlock_button);
            unlockButtonIcon = itemView.findViewById(R.id.unlock_button_icon);
            unlockButtonText = itemView.findViewById(R.id.unlock_button_text);
        }
    }

    public BicycleNumbersListAdapter(List<BicycleNumber> bicycleList, DatabaseReference bicycleRef, DatabaseReference rentalRef, String userId, String bicycleId, String bicycleRate, String bicyclePerMin, int bicycleImageID) {
        this.bicycleList = bicycleList;
        this.bicycleRef = bicycleRef;
        this.rentalRef = rentalRef;
        this.userId = userId;
        this.bicycleId = bicycleId;
        this.bicycleRate = bicycleRate;
        this.bicyclePerMin = bicyclePerMin;
        this.bicycleImageID = bicycleImageID;
    }

    @NonNull
    @Override
    public BicycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bicycle_number_item, parent, false);
        return new BicycleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BicycleViewHolder holder, int position) {
        BicycleNumber currentItem = bicycleList.get(position);
        holder.bicycleNumber.setText(currentItem.getNumber());
        holder.bicycleStatus.setText(currentItem.getStatus());

        // Customize the text & button color based on the Status Value
        int statusColor;
        Context context = holder.itemView.getContext();
        switch (currentItem.getStatus()) {
            case "Available":
                statusColor = ContextCompat.getColor(context, R.color.green);
                holder.unlockButton.setEnabled(true);
                holder.unlockButtonIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.active2));
                holder.unlockButtonText.setTextColor(ContextCompat.getColor(context, R.color.active2));
                break;
            case "Reserved":
                statusColor = ContextCompat.getColor(context, R.color.orange);
                holder.unlockButton.setEnabled(false);
                holder.unlockButtonIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray));
                holder.unlockButtonText.setTextColor(ContextCompat.getColor(context, R.color.gray));
                break;
            case "In Use":
                statusColor = ContextCompat.getColor(context, R.color.gray);
                holder.unlockButton.setEnabled(false);
                holder.unlockButtonIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray));
                holder.unlockButtonText.setTextColor(ContextCompat.getColor(context, R.color.gray));
                break;
            case "Damaged":
                statusColor = ContextCompat.getColor(context, R.color.dark_red);
                holder.unlockButton.setEnabled(false);
                holder.unlockButtonIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray));
                holder.unlockButtonText.setTextColor(ContextCompat.getColor(context, R.color.gray));
                break;
            default:
                statusColor = ContextCompat.getColor(context, R.color.black);
                holder.unlockButton.setEnabled(false);
                holder.unlockButtonIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray));
                holder.unlockButtonText.setTextColor(ContextCompat.getColor(context, R.color.gray));
                break;
        }
        holder.bicycleStatus.setTextColor(statusColor);


        //dialog of Reconfirm Bicycle Number
        Dialog ReconfirmDialog = new Dialog(context);
        ReconfirmDialog.setContentView(R.layout.reconfirm_bicycle_number);
        ReconfirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView ReconfirmBicycleNumber = ReconfirmDialog.findViewById(R.id.reconfirm_bicycle_number);
        MaterialButton startRideBtn = ReconfirmDialog.findViewById(R.id.start_ride_btn);
        ProgressBar progressBar = ReconfirmDialog.findViewById(R.id.progress_bar);
        MaterialButton cancelRideBtn = ReconfirmDialog.findViewById(R.id.cancel_ride_btn);

        //unlock button click listener
        holder.unlockButton.setOnClickListener(v -> {

            ReconfirmDialog.show(); //show dialog
            ReconfirmBicycleNumber.setText(currentItem.getNumber()); //set reconfirm bicycle number

            startRideBtn.setOnClickListener(v1 -> {
                //add checks for internet, nearby devices, bluetooth, Security Deposit & Minimum Wallet Balance.
                if (checkPermissionsAndSettings(context)) {

                    // Show the progress bar and disable the button
                    progressBar.setVisibility(View.VISIBLE);
                    startRideBtn.setText("");
                    startRideBtn.setEnabled(false);

                    // Pass here Bicycle Id, Number, Rate and create new Rental_id for Current User's OngoingRide into Firebase rentalData
                    createNewRental(context, currentItem, progressBar, startRideBtn, ReconfirmDialog);

                } else {
                    Toast.makeText(context, "All Permissions are required", Toast.LENGTH_SHORT).show();
                }
            });

            cancelRideBtn.setOnClickListener(v1 -> {
                ReconfirmDialog.dismiss();
            });
        });
    }

    @Override
    public int getItemCount() {
        return bicycleList.size();
    }


    private void createNewRental(Context context, BicycleNumber currentItem, ProgressBar progressBar, MaterialButton startRideBtn, Dialog ReconfirmDialog) {

        // Generate a 6-digit numeric rental ID
        Random random = new Random();
        int rentalIdInt = 1000000 + random.nextInt(9999999); // Generate a random number between 1000000 and 9999999

        TimeStampApi.getCurrentTimestamp(new TimeStampApi.TimeStampCallback() {
            @Override
            public void onSuccess(String timestamp) {
                // Create rental data map with fetched timestamp
                Map<String, Object> rentalData = new HashMap<>();
                rentalData.put("bicycleId", bicycleId);
                rentalData.put("bicycleImageId", bicycleImageID);
                rentalData.put("bicycleNumber", currentItem.getNumber());
                rentalData.put("bicycleRate", Map.of("mins", bicyclePerMin, "rate", bicycleRate));
                rentalData.put("rentalId", rentalIdInt);
                rentalData.put("rideAmount", "");
                rentalData.put("rideDuration", "");
                rentalData.put("rideEndTime", "");
                rentalData.put("rideStartTime", timestamp);

                Map<String, Object> updates = new HashMap<>();
                updates.put("anyOngoingRide?", true);
                updates.put("ongoingRide", rentalData);
                updates.put("pastRides", "");
                updates.put("totalRides", 0);

                rentalRef.child(userId).updateChildren(updates).addOnCompleteListener(task -> {

                    // Reset progress bar visibility, button state, text and dismiss dialog
                    progressBar.setVisibility(View.GONE);
                    startRideBtn.setText("Unlock & Start Ride");
                    startRideBtn.setEnabled(true);
                    ReconfirmDialog.dismiss();

                    if (task.isSuccessful()) {
                        // Update the bicycle status in bicycleData
                        bicycleRef.child(currentItem.getNumber()).child("Status").setValue("In Use");

                        Log.d("createNewRental", "Rental data updated successfully");
                        Toast.makeText(context, "Rental Data updated successfully", Toast.LENGTH_SHORT).show();

                        // Show unlock animation
                        showUnlockAnimation(context, currentItem);
                    } else {
                        Log.e("createNewRental", "Failed to update rental data", task.getException());
                        Toast.makeText(context, "Failed to update rental data", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onFailure(String errorMsg) {
                // Reset progress bar visibility, button state, text and dismiss dialog
                progressBar.setVisibility(View.GONE);
                startRideBtn.setText("Unlock & Start Ride");
                startRideBtn.setEnabled(true);
                ReconfirmDialog.dismiss();

                // Handle failure to fetch timestamp if needed
                Log.e("createNewRental", "Failed to fetch timestamp: " + errorMsg);
                Toast.makeText(context, "Failed to fetch timestamp", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean checkPermissionsAndSettings(Context context) {   // Bluetooth, Nearby Devices & Location Settings check
        Activity activity = (Activity) context;

        // Check for Location Permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return false;
        }

        // Check if Location is Enabled
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
            return false;
        }

        // Check for Bluetooth Permission (for Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 1002);
                return false;
            }
        }

        // Check if Bluetooth is Enabled
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        return true;
    }

    private void showUnlockAnimation(Context context, BicycleNumber currentItem) {
        FragmentActivity activity = (FragmentActivity) context;
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment unlockAnimationFragment = new UnlockAnimationFragment();

        fragmentTransaction.replace(R.id.frameLayout, unlockAnimationFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
