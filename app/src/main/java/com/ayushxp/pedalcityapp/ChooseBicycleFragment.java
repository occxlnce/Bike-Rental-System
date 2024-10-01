package com.ayushxp.pedalcityapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wajahatkarim3.easyflipview.EasyFlipView;

public class ChooseBicycleFragment extends Fragment {

    private View view;
    private boolean isLongPressed = false;
    private boolean isFlipped = true;
    private EasyFlipView[] flipViews;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mWalletRef;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_choose_bicycle, container, false);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            mWalletRef = mDatabase.getReference("userWallet").child(userId);
        }


        // Set onClickListener to back button
        ImageButton backButton = view.findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Initialize Easy Flip Views
        flipViews = new EasyFlipView[]{
                //sponsored bicycles
                view.findViewById(R.id.firefox),
                view.findViewById(R.id.hero_lectro),
                view.findViewById(R.id.ninety_one),
                view.findViewById(R.id.emotorad),
                view.findViewById(R.id.cradiac),
                view.findViewById(R.id.triban),
                //pedalcity bicycles
                view.findViewById(R.id.pedalcity_eco),
                view.findViewById(R.id.pedalcity_ranger),
                view.findViewById(R.id.pedalcity_cargo)
        };


        // Set up buttons for each flip view
        for (EasyFlipView flipView : flipViews) {
            setupCardButtons(flipView);
        }


