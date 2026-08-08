package com.quanglewangle.peter.shoppingBeta;

import android.app.ListActivity;
import android.os.Bundle;
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
                HashMap<String, String> map = new HashMap<>();
                map.put("description", obj.getString("description"));
                map.put("quantity", obj.optString("quantity"));
                map.put("timestamp", obj.optString("timestamp"));
                map.put("store", obj.optString("store"));
                entries.add(map);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Couldn't load purchase log", Toast.LENGTH_SHORT).show();
        }

        setListAdapter(new HistoryListAdapter(this, entries));
    }
}
