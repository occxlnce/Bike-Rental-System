package com.ayushxp.pedalcityapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.material.card.MaterialCardView;

public class WalletFragment extends Fragment implements OnBackPressedListener {

    TextView balance;
    Button add_money;
    Button withdraw_money;
    TextView paid_check_text;
    ImageView paid_check_icon;
    Button pay_now;
    Button refund;
    Button transactions_btn;
    private View view;
    MaterialCardView sd_card;
    Dialog dialog, withdraw_dialog;
    EditText amount;
    Button add_amount;
    EditText withdraw_amount;
    String amountString;
    String cleanedBalanceString;
    int balance_int;
    MaterialCheckBox withdraw_checkbox;
    Button withdraw_amount_btn;


    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wallet, container, false);

        // Initialize UI components
        balance = view.findViewById(R.id.balance);
        add_money = view.findViewById(R.id.add_money_btn);
        withdraw_money = view.findViewById(R.id.withdraw_money_btn);
        paid_check_text = view.findViewById(R.id.paid_check_text);
        paid_check_icon = view.findViewById(R.id.paid_check_icon);
        pay_now = view.findViewById(R.id.pay_now_btn);
        refund = view.findViewById(R.id.refund_btn);
        transactions_btn = view.findViewById(R.id.transactions_btn);
        sd_card = view.findViewById(R.id.sd_card_view);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            mUserRef = mDatabase.getReference("userWallet").child(userId);
        }

        //dialog of Add money popup
        dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.add_money_popup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        amount = dialog.findViewById(R.id.amount_edit);
        add_amount = dialog.findViewById(R.id.add_amount_btn);

        add_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if security deposit is paid
                if (pay_now.getVisibility() == View.GONE){
                    // Show the dialog for adding money
                    dialog.show();
                    amount.setText("");

                    add_amount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String amountString = amount.getText().toString();
                            if (!amountString.isEmpty()){
                                int amount = Integer.parseInt(amountString);

                                // Add the amount to the wallet balance
                                updateBalance(amount);
                                dialog.dismiss();

                            } else {
                                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Pay Security deposit first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Withdraw money button VISIBILITY check
        if (balance != null) {
            withdraw_money.setVisibility(View.VISIBLE);
        } else {
            withdraw_money.setVisibility(View.GONE);
        }

        //dialog of Withdraw money popup
        withdraw_dialog = new Dialog(requireContext());
        withdraw_dialog.setContentView(R.layout.withdraw_money_popup);
        withdraw_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        withdraw_amount = withdraw_dialog.findViewById(R.id.withdraw_amount_edit);
        withdraw_checkbox = withdraw_dialog.findViewById(R.id.withdraw_checkbox);
        withdraw_amount_btn = withdraw_dialog.findViewById(R.id.withdraw_amount_btn);

        withdraw_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                withdraw_dialog.show();
                withdraw_amount.setText("");
                withdraw_checkbox.setChecked(false);

                String balanceString = balance.getText().toString(); // Original string with currency symbol
                cleanedBalanceString = balanceString.replaceAll("[^\\d]", ""); // This will keep only digits
                balance_int = Integer.parseInt(cleanedBalanceString); // Balance in Int type

                withdraw_amount.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //if Amount equals Balance then checkbox is true checked
                        amountString = withdraw_amount.getText().toString();
                        if (amountString.equals(cleanedBalanceString)){
                            withdraw_checkbox.setCheckedState(MaterialCheckBox.STATE_CHECKED);
                        } else{
                            withdraw_checkbox.setCheckedState(MaterialCheckBox.STATE_UNCHECKED);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                withdraw_checkbox.addOnCheckedStateChangedListener(new MaterialCheckBox.OnCheckedStateChangedListener() {
                    @Override
                    public void onCheckedStateChangedListener(@NonNull MaterialCheckBox checkBox, int state) {
                        if (state == 1){
                            withdraw_amount.setText(cleanedBalanceString);
                            withdraw_amount.setSelection(withdraw_amount.getText().length());
                        } else {
                            withdraw_amount.setText("");
                        }
                    }
                });

                withdraw_amount_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        amountString = withdraw_amount.getText().toString();
                        if (!amountString.isEmpty()){
                            int amount = Integer.parseInt(amountString);
                            if (amount <= balance_int){
                                updateBalance(-amount);
                                Toast.makeText(requireContext(), "₹" + amount + " Withdrawn Successfully", Toast.LENGTH_SHORT).show();
                                withdraw_dialog.dismiss();
                            } else {
                                Toast.makeText(requireContext(), "Amount should not be greater than balance", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        pay_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(requireContext(), "₹300 paid successfully", Toast.LENGTH_SHORT).show();
                paid_check_text.setText("Paid");
                paid_check_icon.setImageDrawable(getResources().getDrawable(R.drawable.tick));
                pay_now.setVisibility(View.GONE);
                refund.setVisibility(View.VISIBLE);

                // Update the security_deposit_paid flag in Firebase
                mUserRef.child("security_deposit_paid").setValue(true);

                // Log the transaction in the transactions node
                logTransaction("Pay Security Deposit", 300);
            }
        });

        refund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(requireContext(), "₹300 refunded to your account", Toast.LENGTH_SHORT).show();
                paid_check_text.setText("Not Paid");
                paid_check_icon.setImageDrawable(getResources().getDrawable(R.drawable.cancel));
                pay_now.setVisibility(View.VISIBLE);
                refund.setVisibility(View.GONE);

                // Update the security_deposit_paid flag in Firebase
                mUserRef.child("security_deposit_paid").setValue(false);

                // Log the transaction in the transactions node
                logTransaction("Security Deposit Refund", 300);
            }
        });

        transactions_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAllTransactions();
            }
        });

        // Fetch balance and transactions from Firebase and update UI
        fetchBalanceAndTransactions();

        return view;
    }

    private void showAllTransactions() {
        // Retrieve transactions from Firebase
        DatabaseReference transactionsRef = mUserRef.child("transactions");
        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                LinearLayout transactionsBg = view.findViewById(R.id.transactions_bg);
                transactionsBg.setVisibility(View.VISIBLE);

                transactionsBg.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true; // Consume touch events to prevent them from passing through
                    }
                });

                LinearLayout transactionsContainer = view.findViewById(R.id.transactions_container);
                transactionsContainer.removeAllViews(); // Clear previous transactions

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Retrieve transaction data
                    Transaction transaction = snapshot.getValue(Transaction.class);

                    // Inflate transaction item layout
                    View transactionItemView = getLayoutInflater().inflate(R.layout.transaction_item, null);

                    // Populate transaction item views
                    TextView transactionType = transactionItemView.findViewById(R.id.transaction_type);
                    TextView transactionAmount = transactionItemView.findViewById(R.id.transaction_amount);
                    TextView transactionTimestamp = transactionItemView.findViewById(R.id.transaction_timestamp);

                    transactionType.setText(transaction.getType()); //Set Type
                    transactionTimestamp.setText(transaction.getTimestamp()); //Set TimeStamp
                    // Format the amount string
                    int amount = transaction.getAmount();
                    if (amount < 0) {
                        String formattedAmount = "-" + "₹" + Math.abs(amount);
                        transactionAmount.setText(formattedAmount); //Set negative Amount
                    } else {
                        transactionAmount.setText("₹" + amount); //Set positive Amount
                    }

                    // Add margin to transaction item view
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    layoutParams.setMargins(0, 0, 0, 20); // Add bottom margin of 20px
                    transactionItemView.setLayoutParams(layoutParams);

                    // Add transaction item to container
                    transactionsContainer.addView(transactionItemView, 0); // Add to top

                }

                // Set onClickListener to back button
                ImageButton backButton = view.findViewById(R.id.back_btn);
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Hide the transactions view
                        transactionsBg.setVisibility(View.GONE);
                        transactionsContainer.removeAllViewsInLayout();
                        transactionsContainer.removeAllViews();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

    }

    @Override
    public void onBackPressed() {
        LinearLayout transactionsBg = view.findViewById(R.id.transactions_bg);
        LinearLayout transactionsContainer = view.findViewById(R.id.transactions_container);

        if (transactionsBg.getVisibility() == View.VISIBLE) {
            // Hide the transactions view
            transactionsBg.setVisibility(View.GONE);
            transactionsContainer.removeAllViewsInLayout();
            transactionsContainer.removeAllViews();
        } else {
            // If the transactions view is not visible, handle default behavior
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }


    private void updateBalance(int amount) {
        // Update balance in Firebase
        mUserRef.child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int currentBalance = dataSnapshot.getValue(Integer.class);
                    int newBalance = currentBalance + amount;
                    balance.setText("₹" + newBalance);
                    mUserRef.child("balance").setValue(newBalance);
                } else {
                    // If balance doesn't exist yet, set it to the added amount
                    balance.setText("₹" + amount);
                    mUserRef.child("balance").setValue(amount);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        // Log transaction in Firebase
        logTransaction((amount >= 0) ? "Add Money" : "Withdraw Money", amount);
    }

    private void logTransaction(String transactionType, int amount) {
        Transaction transaction = new Transaction(transactionType, amount);
        transaction.getTimestampTask().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mUserRef.child("transactions").push().setValue(transaction);
                } else {
                    Log.e("WalletFragment", "Failed to get timestamp for transaction", task.getException());
                }
            }
        });
    }

    private void fetchBalanceAndTransactions() {
        if (userId != null) {
            mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Fetch balance, transactions, and security deposit status from Firebase
                        int balance = dataSnapshot.child("balance").getValue(Integer.class);
                        boolean isSecurityDepositPaid = dataSnapshot.child("security_deposit_paid").getValue(Boolean.class);
                        // Update UI with balance, transactions, and security deposit status
                        updateUI(balance, isSecurityDepositPaid);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }
    }

    private void updateUI(int balance, boolean isSecurityDepositPaid) {
        // Update UI with balance, transactions, and security deposit status
        this.balance.setText("₹" + balance);
        if (isSecurityDepositPaid) {
            paid_check_text.setText("Paid");
            paid_check_icon.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            pay_now.setVisibility(View.GONE);
            refund.setVisibility(View.VISIBLE);
        } else {
            paid_check_text.setText("Not Paid");
            paid_check_icon.setImageDrawable(getResources().getDrawable(R.drawable.cancel));
            pay_now.setVisibility(View.VISIBLE);
            refund.setVisibility(View.GONE);
        }
    }

}