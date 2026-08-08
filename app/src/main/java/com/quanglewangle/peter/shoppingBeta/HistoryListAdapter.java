package com.quanglewangle.peter.shoppingBeta;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

    // A little Gmail-style colored initial badge instead of spelling out the store name.
    private static final int[] BADGE_COLORS = {
            0xFF1976D2, 0xFF388E3C, 0xFFD32F2F, 0xFF7B1FA2, 0xFFF57C00, 0xFF00796B,
    };

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
        TextView storeBadge;
        TextView description;
        TextView timestamp;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.history_list_item, parent, false);

            holder = new ViewHolderItem();
            holder.storeBadge = convertView.findViewById(R.id.store_badge);
            holder.description = convertView.findViewById(R.id.description);
            holder.timestamp = convertView.findViewById(R.id.timestamp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderItem) convertView.getTag();
        }

        HashMap<String, String> entry = entries.get(position);
        String store = entry.get("store");
        holder.description.setText(entry.get("description"));
        holder.timestamp.setText(formatTimestamp(entry.get("timestamp")));

        if (store != null && !store.isEmpty()) {
            holder.storeBadge.setVisibility(View.VISIBLE);
            holder.storeBadge.setText(store.substring(0, 1).toUpperCase(Locale.getDefault()));
            Drawable badgeBackground = holder.storeBadge.getBackground().mutate();
            badgeBackground.setTint(BADGE_COLORS[Math.floorMod(store.hashCode(), BADGE_COLORS.length)]);
            holder.storeBadge.setBackground(badgeBackground);
        } else {
            holder.storeBadge.setVisibility(View.GONE);
        }

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
