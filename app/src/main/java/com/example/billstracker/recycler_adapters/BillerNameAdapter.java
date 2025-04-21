package com.example.billstracker.recycler_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Biller;
import com.example.billstracker.tools.Tools;

import java.util.ArrayList;

    public class BillerNameAdapter extends ArrayAdapter<Biller> {
        private final ArrayList<Biller> items;
        private final ArrayList<Biller> itemsAll;
        private final ArrayList<Biller> suggestions;
        private final int viewResourceId;
        public BillerItemClickListener mClickListener;
        private final Context context;

        @SuppressWarnings("unchecked")
        public BillerNameAdapter(Context context1, int viewResourceId, ArrayList<Biller> items) {
            super(context1, viewResourceId, items);
            context = context1;
            this.items = items;
            this.itemsAll = (ArrayList<Biller>) items.clone();
            this.suggestions = new ArrayList<>();
            this.viewResourceId = viewResourceId;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            boolean darkMode = Tools.isDarkMode(context);
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(viewResourceId, null);
            }
            Biller biller = items.get(position);
            if (biller != null) {
                ImageView image = v.findViewById(R.id.foundImage);
                if (image != null) {
                    Glide.with(image).load(biller.getIcon()).into(image);
                    if (darkMode) {
                        image.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.circle, context.getTheme()));
                        image.setPadding(10,10,10,10);
                    }
                }
                TextView name = v.findViewById(R.id.foundBillerName);
                if (name != null) {
                    name.setText(biller.getBillerName());
                }
                TextView website = v.findViewById(R.id.foundBillerWebsite);
                if (website != null) {
                    website.setText(biller.getWebsite());
                }
            }

            v.setOnClickListener(view -> {
                if (mClickListener != null) mClickListener.onItemClick(position, items.get(position));
            });

            return v;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return nameFilter;
        }

        final Filter nameFilter = new Filter() {
            public String convertResultToString(Object resultValue) {
                return ((Biller) (resultValue)).getBillerName();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    suggestions.clear();
                    for (Biller biller : itemsAll) {
                        if (biller.getBillerName().toLowerCase()
                                .contains(constraint.toString().toLowerCase())) {
                            suggestions.add(biller);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                @SuppressWarnings("unchecked")
                ArrayList<Biller> filteredList = (ArrayList<Biller>) results.values;
                if (results.count > 0) {
                    clear();
                    for (Biller c : filteredList) {
                        add(c);
                    }
                    notifyDataSetChanged();
                }
            }
        };

        public void setClickListener(BillerNameAdapter.BillerItemClickListener itemClickListener) {
            this.mClickListener = itemClickListener;
        }
        public interface BillerItemClickListener {
            void onItemClick(int ignoredPosition, Biller biller);
        }
}
