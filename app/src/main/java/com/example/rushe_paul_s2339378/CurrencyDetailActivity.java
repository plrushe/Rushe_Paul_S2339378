package com.example.rushe_paul_s2339378;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CurrencyDetailActivity extends AppCompatActivity {

    private double rate = 1.0;
    private String currencyCode = "";
    private final DecimalFormat formatter = new DecimalFormat("#,##0.00");
    private final Map<String, LocationInfo> currencyLocations = buildLocations();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_detail);

        TextView titleView = findViewById(R.id.detailTitle);
        TextView flagView = findViewById(R.id.detailFlag);
        TextView descriptionView = findViewById(R.id.detailDescription);
        TextView pubDateView = findViewById(R.id.detailPubDate);
        TextView rateView = findViewById(R.id.detailRate);
        TextView conversionResult = findViewById(R.id.conversionResult);
        TextView conversionLabel = findViewById(R.id.conversionLabel);
        TextView locationLabel = findViewById(R.id.locationLabel);
        MaterialButton openMapButton = findViewById(R.id.openMapButton);
        EditText amountInput = findViewById(R.id.amountInput);
        RadioGroup directionGroup = findViewById(R.id.directionGroup);
        RadioButton directionToCurrency = findViewById(R.id.directionToCurrency);
        RadioButton directionToGbp = findViewById(R.id.directionToGbp);

        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String pubDate = getIntent().getStringExtra("pubDate");
        currencyCode = getIntent().getStringExtra("currencyCode");

        // just fill the views with whatever we were passed
        titleView.setText(title);
        descriptionView.setText(description);
        if (pubDate != null && !pubDate.trim().isEmpty()) {
            pubDateView.setText("Last updated: " + pubDate.trim());
        } else {
            pubDateView.setText("Last updated: Not provided");
        }

        rate = parseRate(description);
        if (currencyCode == null) {
            currencyCode = "";
        }

        flagView.setText(flagEmojiForCurrency(currencyCode));

        LocationInfo locationInfo = currencyLocations.get(currencyCode.toUpperCase());
        if (locationInfo != null) {
            String locationText = locationInfo.capital + ", " + locationInfo.country +
                    " (" + locationInfo.latitude + ", " + locationInfo.longitude + ")";
            locationLabel.setText(locationText);
            openMapButton.setOnClickListener(v -> openInMaps(locationInfo));
        } else {
            locationLabel.setText("Location unavailable for this currency code.");
            openMapButton.setEnabled(false);
        }

        directionToCurrency.setText("GBP → " + (currencyCode.isEmpty() ? "Currency" : currencyCode));
        directionToGbp.setText((currencyCode.isEmpty() ? "Currency" : currencyCode) + " → GBP");
        conversionLabel.setText("Convert between GBP and " + (currencyCode.isEmpty() ? "currency" : currencyCode));

        String rateText = "1 GBP = " + formatter.format(rate) + " " + currencyCode;
        rateView.setText(rateText);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // re-run the conversion whenever text changes
                updateConversion(amountInput, directionGroup, conversionResult);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        amountInput.addTextChangedListener(watcher);
        directionGroup.setOnCheckedChangeListener((group, checkedId) -> updateConversion(amountInput, directionGroup, conversionResult));

        amountInput.setText("1");
        updateConversion(amountInput, directionGroup, conversionResult);
    }

    private double parseRate(String raw) {
        // keep a sane default if anything odd comes through
        if (raw == null) {
            return 1.0;
        }
        try {
            double parsed = Double.parseDouble(raw.trim());
            if (parsed <= 0) {
                return 1.0;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            return 1.0;
        }
    }

    private void updateConversion(EditText amountInput, RadioGroup directionGroup, TextView conversionResult) {
        String amountText = amountInput.getText().toString().trim();
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            conversionResult.setText("Enter a valid amount");
            return;
        }

        int checkedId = directionGroup.getCheckedRadioButtonId();
        boolean toCurrency = checkedId == R.id.directionToCurrency;

        double converted;
        String fromLabel;
        String toLabel;
        if (toCurrency) {
            converted = amount * rate;
            fromLabel = "GBP";
            toLabel = currencyCode.isEmpty() ? "Target" : currencyCode;
        } else {
            converted = amount / rate;
            fromLabel = currencyCode.isEmpty() ? "Source" : currencyCode;
            toLabel = "GBP";
        }

        String resultText = formatter.format(amount) + " " + fromLabel + " = " + formatter.format(converted) + " " + toLabel;
        conversionResult.setText(resultText);
    }

    private void openInMaps(LocationInfo locationInfo) {
        String query = Uri.encode(locationInfo.capital + ", " + locationInfo.country);
        Uri geoUri = Uri.parse("geo:" + locationInfo.latitude + "," + locationInfo.longitude + "?q=" + query);
        Intent intent = new Intent(Intent.ACTION_VIEW, geoUri);
        intent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // fallback to any available map handler
            intent.setPackage(null);
            startActivity(intent);
        }
    }

    private String flagEmojiForCurrency(String code) {
        if (code == null || code.trim().length() < 2) {
            return "🏳️";
        }

        String normalized = code.trim().toUpperCase();
        String country;
        switch (normalized) {
            case "EUR":
                country = "EU";
                break;
            case "GBP":
                country = "GB";
                break;
            case "USD":
                country = "US";
                break;
            default:
                country = normalized.substring(0, 2);
                break;
        }

        StringBuilder builder = new StringBuilder();
        for (char ch : country.toCharArray()) {
            if (ch < 'A' || ch > 'Z') {
                return "🏳️";
            }
            int codePoint = 0x1F1E6 + (ch - 'A');
            builder.appendCodePoint(codePoint);
        }
        return builder.toString();
    }

    private Map<String, LocationInfo> buildLocations() {
        Map<String, LocationInfo> locations = new HashMap<>();
        locations.put("USD", new LocationInfo("United States", "Washington, D.C.", 38.9072, -77.0369));
        locations.put("EUR", new LocationInfo("Eurozone", "Brussels", 50.8503, 4.3517));
        locations.put("GBP", new LocationInfo("United Kingdom", "London", 51.5074, -0.1278));
        locations.put("JPY", new LocationInfo("Japan", "Tokyo", 35.6762, 139.6503));
        locations.put("AUD", new LocationInfo("Australia", "Canberra", -35.2809, 149.1300));
        locations.put("CAD", new LocationInfo("Canada", "Ottawa", 45.4215, -75.6972));
        locations.put("CHF", new LocationInfo("Switzerland", "Bern", 46.9480, 7.4474));
        locations.put("NZD", new LocationInfo("New Zealand", "Wellington", -41.2865, 174.7762));
        locations.put("CNY", new LocationInfo("China", "Beijing", 39.9042, 116.4074));
        locations.put("INR", new LocationInfo("India", "New Delhi", 28.6139, 77.2090));
        locations.put("SGD", new LocationInfo("Singapore", "Singapore", 1.3521, 103.8198));
        locations.put("HKD", new LocationInfo("Hong Kong", "Hong Kong", 22.3193, 114.1694));
        locations.put("ZAR", new LocationInfo("South Africa", "Pretoria", -25.7479, 28.2293));
        locations.put("SEK", new LocationInfo("Sweden", "Stockholm", 59.3293, 18.0686));
        locations.put("NOK", new LocationInfo("Norway", "Oslo", 59.9139, 10.7522));
        locations.put("DKK", new LocationInfo("Denmark", "Copenhagen", 55.6761, 12.5683));
        locations.put("BRL", new LocationInfo("Brazil", "Brasilia", -15.7939, -47.8828));
        locations.put("MXN", new LocationInfo("Mexico", "Mexico City", 19.4326, -99.1332));
        locations.put("ILS", new LocationInfo("Israel", "Jerusalem", 31.7683, 35.2137));
        locations.put("TRY", new LocationInfo("Türkiye", "Ankara", 39.9334, 32.8597));
        locations.put("PLN", new LocationInfo("Poland", "Warsaw", 52.2297, 21.0122));
        locations.put("HUF", new LocationInfo("Hungary", "Budapest", 47.4979, 19.0402));
        locations.put("CZK", new LocationInfo("Czechia", "Prague", 50.0755, 14.4378));
        locations.put("RON", new LocationInfo("Romania", "Bucharest", 44.4268, 26.1025));
        locations.put("AED", new LocationInfo("United Arab Emirates", "Abu Dhabi", 24.4539, 54.3773));
        locations.put("SAR", new LocationInfo("Saudi Arabia", "Riyadh", 24.7136, 46.6753));
        locations.put("THB", new LocationInfo("Thailand", "Bangkok", 13.7563, 100.5018));
        locations.put("KRW", new LocationInfo("South Korea", "Seoul", 37.5665, 126.9780));
        locations.put("PHP", new LocationInfo("Philippines", "Manila", 14.5995, 120.9842));
        locations.put("IDR", new LocationInfo("Indonesia", "Jakarta", -6.2088, 106.8456));
        locations.put("MYR", new LocationInfo("Malaysia", "Kuala Lumpur", 3.1390, 101.6869));
        return Collections.unmodifiableMap(locations);
    }

    private static class LocationInfo {
        final String country;
        final String capital;
        final double latitude;
        final double longitude;

        LocationInfo(String country, String capital, double latitude, double longitude) {
            this.country = country;
            this.capital = capital;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
