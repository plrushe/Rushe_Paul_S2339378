package com.example.rushe_paul_s2339378;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FeaturedCurrencyAdapter extends RecyclerView.Adapter<FeaturedCurrencyAdapter.FeaturedViewHolder> {

    private final List<Thing> featuredCurrencies = new ArrayList<>();
    private final CurrencyAdapter.OnCurrencyClickListener listener;
    private final DecimalFormat rateFormatter = new DecimalFormat("#,##0.00");

    public FeaturedCurrencyAdapter(CurrencyAdapter.OnCurrencyClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Thing> newCurrencies) {
        featuredCurrencies.clear();
        if (newCurrencies != null) {
            featuredCurrencies.addAll(newCurrencies);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_currency, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        Thing thing = featuredCurrencies.get(position);
        holder.bind(thing, listener, rateFormatter);
    }

    @Override
    public int getItemCount() {
        return featuredCurrencies.size();
    }

    static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        private final TextView codeView;
        private final TextView rateView;

        FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            codeView = itemView.findViewById(R.id.featuredCurrencyCode);
            rateView = itemView.findViewById(R.id.featuredRate);
        }

        void bind(final Thing thing, final CurrencyAdapter.OnCurrencyClickListener listener, DecimalFormat formatter) {
            String currencyCode = thing.getCurrencyCode();
            String rateText = thing.getDescription();

            codeView.setText(currencyCode);

            double rate = parseRate(rateText);
            String formattedRate = formatter.format(rate);
            rateView.setText(formattedRate);

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
