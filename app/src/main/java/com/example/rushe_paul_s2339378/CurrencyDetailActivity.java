package com.example.rushe_paul_s2339378;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class CurrencyDetailActivity extends AppCompatActivity {

    private double rate = 1.0;
    private String currencyCode = "";
    private final DecimalFormat formatter = new DecimalFormat("#,##0.00");

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
        EditText amountInput = findViewById(R.id.amountInput);
        RadioGroup directionGroup = findViewById(R.id.directionGroup);
        RadioButton directionToCurrency = findViewById(R.id.directionToCurrency);
        RadioButton directionToGbp = findViewById(R.id.directionToGbp);

        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String pubDate = getIntent().getStringExtra("pubDate");
        currencyCode = getIntent().getStringExtra("currencyCode");

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
}
