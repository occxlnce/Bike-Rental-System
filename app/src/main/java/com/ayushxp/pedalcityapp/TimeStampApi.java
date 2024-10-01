package com.ayushxp.pedalcityapp;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class TimeStampApi {
    private static final String TIME_API_URL = "https://www.timeapi.io/api/Time/current/coordinate?latitude=19.0634&longitude=72.8677";

    public static void getCurrentTimestamp(final TimeStampCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(TIME_API_URL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String time = response.getString("time");
                    String date = response.getString("date");
                    String formattedTimestamp = formatTimestamp(time, date);
                    callback.onSuccess(formattedTimestamp);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onFailure("Failed to parse JSON");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("TimeStampApi", "Failed to fetch timestamp", throwable);
                callback.onFailure("Failed to fetch timestamp");
            }
        });
    }

    private static String formatTimestamp(String time, String date) {

        // Assuming time is in "HH:mm" format and date is in "MM/dd/yyyy" format
        String formattedTime = formatTime12Hr(time); // Format time to "hh:mm a" format
        String[] dateParts = date.split("/");
        String formattedDate = dateParts[1] + "/" + dateParts[0] + "/" + dateParts[2]; // Format date to "dd/MM/yyyy" format

        return formattedTime + " " + formattedDate;
    }

    private static String formatTime12Hr(String time24Hr) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date dateObj = sdf24.parse(time24Hr);

            return sdf12.format(dateObj);
        } catch (ParseException e) {
            e.printStackTrace();
            // Return original time if parsing fails
            return time24Hr;
        }
    }

    public interface TimeStampCallback {
        void onSuccess(String timestamp);

        void onFailure(String errorMsg);
    }
}
