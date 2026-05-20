package com.quanglewangle.peter.shopping3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
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

        Log.d("PETER", "in CupboardActivity ocCreate");
        Intent myIntent = getIntent(); // gets the previously created intent
        String firstKeyName = myIntent.getStringExtra("firstKeyName");

        Log.d("PETER", "intent parameter " + firstKeyName);
        //	setContentView(R.layout.activity_main);
        setContentView(R.layout.cupboard_list);
    }
    @Override

    protected void onResume() {
        super.onResume();
        setContentView(R.layout.cupboard_list);
        LoadURL loadUrl = new LoadURL(CupboardActivity.this);
        loadUrl.execute(new String[]{Constants.SHOPPING_URL+"?cmd=dumpFiltered&curBasket=1"});
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        final int listPosition = info.position;
        products.get(listPosition).get("description");
        switch (item.getItemId()) {
            case R.id.quantity:
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_layout);
                dialog.show();

 /*               String title = products.get(listPosition).get("description");
                Toast.makeText(getApplicationContext(), products.get(listPosition).get("description")+" "+"quantity", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(title);
                alert.setMessage("Set quantity or note");

                // Set an EditText view to get user input
                final EditText input;
                input = new EditText(this);
                input.setText( products.get(listPosition).get("quantity"));
                input.setSelection(input.getText().length());
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String value = input.getText().toString();
                        Toast.makeText(getApplicationContext(), "Dialog got:" + value, Toast.LENGTH_SHORT).show();
                        Map o = (HashMap<String, String>) products.get(listPosition);
                        LoadURL loadUrl = new LoadURL(CupboardActivity.this);
                        loadUrl.execute(new String[]{"http://fimblefowl.co.uk/json?cmd=ubQ&newQuantity="+value+ "&product_id=" + o.get("id")});

                        return;
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                return;
                            }
                        });
                alert.show();

                return true;
                */
            case R.id.description:
                Toast.makeText(getApplicationContext(), "description", Toast.LENGTH_SHORT).show();
                String title2 = products.get(listPosition).get("description");
                Toast.makeText(getApplicationContext(), products.get(listPosition).get("description")+" "+"quantity", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
                alert2.setTitle(title2);
                alert2.setMessage("Edit description");

                // Set an EditText view to get user input
                final EditText input2;
                input2 = new EditText(this);
                input2.setText( products.get(listPosition).get("description"));
                input2.setSelection(input2.getText().length());
                alert2.setView(input2);

                alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String value = input2.getText().toString();
                        Toast.makeText(getApplicationContext(), "Dialog got:" + value, Toast.LENGTH_SHORT).show();
                        Map o = (HashMap<String, String>) products.get(listPosition);
                        LoadURL loadUrl = new LoadURL(CupboardActivity.this);
                        loadUrl.execute(new String[]{Constants.SHOPPING_URL+"?cmd=uBd&newDescription="+value+ "&product_id=" + o.get("id")});

                        return;
                    }
                });

                alert2.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                return;
                            }
                        });
                alert2.show();

                return true;
            default:
        }
        return true;
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

        registerForContextMenu(getListView());
    }

    protected void onListItemClick(ListView list, View v, int position, long id) {
        super.onListItemClick(list, v, position, id);
        pos = list.getFirstVisiblePosition();

        Map o = (HashMap<String, String>) this.getListAdapter().getItem(position);
        LoadURL loadUrl = new LoadURL(CupboardActivity.this);
        loadUrl.execute(new String[]{Constants.SHOPPING_URL+"?cmd=ubT&newBasket=2&product_id=" + o.get("id") + "&curBasket=1"});
    }

    public void sendForm(View view) {
    }
}

