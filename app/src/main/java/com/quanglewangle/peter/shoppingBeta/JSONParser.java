package com.quanglewangle.peter.shopping3;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONObject;
 
import android.util.Log;
 
public class JSONParser {
 
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
 
    // constructor
    public JSONParser() {
    }
 
    // function get json from url
    // by making HTTP POST or GET mehtod
    public String makeHttpRequest(String url, List<NameValuePair> params){

        // Making HTTP request
        try {
 

                // request method is GET
               // DefaultHttpClient httpClient = new DefaultHttpClient();
            String paramString = "?";

            for (NameValuePair item : params) {
                paramString += item;
            }

                

            url +=  paramString;
              //  HttpGet httpGet = new HttpGet(url);
                URL urlObj = new URL(url );
                HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
                InputStream is = urlConnection.getInputStream();

              //  HttpResponse httpResponse = httpClient.execute(httpGet);
              //  HttpEntity httpEntity = httpResponse.getEntity();


 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
 Log.d("JSON Parser got:", json);

        return json;
    }
}