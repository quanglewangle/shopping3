package com.quanglewangle.peter.shoppingBeta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class HistoryListAdapter extends BaseAdapter {

    private final ArrayList<HashMap<String, String>> entries;
    private final Context context;
    private final SimpleDateFormat inputFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputFormat =
            new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());

    HistoryListAdapter(Context context, ArrayList<HashMap<String, String>> entries) {
        this.context = context;
        this.entries = entries;
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolderItem {
        TextView description;
        TextView quantity;
        TextView timestamp;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.history_list_item, parent, false);

            holder = new ViewHolderItem();
            holder.description = convertView.findViewById(R.id.description);
            holder.quantity = convertView.findViewById(R.id.quantity);
            holder.timestamp = convertView.findViewById(R.id.timestamp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderItem) convertView.getTag();
        }

        HashMap<String, String> entry = entries.get(position);
        String store = entry.get("store");
        String description = entry.get("description");
        if (store != null && !store.isEmpty()) {
            description = description + " — " + store;
        }
        holder.description.setText(description);
        holder.quantity.setText(entry.get("quantity"));
        holder.timestamp.setText(formatTimestamp(entry.get("timestamp")));

        return convertView;
    }

    private String formatTimestamp(String raw) {
        if (raw == null) {
            return "";
        }
        try {
            return outputFormat.format(inputFormat.parse(raw));
        } catch (ParseException e) {
            return raw;
        }
    }
}
