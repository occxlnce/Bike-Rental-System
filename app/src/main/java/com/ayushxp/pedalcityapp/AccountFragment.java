package com.ayushxp.pedalcityapp;

import static android.content.Intent.getIntent;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class AccountFragment extends Fragment {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private DatabaseReference userReference;
    DataSnapshot snapshot;
    private Uri imagePath;
    ImageView profilePic;
    TextView nameView;
    TextView mailView;
    TextView numberView;
    TextView dateView;
    Button edit_profile;
    EditText name, email, number, date;
    Button save;
    Dialog dialog;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private static final String TAG = "AccountFragment";
    Button signout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("userdata");
        userReference = reference.child(user.getUid());

        profilePic = view.findViewById(R.id.profile_pic);
        nameView = view.findViewById(R.id.profile_name);
        mailView = view.findViewById(R.id.profile_email);
        numberView = view.findViewById(R.id.profile_number);
        dateView = view.findViewById(R.id.profile_birth_date);


        if (user != null) {

            if (profilePic == null){
                // User has not uploaded a profile picture, set default background
                profilePic.setImageResource(R.drawable.noprofile);
                profilePic.setBackgroundResource(R.drawable.noprofile);
            } else {
                retrieveProfilePicUrl();
            }

            String userEmail = user.getEmail();
            mailView.setText(userEmail);

            nameView.setText(this.getArguments().getString("name"));
            numberView.setText(this.getArguments().getString("number"));
            dateView.setText(this.getArguments().getString("date"));

        } else {
            // Handle the case where the user is not signed in
        }

        // Attach a ValueEventListener to the user's node in the database to fetch the user data
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the dataSnapshot exists
                if (dataSnapshot.exists()) {
                    // Retrieve user data from dataSnapshot
                    UserData userData = dataSnapshot.getValue(UserData.class);
                    if (userData != null) {
                        // Set the retrieved data to the respective views
                        nameView.setText(userData.getName());
                        numberView.setText(userData.getPhoneNumber());
                        dateView.setText(userData.getBirthDate());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        //dialog Edit profile pop up
        dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.edit_profile_popup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        name = dialog.findViewById(R.id.name_edit);
        email = dialog.findViewById(R.id.email_edit);
        number = dialog.findViewById(R.id.phone_edit);
        date = dialog.findViewById(R.id.date_edit);
        save = dialog.findViewById(R.id.save_btn);

        edit_profile = view.findViewById(R.id.edit_profile_btn);
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                name.setText("");
                email.setText("");
                number.setText("");
                date.setText("");

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String nameText = name.getText().toString();
                        String emailText = email.getText().toString();
                        String numberText = number.getText().toString();
                        String dateText = date.getText().toString();

                        if (!nameText.isEmpty() && !emailText.isEmpty() && !numberText.isEmpty() && !dateText.isEmpty()){

                            name.setError(null);
                            email.setError(null);
                            number.setError(null);
                            date.setError(null);

                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date selectedDate = sdf.parse(dateText);
                                Date currentDate = new Date();
                                int age = calculateAge(selectedDate, currentDate);

                                if (numberText.length() == 10) {
                                    if (age >= 16) {
                                        firebaseDatabase = FirebaseDatabase.getInstance();
                                        reference = firebaseDatabase.getReference("userdata");

                                        // Get the existing profile picture URL from Firebase
                                        DatabaseReference profilePicRef = reference.child(user.getUid()).child("ProfilePic");
                                        profilePicRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String profilePicUrl = dataSnapshot.getValue(String.class);

                                                // Create a HashMap to update the user's data
                                                HashMap<String, Object> userData = new HashMap<>();
                                                userData.put("Name", nameText);
                                                userData.put("PhoneNumber", numberText);
                                                userData.put("BirthDate", dateText);
                                                userData.put("ProfilePic", profilePicUrl); // Retain the existing profile picture URL

                                                // Update the existing user node with the updated data
                                                reference.child(user.getUid()).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // Update the views with new user details
                                                            nameView.setText(nameText);
                                                            numberView.setText(numberText);
                                                            dateView.setText(dateText);
                                                            Toast.makeText(requireContext(), "User Details Saved", Toast.LENGTH_SHORT).show();
                                                            dialog.dismiss();
                                                        } else {
                                                            Toast.makeText(requireContext(), "Failed to save user details", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                // Handle database error if needed
                                            }
                                        });
                                    } else {
                                        date.setError("Age must be 16+");
                                        Toast.makeText(requireContext(), "Enter correct Date.\nAge must be 16+", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    number.setError("Phone number should be exactly 10 digits");
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        } else {
                            handleEmptyFields(nameText, numberText, dateText);
                        }
                    }
                });

                date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                                com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Picker_Date_Spinner,
                                mDateSetListener,
                                year, month, day);

                        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLUE));
                        dialog.show();
                    }
                });

                mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month + 1;
                        Log.d(TAG, "onDateSet: dd/mm/yyyy: " + day + "/" + month + "/" + year);
                        String dates = day + "/" + month + "/" + year;
                        date.setText(dates);
                    }
                };
            }
            private int calculateAge(Date birthDate, Date currentDate) {
                Calendar birthCalendar = Calendar.getInstance();
                birthCalendar.setTime(birthDate);

                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(currentDate);

                int age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);

                if (currentCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }

                return age;
            }

            private void handleEmptyFields(String nameText, String numberText, String dateText) {
                if (nameText.isEmpty()) {
                    name.setError("Please Enter Your Name");
                } else {
                    name.setError(null);
                }

                if (numberText.isEmpty()) {
                    number.setError("Please Enter your Number");
                } else {
                    number.setError(null);
                }

                if (dateText.isEmpty()) {
                    date.setError("Please Enter Date");
                } else {
                    date.setError(null);
                }
            }

        });


        signout = view.findViewById(R.id.sign_out_btn);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOutAndStartGoogleSignin();
            }
        });

        return view;
    }

    private void retrieveProfilePicUrl() {
        // Get a reference to the profile picture URL in the database
        DatabaseReference profilePicRef = reference.child(user.getUid()).child("ProfilePic");

        // Retrieve the URL from the database
        profilePicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Profile picture URL exists in the database
                    String profilePicUrl = dataSnapshot.getValue(String.class);
                    // Load and display the profile picture
                    loadImageFromUrl(profilePicUrl);
                } else {
                    // Profile picture URL does not exist, set default background
                    profilePic.setImageResource(R.drawable.noprofile);
                    profilePic.setBackgroundResource(R.drawable.noprofile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }

    private void loadImageFromUrl(String profilePicUrl) {
        profilePic.setImageBitmap(null);
        profilePic.setImageURI(null);
        profilePic.setImageDrawable(null);

        // Use Picasso/Glide to load the image from URL into the ImageView
        Picasso.get().load(profilePicUrl).resize(500,500).centerCrop().networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.noprofile).into(profilePic, new Callback() {
            @Override
            public void onSuccess() {
                // Image loaded successfully
            }

            @Override
            public void onError(Exception e) {
                // Failed to load image from cache, try again from network
                Picasso.get().load(profilePicUrl).resize(500,500).centerCrop().into(profilePic);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK && data != null){
            imagePath = data.getData();
            getImageinImageView();
        }
    }


    private void getImageinImageView() {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(),imagePath);
        } catch (IOException e){
            e.printStackTrace();
        }

        // Compress the bitmap to reduce file size
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Adjust the compression quality as needed
        byte[] imageData = baos.toByteArray();

        // Upload the compressed image data to Firebase Storage
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        FirebaseStorage.getInstance().getReference("images/"+ UUID.randomUUID().toString()).putBytes(imageData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                updateProfilePic(task.getResult().toString());
                                // Load the new image using Picasso
                                Picasso.get().load(task.getResult().toString()).resize(500,500).centerCrop().placeholder(R.drawable.noprofile).into(profilePic);
                            }
                        }
                    });
                    Toast.makeText(getContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = 100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount();
                progressDialog.setMessage("Uploaded "+(int)progress+"%");
            }
        });
    }

    private void updateProfilePic(String url){
        reference.child(user.getUid()+"/ProfilePic").setValue(url);
    }

    private void signOutAndStartGoogleSignin() {
        mAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            // Optional: Update UI or show a message to the user
            Intent intent = new Intent(requireActivity(), GoogleSignin.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}