package com.ayushxp.pedalcityapp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class UserDetailsActivity extends AppCompatActivity {

    private static final String TAG = "UserDetailsActivity";

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private DatabaseReference userReference;
    private EditText nameET, phoneNumberET, mDisplayDate;
    private Button submitBtn;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    DataSnapshot dataSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        nameET = findViewById(R.id.nameET);
        phoneNumberET = findViewById(R.id.phoneNumberET);
        mDisplayDate = findViewById(R.id.dateTV);
        submitBtn = findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameET.getText().toString();
                String number = phoneNumberET.getText().toString();
                String date = mDisplayDate.getText().toString();

                if (user != null) {
                    if (!name.isEmpty() && !number.isEmpty() && !date.isEmpty()) {
                        // No errors, proceed with the intent
                        nameET.setError(null);
                        phoneNumberET.setError(null);
                        mDisplayDate.setError(null);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            Date selectedDate = sdf.parse(date);
                            Date currentDate = new Date();
                            int age = calculateAge(selectedDate, currentDate);

                            if (number.length() == 10) {
                                if (age >= 16) {
                                    firebaseDatabase = FirebaseDatabase.getInstance();
                                    reference = firebaseDatabase.getReference("userdata");

                                    UserData userData = new UserData(name, number, date);
                                    reference.child(user.getUid()).setValue(userData);

                                    userReference = reference.child(user.getUid());

                                    Toast.makeText(getApplicationContext(), "User Details Submitted", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    intent.putExtra("name", name);
                                    intent.putExtra("number", number);
                                    intent.putExtra("date", date);

                                    startActivity(intent);
                                    finish();
                                } else {
                                    mDisplayDate.setError("Age must be 16+");
                                    Toast.makeText(getApplicationContext(), "Enter correct Date.\nAge must be 16+", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                phoneNumberET.setError("Phone number should be exactly 10 digits");
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    } else {
                        handleEmptyFields(name, number, date);
                    }
                } else {
                    // Handle the case where the user is not signed in
                }
            }
        });

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(UserDetailsActivity.this,
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
                String date = day + "/" + month + "/" + year;
                mDisplayDate.setText(date);
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

    private void handleEmptyFields(String name, String number, String date) {
        if (name.isEmpty()) {
            nameET.setError("Please Enter Your Name");
        } else {
            nameET.setError(null);
        }

        if (number.isEmpty()) {
            phoneNumberET.setError("Please Enter your Number");
        } else {
            phoneNumberET.setError(null);
        }

        if (date.isEmpty()) {
            mDisplayDate.setError("Please Enter Date");
        } else {
            mDisplayDate.setError(null);
        }
    }
}