//        setupCardInfoButton(view.findViewById(R.id.hero_lectro));
//        setupCardInfoButton(view.findViewById(R.id.ninety_one));
//        setupCardInfoButton(view.findViewById(R.id.emotorad));
//        setupCardInfoButton(view.findViewById(R.id.cradiac));
//        setupCardInfoButton(view.findViewById(R.id.triban));
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCardButtons(EasyFlipView cardFlipView) {

        // Find the ImageButton inside the Easy flip views
        ImageButton infoButton = cardFlipView.findViewById(R.id.info_button_id); // Replace with the actual ImageButton ID in your XML
        ImageButton flipButton = cardFlipView.findViewById(R.id.flip_btn);
        // Find the Material Button inside the Easy flip views
        MaterialButton bookRideButton = cardFlipView.findViewById(R.id.book_ride_btn);

        // Extract data from the card views
        ImageView bicycleLogoView = cardFlipView.findViewById(R.id.bicycle_logo_id);
        ImageView bicycleImageView = cardFlipView.findViewById(R.id.bicycle_image_id);
        TextView bicycleTypeView = cardFlipView.findViewById(R.id.bicycle_type);
        TextView bicycleNameView = cardFlipView.findViewById(R.id.bicycle_name_id);
        TextView priceTextView = cardFlipView.findViewById(R.id.price_id);
        TextView perMinTextView = cardFlipView.findViewById(R.id.per_min_id);

        // Retrieve and log tag values
        String logoTag = (String) bicycleLogoView.getTag();
        String imageTag = (String) bicycleImageView.getTag();
        // Log the tag values
        Log.d("Tag Values", "Logo Tag: " + logoTag + ", Image Tag: " + imageTag);

        // Get resource IDs from tags
        int bicycleLogoResId = getResources().getIdentifier(logoTag, "drawable", getActivity().getPackageName());
        int bicycleImageResId = getResources().getIdentifier(imageTag, "drawable", getActivity().getPackageName());
        // Log the resource IDs
        Log.d("Resource IDs", "Logo Res ID: " + bicycleLogoResId + ", Image Res ID: " + bicycleImageResId);

        String bicycle_id = getResources().getResourceEntryName(cardFlipView.getId()); // Get View IDs as a String
        Log.d("Bicycle_ID", "Button ID as string: " + bicycle_id);

        String bicycleType = bicycleTypeView.getText().toString();
        String priceText = priceTextView.getText().toString();
        String perMinText = perMinTextView.getText().toString();
        String bicycleName = null;

        if (bicycleNameView != null) {
            bicycleName = bicycleNameView.getText().toString();
        } else {
            // Handle the case where bicycle_name_id TextView is not present in this flip view
        }

        // Final variable declaration for bicycleName
        final String finalBicycleName = bicycleName;

        // Set up the book ride button to open the SelectedBicycleFragment
        bookRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check Security deposit paid
                if (mWalletRef != null) {
                    mWalletRef.child("security_deposit_paid").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Boolean securityDepositPaid = dataSnapshot.getValue(Boolean.class);
                            if (securityDepositPaid != null && securityDepositPaid) {
                                // Security deposit is paid, proceed to the next fragment

                                Bundle bundle = new Bundle();
                                bundle.putInt("bicycleLogoId", bicycleLogoResId);
                                bundle.putInt("bicycleImageId", bicycleImageResId);
                                bundle.putString("bicycleType", bicycleType);
                                bundle.putString("bicycleName", finalBicycleName);
                                bundle.putString("priceText", priceText);
                                bundle.putString("perMinText", perMinText);
                                bundle.putString("bicycle_id", bicycle_id);

                                SelectedBicycleFragment selectedBicycleFragment = new SelectedBicycleFragment();
                                selectedBicycleFragment.setArguments(bundle); //set bundle as arguments
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.frameLayout, selectedBicycleFragment)
                                        .addToBackStack(null)
                                        .commit();
                            } else {
                                // Security deposit is not paid, show a message or handle accordingly
                                Toast.makeText(getActivity(), "Please pay the Security Deposit first", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle possible errors
                            Toast.makeText(getActivity(), "Error checking security deposit status", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        if (isFlipped == false) {
            cardFlipView.setAutoFlipBack(false);
        }

        // Set an OnClickListener for info button
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlipped == false) {
                    cardFlipView.setAutoFlipBack(false);
                } else {
                    cardFlipView.setAutoFlipBack(true);
                }

                cardFlipView.flipTheView();
                isFlipped = true; // Update flip state

            /*    // Fetch the card view ID
                int cardViewId = cardView.getId();
                // Handle the info window based on the card view ID
                showInfoWindow(cardViewId); */
            }
        });

        // Set an OnClickListener for flip button
        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cardFlipView.flipTheView();
                isFlipped = false; // Update flip state
                cardFlipView.setAutoFlipBack(false); // Disable auto flip back immediately
            }
        });

        // Set an OnLongClickListener for info button
        infoButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                isLongPressed = true;
                cardFlipView.flipTheView();
                cardFlipView.setAutoFlipBack(false);
                isFlipped = true; // Update flip state

                return true;
            }
        });

        // Set OnTouchListener on info button to flip back on release
        infoButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        isLongPressed = false; // Reset the long press state on touch down
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isLongPressed) {
                            cardFlipView.flipTheView();
                            isLongPressed = false; // Reset the long press state after flipping back
                            isFlipped = false; // Update flip state
                        }
                        break;
                }
                return false; // Return false to allow other touch events like click and long click
            }
        });

    }

 /*   private void showInfoWindow(int cardViewId) {
        if (cardViewId == R.id.firefox_viper) {
            // Show info for Firefox Viper
            showFirefoxViperInfo();
        } else if (cardViewId == R.id.hero_lectro) {
            // Show info for Hero Lectro
            showHeroLectroInfo();
        } else if (cardViewId == R.id.ninety_one) {
            // Show info for Ninety-One Madrid
            showNinetyOneInfo();
        } else if (cardViewId == R.id.emotorad) {
            // Show info for Emotorad Emx+
            showEmotoradInfo();
        } else if (cardViewId == R.id.cradiac) {
            // Show info for Cradiac Xc900
            showCradiacInfo();
        } else if (cardViewId == R.id.triban) {
            // Show info for Triban Rc100
            showTribanInfo();
        }
        // Add more if-else blocks for other card views if needed
    } */

 /*   private void showFirefoxViperInfo() {
        // Implement the logic to show Firefox Viper info window
        Toast.makeText(requireContext(), "firefox viper", Toast.LENGTH_SHORT).show();
    }

    private void showHeroLectroInfo() {
        // Implement the logic to show Hero Lectro info window
        Toast.makeText(requireContext(), "hero lectro", Toast.LENGTH_SHORT).show();
    }

    private void showNinetyOneInfo() {
        // Implement the logic to show Ninety One info window
        Toast.makeText(requireContext(), "ninety one madrid", Toast.LENGTH_SHORT).show();
    }

    private void showEmotoradInfo() {
        // Implement the logic to show Emotorad info window
        Toast.makeText(requireContext(), "emotorad emx+", Toast.LENGTH_SHORT).show();
    }

    private void showCradiacInfo() {
        // Implement the logic to show Emotorad info window
        Toast.makeText(requireContext(), "cradiac xc900", Toast.LENGTH_SHORT).show();
    }

    private void showTribanInfo() {
        // Implement the logic to show Emotorad info window
        Toast.makeText(requireContext(), "triban rc100", Toast.LENGTH_SHORT).show();
    }*/

}