package com.ayushxp.pedalcityapp;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Transaction {
    private String type;
    private int amount;
    private String timestamp;
    private TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

    public Transaction() {
        // Default constructor required for Firebase
    }

    public Transaction(String type, int amount) {
        this.type = type;
        this.amount = amount;
        fetchCurrentTimestamp();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        taskCompletionSource.setResult(null);  // Indicate that the timestamp is ready
    }

    public Task<Void> getTimestampTask() {
        return taskCompletionSource.getTask();
    }

    private void fetchCurrentTimestamp() {
        TimeStampApi.getCurrentTimestamp(new TimeStampApi.TimeStampCallback() {
            @Override
            public void onSuccess(String timestamp) {
                setTimestamp(timestamp);
            }

            @Override
            public void onFailure(String errorMsg) {
                // Handle failure if needed
                taskCompletionSource.setException(new Exception(errorMsg));
            }
        });
    }
}
