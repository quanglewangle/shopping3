package com.quanglewangle.peter.shoppingBeta;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 29/07/2017.
 */


public class ListActivity extends android.app.ListActivity implements AsyncTaskCompleteListener<String> {
    CupboardListAdapter adapter;
    ArrayList<HashMap<String, String>> products;
    int pos;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.cupboard_list);
        LoadURL loadUrl = new LoadURL(ListActivity.this);
        loadUrl.execute(new String[]{Constants.DUMP_LIST});
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
                map.put("quantity", obj.getString("quantity"));
                map.put("aisle", obj.optString("aisle"));
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

        // updating listview
        setListAdapter(adapter);
        Log.d("about to call update ", "products size: " + products.size());
//        adapter.update(products);
        ((BaseAdapter) adapter).notifyDataSetChanged();
        getListView().setSelectionFromTop(pos, 0);
        getListView().setLongClickable(true);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showEditDialog(position);
                return true;
            }
        });
    }

    protected void onListItemClick(ListView list, View v, int position, long id) {
        super.onListItemClick(list, v, position, id);
        pos = list.getFirstVisiblePosition();
        Map o = (HashMap<String, String>) this.getListAdapter().getItem(position);
        //     final WebChangeBasketStatus webrequest = new WebChangeBasketStatus((WebChangeBasketStatus.download_complete)this);
        //    webrequest.doRequest("http://fimblefowl.co.uk/json?cmd=ubT&basket=2&product_id=" + o.get("id"));
        LoadURL loadUrl = new LoadURL(ListActivity.this);
        loadUrl.execute(new String[]{Constants.SHOPPING_URL+"?cmd=ubT&newBasket=3&product_id=" + o.get("id") + "&curBasket=2"});
    }

    private void showEditDialog(int position) {
        HashMap<String, String> product = (HashMap<String, String>) this.getListAdapter().getItem(position);
        String productId = product.get("id");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        builder.setView(dialogView);

        ((TextView) dialogView.findViewById(R.id.item_name)).setText(product.get("description"));

        EditText descriptionInput = (EditText) dialogView.findViewById(R.id.descriptionInput);
        descriptionInput.setText(product.get("description"));

        EditText quantityInput = (EditText) dialogView.findViewById(R.id.noteInput);
        quantityInput.setText(product.get("quantity"));

        EditText aisleInput = (EditText) dialogView.findViewById(R.id.aisleInput);
        aisleInput.setText(product.get("aisle"));

        RadioButton toCupboard = (RadioButton) dialogView.findViewById(R.id.toCupboardRadioButton);
        RadioButton toList = (RadioButton) dialogView.findViewById(R.id.toListRadioButton);
        RadioButton toBasket = (RadioButton) dialogView.findViewById(R.id.toBasketRadioButton);
        toList.setChecked(true);

        AlertDialog dialog = builder.create();

        ((Button) dialogView.findViewById(R.id.buttonSend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetBasket = toCupboard.isChecked() ? "1" : toBasket.isChecked() ? "3" : "2";
                try {
                    String url = Constants.SHOPPING_URL + "?cmd=updR"
                        + "&product_id=" + productId
                        + "&newDescription=" + URLEncoder.encode(descriptionInput.getText().toString(), "UTF-8")
                        + "&newQuantity=" + URLEncoder.encode(quantityInput.getText().toString(), "UTF-8")
                        + "&newAisle=" + (aisleInput.getText().toString().isEmpty() ? "0" : URLEncoder.encode(aisleInput.getText().toString(), "UTF-8"))
                        + "&curBasket=" + targetBasket
                        + "&newPrice=0&newPriority="
                        + "&displayBasket=2";
                    new LoadURL(ListActivity.this).execute(new String[]{url});
                } catch (java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}

