package com.quanglewangle.peter.shoppingBeta;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class HistoryActivity extends ListActivity implements AsyncTaskCompleteListener<String> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_list);
        setTitle("Purchase Log");

        // Android 16+ forces edge-to-edge, so the action bar overlaps the list's first
        // row. Same fix as AndroidTabAndListView's tab bar padding.
        if (Build.VERSION.SDK_INT >= 35) {
            ListView list = getListView();
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, getResources().getDisplayMetrics());
            }
            int statusBarHeight = 0;
            int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resId);
            }
            final int topPadding = statusBarHeight + actionBarHeight;
            list.post(() -> list.setPadding(list.getPaddingLeft(), topPadding,
                    list.getPaddingRight(), list.getPaddingBottom()));
        }

        getListView().setLongClickable(true);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                confirmDelete(position);
                return true;
            }
        });
    }

    private void confirmDelete(int position) {
        @SuppressWarnings("unchecked")
        HashMap<String, String> entry = (HashMap<String, String>) getListAdapter().getItem(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete log entry")
                .setMessage("Remove \"" + entry.get("description") + "\" from the purchase log?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEntry(entry))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEntry(HashMap<String, String> entry) {
        try {
            String url = Constants.SHOPPING_URL + "?cmd=deleteHistory"
                    + "&id=" + entry.get("id")
                    + "&timestamp=" + URLEncoder.encode(entry.get("timestamp"), "UTF-8");
            new LoadURL(this).execute(new String[]{url});
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadURL(this).execute(new String[]{Constants.GET_HISTORY});
    }

    @Override
    public void onTaskComplete(String result) {
        ArrayList<HashMap<String, String>> entries = new ArrayList<>();
        try {
            JSONArray dataArray = new JSONArray(result);
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject obj = dataArray.getJSONObject(i);
                String store = obj.optString("store");
                if (store == null || store.isEmpty() || "Home".equalsIgnoreCase(store)) {
                    continue;
                }
                HashMap<String, String> map = new HashMap<>();
                map.put("id", obj.optString("id"));
                map.put("description", obj.getString("description"));
                map.put("timestamp", obj.optString("timestamp"));
                map.put("store", store);
                entries.add(map);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Couldn't load purchase log", Toast.LENGTH_SHORT).show();
        }

        setListAdapter(new HistoryListAdapter(this, entries));
    }
}
