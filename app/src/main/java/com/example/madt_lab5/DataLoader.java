package com.example.madt_lab5;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.example.madt_lab5.Constant.CURRENCY_API_URL;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataLoader extends Worker {

    public DataLoader(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params); // Call the superclass constructor with context and parameters
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            InputStream xmlResponse = fetchCurrencyData(); // Fetch XML data from API
            // Parse the XML response (you need to update Parser to handle this)
            Parser.getCurrencyRatesBaseUsd(xmlResponse); // Assuming Parser is handling string XML directly

            // Return success if everything works
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(); // Return failure if an exception occurs
        }
    }



    public InputStream fetchCurrencyData() throws IOException {
        URL url = new URL(CURRENCY_API_URL); // Define API endpoint URL
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000); // Set read timeout
            urlConnection.setConnectTimeout(15000); // Set connection timeout
            urlConnection.setRequestMethod("GET"); // Use GET method
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = urlConnection.getInputStream()) {
                    return inputStream; // Read and return data from the input stream
                }
            } else {
                throw new IOException("HTTP error code: " + responseCode);
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect(); // Ensure connection is closed
            }
        }
    }
}