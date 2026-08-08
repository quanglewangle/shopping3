package com.quanglewangle.peter.shoppingBeta;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListActivity extends android.app.ListActivity implements AsyncTaskCompleteListener<String> {
    CupboardListAdapter adapter;
    ArrayList<HashMap<String, String>> products;
    int pos;
    private ProgressBar progressBar;

    private final Handler pollHandler = new Handler();
    private final Runnable showSpinnerRunnable = () -> {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    };
    private String lastKnownModified = "";
    private Runnable pollRunnable;

    private void startSpinner() {
        pollHandler.postDelayed(showSpinnerRunnable, 400);
    }

    private void stopSpinner() {
        pollHandler.removeCallbacks(showSpinnerRunnable);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void reloadList() {
        startSpinner();
        String url = isQuickShopMode() ? Constants.DUMP_LIST_QUICK_SHOP : Constants.DUMP_LIST;
        new LoadURL(ListActivity.this).execute(new String[]{url});
    }

    private void reloadListSilent() {
        String url = isQuickShopMode() ? Constants.DUMP_LIST_QUICK_SHOP : Constants.DUMP_LIST;
        new LoadURL(ListActivity.this).execute(new String[]{url});
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener prefListener =
        (prefs, key) -> {
            if (Constants.PREF_QUICK_SHOP_MODE.equals(key)) {
                String url = isQuickShopMode() ? Constants.DUMP_LIST_QUICK_SHOP : Constants.DUMP_LIST;
                new LoadURL(ListActivity.this).execute(new String[]{url});
            }
        };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pollRunnable = () -> {
            new LoadURL(result -> {
                if (!lastKnownModified.isEmpty() && !result.equals(lastKnownModified)) {
                    reloadListSilent();
                }
                lastKnownModified = result;
                pollHandler.postDelayed(pollRunnable, 3000);
            }).execute(new String[]{Constants.LAST_MODIFIED_URL});
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastKnownModified = "";
        getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(prefListener);
        setContentView(R.layout.list_list);
        progressBar = findViewById(R.id.progressBar);
        reloadList();
        pollHandler.removeCallbacks(pollRunnable);
        pollHandler.postDelayed(pollRunnable, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollHandler.removeCallbacks(pollRunnable);
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
            // Reload list with correct filter
            String url = newState ? Constants.DUMP_LIST_QUICK_SHOP : Constants.DUMP_LIST;
            new LoadURL(ListActivity.this).execute(new String[]{url});
            return true;
        }
        if (item.getItemId() == R.id.view_purchase_log) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskComplete(String result) {
        stopSpinner();
        getListView().setEnabled(true);
        products = new ArrayList<HashMap<String, String>>();
        try {
            JSONArray data_array = new JSONArray(result);

            for (int i = 0; i < data_array.length(); i++) {
                JSONObject obj = new JSONObject(data_array.get(i).toString());
                HashMap<String, String> map = new HashMap<String, String>();

                map.put("description", obj.getString("description"));
                map.put("barcode", obj.optString("barcode"));
                map.put("id", obj.getString("id"));
                map.put("quantity", obj.getString("quantity"));
                map.put("aisle", obj.optString("aisle"));
                map.put("quickShopMode", obj.optString("quickShop"));
                map.put("nectar", obj.optString("nectar", "false"));
                map.put("linked_id", obj.optString("linked_id", "0"));

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
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showEditDialog(position);
                return true;
            }
        });
    }

    protected void onListItemClick(ListView list, View v, int position, long id) {
        super.onListItemClick(list, v, position, id);
        getListView().setEnabled(false);
        startSpinner();
        pos = list.getFirstVisiblePosition();
        Map o = (HashMap<String, String>) this.getListAdapter().getItem(position);
        String productId = (String) o.get("id");
        String url;
        if (isQuickShopMode()) {
            url = Constants.SHOPPING_URL + "?cmd=ubT&newBasket=3&product_id=" + productId
                    + "&curBasket=2&setQuickShop=0&quickShopOnly=1";
        } else {
            url = Constants.SHOPPING_URL + "?cmd=ubT&newBasket=3&product_id=" + productId + "&curBasket=2";
        }
        new LoadURL(ListActivity.this).execute(new String[]{url});
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

        CheckBox quickShopCheckBox = (CheckBox) dialogView.findViewById(R.id.checkBox);
        quickShopCheckBox.setChecked(isQuickShopMode() || "true".equals(product.get("quickShopMode")));

        CheckBox nectarCheckBox = (CheckBox) dialogView.findViewById(R.id.nectarCheckBox);
        nectarCheckBox.setChecked("true".equals(product.get("nectar")));

        RadioButton toCupboard = (RadioButton) dialogView.findViewById(R.id.toCupboardRadioButton);
        RadioButton toList = (RadioButton) dialogView.findViewById(R.id.toListRadioButton);
        RadioButton toBasket = (RadioButton) dialogView.findViewById(R.id.toBasketRadioButton);
        toList.setChecked(true);

        HashMap<String, String> existingLinked = findLinkedPartner(product, products);
        TextView linkedItemValue = (TextView) dialogView.findViewById(R.id.linkedItemValue);
        String[] selectedLinkedId = {existingLinked != null ? existingLinked.get("id") : "0"};
        linkedItemValue.setText(existingLinked != null ? existingLinked.get("description") : "None");
        linkedItemValue.setOnClickListener(v -> showLinkPicker(productId, linkedItemValue, selectedLinkedId));

        AlertDialog dialog = builder.create();

        ((Button) dialogView.findViewById(R.id.buttonSend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetBasket = toCupboard.isChecked() ? "1" : toBasket.isChecked() ? "3" : "2";
                if ("1".equals(targetBasket) && existingLinked != null) {
                    new AlertDialog.Builder(ListActivity.this)
                            .setTitle("Linked item")
                            .setMessage("Move \"" + existingLinked.get("description") + "\" back to Cupboard too?")
                            .setPositiveButton("Yes", (d, w) -> sendListEdit(productId, targetBasket, descriptionInput, quantityInput, aisleInput, quickShopCheckBox, nectarCheckBox, toCupboard, toBasket, selectedLinkedId[0], existingLinked))
                            .setNegativeButton("No", (d, w) -> sendListEdit(productId, targetBasket, descriptionInput, quantityInput, aisleInput, quickShopCheckBox, nectarCheckBox, toCupboard, toBasket, selectedLinkedId[0], null))
                            .show();
                } else {
                    sendListEdit(productId, targetBasket, descriptionInput, quantityInput, aisleInput, quickShopCheckBox, nectarCheckBox, toCupboard, toBasket, selectedLinkedId[0], null);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /** Finds the product linked to {@code item} within {@code pool}, if any. */
    private HashMap<String, String> findLinkedPartner(HashMap<String, String> item, List<HashMap<String, String>> pool) {
        String linkedId = item.get("linked_id");
        if (linkedId == null || linkedId.equals("0") || pool == null) return null;
        for (HashMap<String, String> p : pool) {
            if (linkedId.equals(p.get("id"))) return p;
        }
        return null;
    }

    private void showLinkPicker(String productId, TextView linkedItemValue, String[] selectedLinkedId) {
        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        names.add("None");
        ids.add("0");
        if (products != null) {
            for (HashMap<String, String> p : products) {
                if (!p.get("id").equals(productId)) {
                    names.add(p.get("description"));
                    ids.add(p.get("id"));
                }
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Link to item")
                .setItems(names.toArray(new String[0]), (d, which) -> {
                    selectedLinkedId[0] = ids.get(which);
                    linkedItemValue.setText(names.get(which));
                })
                .show();
    }

    private void sendListEdit(String productId, String targetBasket, EditText descriptionInput,
                               EditText quantityInput, EditText aisleInput, CheckBox quickShopCheckBox,
                               CheckBox nectarCheckBox, RadioButton toCupboard, RadioButton toBasket,
                               String newLinkedId, HashMap<String, String> alsoMoveToCupboard) {
        try {
            String url = Constants.SHOPPING_URL + "?cmd=updR"
                + "&product_id=" + productId
                + "&newDescription=" + URLEncoder.encode(descriptionInput.getText().toString(), "UTF-8")
                + "&newQuantity=" + URLEncoder.encode(quantityInput.getText().toString(), "UTF-8")
                + "&newAisle=" + (aisleInput.getText().toString().isEmpty() ? "0" : URLEncoder.encode(aisleInput.getText().toString(), "UTF-8"))
                + "&curBasket=" + targetBasket
                + "&newPrice=0&newPriority="
                + "&newQuickShop=" + (toCupboard.isChecked() ? "0" : quickShopCheckBox.isChecked() ? "1" : "0")
                + "&newNectar=" + (toBasket.isChecked() ? "0" : nectarCheckBox.isChecked() ? "1" : "0")
                + "&newLinkedId=" + newLinkedId
                + "&displayBasket=2";
            new LoadURL(ListActivity.this).execute(new String[]{url});
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (alsoMoveToCupboard != null) {
            String linkedUrl = Constants.SHOPPING_URL + "?cmd=ubT&newBasket=1&product_id=" + alsoMoveToCupboard.get("id") + "&curBasket=2";
            new LoadURL(result -> {}).execute(new String[]{linkedUrl});
        }
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
