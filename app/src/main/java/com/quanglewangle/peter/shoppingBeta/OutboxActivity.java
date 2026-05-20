package com.quanglewangle.peter.shopping3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

public class OutboxActivity extends ListActivity {
	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jsonParser = new JSONParser();

	ArrayList<HashMap<String, String>> outboxList;

	// products JSONArray
	JSONArray outbox = null;

	// Outbox JSON url
	//private static final String OUTBOX_URL = "http://api.androidhive.info/mail/outbox.json";

	private static final String OUTBOX_URL = "http://fimblefowl.co.uk/json";

	// ALL JSON node names
	private static final String TAG_BARCODE = "barcode";
	private static final String TAG_DESCRIPTION = "description";
	private static final String TAG_PRODUCTS = "products";
	private static final String TAG_MESSAGES = "messages";
	private static final String TAG_ID = "id";
	private static final String TAG_FROM = "from";
	private static final String TAG_EMAIL = "email";
	private static final String TAG_SUBJECT = "subject";
	private static final String TAG_DATE = "date";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.outbox_list);
		
		// Hashmap for ListView
        outboxList = new ArrayList<HashMap<String, String>>();
 
        // Loading OUTBOX in Background Thread
        new LoadOutbox().execute();
	}

	/**
	 * Background Async Task to Load all OUTBOX messages by making HTTP Request
	 * */
	class LoadOutbox extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(OutboxActivity.this);
			pDialog.setMessage("Loading Outbox ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Outbox JSON
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair("cmd", "dumpFiltered"));
            params.add(new NameValuePair("basket", "2"));
			// getting JSON string from URL
			String json = jsonParser.makeHttpRequest(OUTBOX_URL, params);

			// Check your log cat for JSON reponse
			Log.d("Outbox JSON: ", json.toString());

			try {
				JSONArray inbox =new JSONArray(json);

				// looping through All messages
				for (int i = 0; i < inbox.length(); i++) {
					JSONObject c = inbox.getJSONObject(i);

					// Storing each json item in variable
					String description = c.getString(TAG_DESCRIPTION);
					String barcode = c.getString(TAG_BARCODE);
					//			String subject = c.getString(TAG_SUBJECT);
					//			String date = c.getString(TAG_DATE);

					// creating new HashMap
					HashMap<String, String> map = new HashMap<String, String>();

					// adding each child node to HashMap key => value
					map.put(TAG_DESCRIPTION, description);
					map.put(TAG_BARCODE, barcode);
//					map.put(TAG_SUBJECT, subject);
//					map.put(TAG_DATE, date);

					// adding HashList to ArrayList
					outboxList.add(map);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */
					ListAdapter adapter = new SimpleAdapter(
							OutboxActivity.this, outboxList,
							R.layout.cupboard_list_item, new String[] {  TAG_DESCRIPTION, TAG_BARCODE, TAG_DATE },
							new int[] { R.id.description, R.id.barcode, R.id.date });
					// updating listview
					setListAdapter(adapter);
				}
			});

		}

	}
}