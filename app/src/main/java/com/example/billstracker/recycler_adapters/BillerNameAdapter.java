package com.example.billstracker.recycler_adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Biller;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BillerNameAdapter extends ArrayAdapter<Biller> {
    private final ArrayList<Biller> itemsAll;
    private final Context context;
    public BillerItemClickListener mClickListener;

    // View Types
    private static final int TYPE_BILLER = 0;
    private static final int TYPE_FOOTER = 1;

    public BillerNameAdapter(Context context, int viewResourceId, ArrayList<Biller> items) {
        super(context, viewResourceId, items);
        this.context = context;
        this.itemsAll = new ArrayList<>(items);
    }

    public void setClickListener(BillerItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface BillerItemClickListener {
        void onItemClick(int ignoredPosition, Biller biller);
    }

    @Override
    public int getCount() {
        // Add 1 for the footer if the list is not empty
        int count = super.getCount();
        return (count > 0) ? count + 1 : 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2; // Biller layout and Attribution Footer
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getCount() - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_BILLER;
    }

    @Nullable
    @Override
    public Biller getItem(int position) {
        // Fix for IndexOutOfBoundsException:
        // Return null for the footer position so AutoCompleteTextView doesn't crash
        if (getItemViewType(position) == TYPE_FOOTER) {
            return null;
        }
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        int viewType = getItemViewType(position);

        if (viewType == TYPE_FOOTER) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.attribution_footer, parent, false);
            }
            return convertView;
        }

        // Standard Biller Row
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.biller_search_result, parent, false);
        }

        Biller biller = getItem(position);
        if (biller != null) {
            TextView name = convertView.findViewById(R.id.foundBillerName);
            TextView website = convertView.findViewById(R.id.foundBillerWebsite);
            ImageView logo = convertView.findViewById(R.id.foundImage);

            name.setText(biller.getBillerName());
            website.setText(biller.getWebsite());

            String iconUrl = biller.getIcon();
            if (iconUrl != null && !iconUrl.isEmpty()) {
                Glide.with(context)
                        .load(iconUrl)
                        .circleCrop()
                        .dontAnimate()
                        .placeholder(R.drawable.invoices)
                        .error(R.drawable.invoices)
                        .into(logo);
            } else {
                logo.setImageResource(R.drawable.invoices);
            }

            convertView.setOnClickListener(view -> {
                if (mClickListener != null) {
                    mClickListener.onItemClick(position, biller);
                }
            });
        }
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<Biller> suggestions = new ArrayList<>();

                if (constraint != null && constraint.length() > 1) {
                    // 1. Local Search
                    for (Biller biller : itemsAll) {
                        if (biller.getBillerName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(biller);
                        }
                    }

                    // 2. Web Search
                    ArrayList<Biller> webResults = fetchBillersFromWeb(constraint.toString());
                    suggestions.addAll(webResults);

                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                if (results != null && results.count > 0) {
                    addAll((ArrayList<Biller>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    private ArrayList<Biller> fetchBillersFromWeb(String query) {
        ArrayList<Biller> results = new ArrayList<>();
        HttpURLConnection connection = null;
        String LOGO_DEV_KEY = "pk_FRytk_swTsCM4Xi2D3cqeQ";

        try {
            String encodedQuery = Uri.encode(query);
            URL url = new URL("https://api.clearout.io/public/companies/autocomplete?query=" + encodedQuery);

            connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);

                JSONObject root = new JSONObject(response.toString());
                JSONArray data = root.getJSONArray("data");

                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    String domain = obj.optString("domain");

                    if (!domain.isEmpty()) {
                        Biller biller = new Biller();
                        biller.setBillerName(obj.getString("name"));
                        biller.setWebsite(domain);

                        // CATEGORY: Extract industry/category from Clearout API
                        //String category = obj.optString("industry", "Other");
                        //biller.set

                        // LOGO: Construct the Logo.dev URL
                        String logoUrl = "https://img.logo.dev/" + domain + "?token=" + LOGO_DEV_KEY;
                        biller.setIcon(logoUrl);

                        results.add(biller);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("BillerAPI", "Error fetching data", e);
        } finally {
            if (connection != null) connection.disconnect();
        }
        return results;
    }
}