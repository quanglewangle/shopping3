package com.quanglewangle.peter.shoppingBeta;

import android.app.ListActivity;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
