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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 29/07/2017.
 */


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
        LoadURL loadUrl = new LoadURL(CupboardActivity.this);
        loadUrl.execute(new String[]{Constants.DUMP_CUPBOARD});
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
//        Log.d("IN CALLBACK ", result);

        products = new ArrayList<HashMap<String, String>>();
        try {
            JSONArray data_array = new JSONArray(result);

            for (int i = 0; i < data_array.length(); i++) {
                JSONObject obj = new JSONObject(data_array.get(i).toString());
                Log.d("in get data", data_array.get(i).toString());
                HashMap<String, String> map = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                map.put("description", obj.getString("description"));
                map.put("quantity", obj.getString("quantity"));
                map.put("barcode", obj.optString("barcode"));
                map.put("id", obj.getString("id"));

                // adding HashList to ArrayList
                products.add(map);
 //               Log.d("adding", "products size: " + products.size());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter = new CupboardListAdapter(this, products);

        // updating listview
        setListAdapter(adapter);
 //       Log.d("about to call update ", "products size: " + products.size());

        ((BaseAdapter) adapter).notifyDataSetChanged();
        getListView().setSelectionFromTop(pos, 0);
        getListView().setLongClickable(true);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // TODO Auto-generated method stub

                Log.d("long clicked","pos: " + pos);
                showAlertDialog(pos);
                return true;
            }
        });
    }

    protected void onListItemClick(ListView list, View v, int position, long id) {
        super.onListItemClick(list, v, position, id);
        pos = list.getFirstVisiblePosition();

        Map o = (HashMap<String, String>) this.getListAdapter().getItem(position);
        LoadURL loadUrl = new LoadURL(CupboardActivity.this);
        loadUrl.execute(new String[]{Constants.SHOPPING_URL+"?cmd=ubT&newBasket=2&product_id=" + o.get("id") + "&curBasket=1"});
    }

    private void showAlertDialog(int position) {
        Map o = (HashMap<String, String>) this.getListAdapter().getItem(position);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_layout, null);  // this line
        alertDialog.setView(v);
        AlertDialog dialog = alertDialog.create();

        EditText description = (EditText) v.findViewById(R.id.descriptionInput);
        description.setText((CharSequence) o.get("description"));
        TextView item_name = (TextView) v.findViewById(R.id.item_name);
        item_name.setText((CharSequence) o.get("description"));

        EditText note = (EditText) v.findViewById(R.id.noteInput);
        note.setText((CharSequence) o.get("quantity"));
        TextView itemLabel = (TextView) v.findViewById(R.id.notesLabel);
        itemLabel.setText((CharSequence) o.get("quantity"));

        Button bt_yes = (Button) v.findViewById(R.id.buttonSend);
        bt_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("button", "pushed" + description + item_name);

            }
        });
        RadioButton rb = (RadioButton) v.findViewById(R.id.toCupboardRadioButton);

        View.OnClickListener toCupboardRadioButtonListener = null;
   //     rb.setOnClickListener(toCupboardRadioButtonListener);

        toCupboardRadioButtonListener = new View.OnClickListener() {
            public void onClick(View v) {
                //Your Implementaions...
                Log.d("radiobutton", "cupboard");
            }
        };


        AlertDialog alert = alertDialog.create();
        alert.show();
    }
}

