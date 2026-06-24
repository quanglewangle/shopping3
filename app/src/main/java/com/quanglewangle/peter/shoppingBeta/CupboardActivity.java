package com.quanglewangle.peter.shoppingBeta;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

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
    ArrayList<HashMap<String, String>> allProducts;
    EditText searchBox;
    int pos;

    private final Handler pollHandler = new Handler();
    private String lastKnownModified = "";
    private Runnable pollRunnable;

    private void reloadList() {
        new LoadURL(CupboardActivity.this).execute(new String[]{Constants.DUMP_CUPBOARD});
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pollRunnable = () -> {
            new LoadURL(result -> {
                if (!lastKnownModified.isEmpty() && !result.equals(lastKnownModified)) {
                    reloadList();
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
        setContentView(R.layout.cupboard_list);
        searchBox = (EditText) findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s.toString());
            }
        });
        reloadList();
        pollHandler.removeCallbacks(pollRunnable);
        pollHandler.postDelayed(pollRunnable, 3000);
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
            return true;
        }
        if (item.getItemId() == R.id.add_item) {
            showAddItemDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAddItemDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad * 2, pad, pad * 2, pad);

        EditText input = new EditText(this);
        input.setHint("Description");
        layout.addView(input);

        TextView warningText = new TextView(this);
        warningText.setTextColor(Color.rgb(180, 60, 0));
        warningText.setVisibility(View.GONE);
        layout.addView(warningText);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add item to cupboard")
                .setView(layout)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> similar = findSimilar(s.toString());
                if (similar.isEmpty()) {
                    warningText.setVisibility(View.GONE);
                } else {
                    warningText.setText("Similar items: " + TextUtils.join(", ", similar));
                    warningText.setVisibility(View.VISIBLE);
                }
            }
        });

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String desc = input.getText().toString().trim();
            if (desc.isEmpty()) return;
            List<String> similar = findSimilar(desc);
            if (!similar.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("Similar items exist")
                        .setMessage("Already in cupboard:\n• " + TextUtils.join("\n• ", similar) + "\n\nAdd anyway?")
                        .setPositiveButton("Add anyway", (d, w) -> { addNewItem(desc); dialog.dismiss(); })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                addNewItem(desc);
                dialog.dismiss();
            }
        });
    }

    private void addNewItem(String description) {
        try {
            String url = Constants.SHOPPING_URL + "?cmd=insertNew&barcode=&description="
                    + java.net.URLEncoder.encode(description, "UTF-8");
            new LoadURL(result -> reloadList()).execute(new String[]{url});
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private List<String> findSimilar(String query) {
        List<String> results = new ArrayList<>();
        if (allProducts == null || query.trim().length() < 2) return results;
        String q = query.toLowerCase().trim();
        for (HashMap<String, String> p : allProducts) {
            String desc = p.get("description");
            if (desc == null) continue;
            String d = desc.toLowerCase().trim();
            if (similarity(q, d) >= 0.65f || d.contains(q) || q.contains(d)) {
                results.add(desc.trim());
            }
        }
        return results;
    }

    private float similarity(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? dp[i - 1][j - 1]
                        : 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }
        return 1.0f - (float) dp[a.length()][b.length()] / Math.max(a.length(), b.length());
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

        allProducts = products;
        applyFilter(searchBox != null ? searchBox.getText().toString() : "");
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

    private void applyFilter(String query) {
        if (allProducts == null) return;
        ArrayList<HashMap<String, String>> filtered;
        if (query.trim().isEmpty()) {
            filtered = allProducts;
        } else {
            filtered = new ArrayList<>();
            String q = query.toLowerCase();
            for (HashMap<String, String> p : allProducts) {
                String desc = p.get("description");
                if (desc != null && desc.toLowerCase().contains(q)) {
                    filtered.add(p);
                }
            }
        }
        adapter = new CupboardListAdapter(this, filtered);
        setListAdapter(adapter);
        ((BaseAdapter) adapter).notifyDataSetChanged();
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
