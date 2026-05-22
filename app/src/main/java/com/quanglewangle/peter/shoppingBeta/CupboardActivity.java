package com.quanglewangle.peter.shoppingBeta;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CupboardActivity extends ListActivity implements AsyncTaskCompleteListener<String> {
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
        new LoadURL(CupboardActivity.this).execute(new String[]{Constants.DUMP_CUPBOARD});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.quick_shop_mode).setChecked(isQuickShopMode());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.quick_shop_mode) {
            boolean newState = !item.isChecked();
            item.setChecked(newState);
            setQuickShopMode(newState);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public void onTaskComplete(String result) {
        products = new ArrayList<HashMap<String, String>>();
        try {
            JSONArray data_array = new JSONArray(result);

            for (int i = 0; i < data_array.length(); i++) {
                JSONObject obj = new JSONObject(data_array.get(i).toString());
                HashMap<String, String> map = new HashMap<String, String>();

                map.put("description", obj.getString("description"));
                map.put("quantity", obj.getString("quantity"));
                map.put("quickShopMode", obj.optString("quickShop"));
                map.put("id", obj.getString("id"));
                map.put("aisle", obj.optString("aisle"));

                products.add(map);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter = new CupboardListAdapter(this, products);
        setListAdapter(adapter);
        ((BaseAdapter) adapter).notifyDataSetChanged();
        getListView().setSelectionFromTop(pos, 0);
        getListView().setLongClickable(true);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
                showAlertDialog(position);
                return true;
            }
        });
    }

    protected void onListItemClick(ListView list, View v, int position, long id) {
        super.onListItemClick(list, v, position, id);
        pos = list.getFirstVisiblePosition();

        Map o = (HashMap<String, String>) this.getListAdapter().getItem(position);
        String productId = (String) o.get("id");
        String url;
        if (isQuickShopMode()) {
            url = Constants.SHOPPING_URL + "?cmd=ubT&newBasket=2&product_id=" + productId + "&curBasket=1&setQuickShop=1";
        } else {
            url = Constants.SHOPPING_URL + "?cmd=ubT&newBasket=2&product_id=" + productId + "&curBasket=1";
        }
        new LoadURL(CupboardActivity.this).execute(new String[]{url});
    }

    private void showAlertDialog(int position) {
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

        CheckBox quickShopCheckBox = (CheckBox) dialogView.findViewById(R.id.checkBox);
        quickShopCheckBox.setChecked(isQuickShopMode() || "true".equals(product.get("quickShopMode")));

        RadioButton toCupboard = (RadioButton) dialogView.findViewById(R.id.toCupboardRadioButton);
        RadioButton toList = (RadioButton) dialogView.findViewById(R.id.toListRadioButton);
        RadioButton toBasket = (RadioButton) dialogView.findViewById(R.id.toBasketRadioButton);
        toCupboard.setChecked(true);

        AlertDialog dialog = builder.create();

        ((Button) dialogView.findViewById(R.id.buttonSend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetBasket = toList.isChecked() ? "2" : toBasket.isChecked() ? "3" : "1";
                try {
                    String url = Constants.SHOPPING_URL + "?cmd=updR"
                        + "&product_id=" + productId
                        + "&newDescription=" + URLEncoder.encode(descriptionInput.getText().toString(), "UTF-8")
                        + "&newQuantity=" + URLEncoder.encode(quantityInput.getText().toString(), "UTF-8")
                        + "&newAisle=" + (aisleInput.getText().toString().isEmpty() ? "0" : URLEncoder.encode(aisleInput.getText().toString(), "UTF-8"))
                        + "&curBasket=" + targetBasket
                        + "&newPrice=0&newPriority="
                        + "&newQuickShop=" + (quickShopCheckBox.isChecked() ? "1" : "0")
                        + "&displayBasket=1";
                    new LoadURL(CupboardActivity.this).execute(new String[]{url});
                } catch (java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean isQuickShopMode() {
        return getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .getBoolean(Constants.PREF_QUICK_SHOP_MODE, false);
    }

    private void setQuickShopMode(boolean value) {
        getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE).edit()
                .putBoolean(Constants.PREF_QUICK_SHOP_MODE, value).apply();
    }
}
