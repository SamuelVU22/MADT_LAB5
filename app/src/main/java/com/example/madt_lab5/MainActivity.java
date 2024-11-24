package com.example.madt_lab5;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ArrayAdapter<String> adapter; // Adapter to connect the data to the ListView
    private List<String> currencyRates; // List to hold currency rates
    private List<String> originalCurrencyRates; // List to hold original currency rates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for this activity

        // Initialize UI components
        // ListView to display currency rates
        ListView currencyListView = findViewById(R.id.currencyListView);
        // EditText for user input to filter currencies
        EditText currencyFilterEditText = findViewById(R.id.currencyFilterEditText);

        // Initialize the list that will hold currency rates and set up the adapter
        currencyRates = new ArrayList<>();
        originalCurrencyRates = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currencyRates);
        currencyListView.setAdapter(adapter);

        // Schedule the WorkManager task to fetch currency data
        scheduleCurrencyFetch();

        // Set a listener for filtering currencies based on user input
        currencyFilterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filterCurrencyList(s.toString()); // Call filter method when text changes
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void scheduleCurrencyFetch() {
        // Create a OneTimeWorkRequest for DataLoader to fetch data in the background
        OneTimeWorkRequest fetchCurrencyRequest = new OneTimeWorkRequest.Builder(DataLoader.class)
                .build();

        // Enqueue the work request with WorkManager
        WorkManager.getInstance(this).enqueue(fetchCurrencyRequest);

        // Observe the work status and update UI when work is finished
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(fetchCurrencyRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Fetch the XML stream and parse it
                        new Thread(() -> {
                            try {
                                InputStream xmlStream = new URL(Constant.CURRENCY_API_URL).openStream();
                                Map<String, String> ratesMap = Parser.getCurrencyRatesBaseUsd(xmlStream);

                                // Convert Map to List<String> for display
                                List<String> rates = new ArrayList<>();
                                for (Map.Entry<String, String> entry : ratesMap.entrySet()) {
                                    rates.add(String.format(" %s - %s", entry.getKey(), entry.getValue()));
                                }

                                // Store both in currencyRates and originalCurrencyRates
                                runOnUiThread(() -> {
                                    originalCurrencyRates.clear(); // Clear previous original rates
                                    originalCurrencyRates.addAll(rates); // Store original rates
                                    updateCurrencyList(rates); // Update displayed list
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                });
    }


    public void updateCurrencyList(List<String> rates) {
        currencyRates.clear(); // Clear existing rates
        currencyRates.addAll(rates); // Add new rates to the list
        adapter.notifyDataSetChanged(); // Notify adapter to refresh the ListView
    }

    private void filterCurrencyList(String query) {
        if (query.isEmpty()) {
            // If the query is empty, display all the data
            adapter.clear();
            adapter.addAll(originalCurrencyRates); // Restore the original list
        } else {
            // Create a new list to store filtered results
            List<String> filteredRates = new ArrayList<>();

            // Iterate through the currencyRates list
            for (String rate : originalCurrencyRates) {
                // Check if the current rate contains the query string (case-insensitive)
                if (rate.toLowerCase().contains(query.toLowerCase())) {
                    filteredRates.add(rate);
                }
            }

            // Clear the adapter and update it with the filtered list
            adapter.clear();
            adapter.addAll(filteredRates);
        }

        // Notify adapter to refresh the ListView
        adapter.notifyDataSetChanged();
    }

}