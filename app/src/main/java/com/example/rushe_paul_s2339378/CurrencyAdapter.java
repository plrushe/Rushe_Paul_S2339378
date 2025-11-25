package com.example.rushe_paul_s2339378;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {

    public interface OnCurrencyClickListener {
        void onCurrencyClick(Thing thing);
    }

    // keep both the raw list and the filtered view handy
    private final List<Thing> currencies = new ArrayList<>();
    private final List<Thing> filteredCurrencies = new ArrayList<>();
    private final OnCurrencyClickListener listener;

    public CurrencyAdapter(OnCurrencyClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Thing> newCurrencies) {
        currencies.clear();
        filteredCurrencies.clear();
        if (newCurrencies != null) {
            currencies.addAll(newCurrencies);
            filteredCurrencies.addAll(newCurrencies);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredCurrencies.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredCurrencies.addAll(currencies);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Thing thing : currencies) {
                String code = thing.getCurrencyCode();
                if (code != null && code.toLowerCase().contains(lowerQuery)) {
                    filteredCurrencies.add(thing);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false);
        return new CurrencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        Thing thing = filteredCurrencies.get(position);
        holder.bind(thing, listener);
    }

    @Override
    public int getItemCount() {
        return filteredCurrencies.size();
    }

    static class CurrencyViewHolder extends RecyclerView.ViewHolder {
        private final TextView codeText;

        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            codeText = itemView.findViewById(R.id.currencyCode);
        }

        void bind(final Thing thing, final OnCurrencyClickListener listener) {
            // quick grab of the bits we show
            String currencyCode = thing.getCurrencyCode();
            String rateText = thing.getDescription();

            codeText.setText(currencyCode);

            double rate = parseRate(rateText);
            int backgroundColor = pickBackgroundColor(rate);
            if (itemView instanceof com.google.android.material.card.MaterialCardView) {
                ((com.google.android.material.card.MaterialCardView) itemView).setCardBackgroundColor(backgroundColor);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onCurrencyClick(thing);
                    }
                }
            });
        }

        private double parseRate(String description) {
            if (description == null || description.trim().isEmpty()) {
                return -1;
            }
            try {
                return Double.parseDouble(description.trim());
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        private int pickBackgroundColor(double rate) {
            int colorRes;
            if (rate < 0) {
                colorRes = R.color.rateUnknownBackground;
            } else if (rate < 1.0) {
                colorRes = R.color.rateBelowOneBackground;
            } else if (rate < 5.0) {
                colorRes = R.color.rateOneToFiveBackground;
            } else if (rate < 10.0) {
                colorRes = R.color.rateFiveToTenBackground;
            } else {
                colorRes = R.color.rateAboveTenBackground;
            }
            return ContextCompat.getColor(itemView.getContext(), colorRes);
        }
    }
}
