package com.quanglewangle.peter.shoppingBeta;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 29/07/2017.
 */

public class BasketActivity extends ListActivity implements AsyncTaskCompleteListener<String> {
    CupboardListAdapter adapter;
    ArrayList<HashMap<String, String>> products;
    int pos;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.basket_list);
        LoadURL loadUrl = new LoadURL(BasketActivity.this);
        loadUrl.execute(new String[]{Constants.DUMP_BASKET});
    }

    @Override
    public void onTaskComplete(String result) {
        Log.d("IN CALLBACK ", result);

        products = new ArrayList<HashMap<String, String>>();
        try {
            JSONArray data_array = new JSONArray(result);

            for (int i = 0; i < data_array.length(); i++) {
                JSONObject obj = new JSONObject(data_array.get(i).toString());
                Log.d("in get data", data_array.get(i).toString());
                HashMap<String, String> map = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                map.put("description", obj.getString("description"));

                map.put("barcode", obj.optString("barcode"));
                map.put("id", obj.getString("id"));

                // adding HashList to ArrayList
                products.add(map);
                Log.d("adding", "products size: " + products.size());
            }
//			adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }

       /* adapter = new SimpleAdapter(
                this, products,
                R.layout.cupboard_list_item, new String[]{"description", "barcode", "date"},
                new int[]{R.id.description, R.id.barcode, R.id.date}); */
        adapter = new CupboardListAdapter(this, products);
        getListView().setSelectionFromTop(pos, 0);
        // updating listview
        setListAdapter(adapter);
        Log.d("about to call update ", "products size: " + products.size());
//        adapter.update(products);
        ((BaseAdapter) adapter).notifyDataSetChanged();
    }

    protected void onListItemClick(ListView list, View v, int position, long id) {
        super.onListItemClick(list, v, position, id);
        pos = list.getFirstVisiblePosition();
        Map o = (HashMap<String, String>) this.getListAdapter().getItem(position);
        LoadURL loadUrl = new LoadURL(BasketActivity.this);
        loadUrl.execute(new String[]{Constants.SHOPPING_URL+"?cmd=ubT&newBasket=1&product_id=" + o.get("id") + "&curBasket=3"});
    }

}

