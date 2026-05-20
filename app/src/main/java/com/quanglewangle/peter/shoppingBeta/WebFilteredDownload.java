package com.quanglewangle.peter.shopping3;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebFilteredDownload implements Runnable {
	
	public download_complete caller;
	
	public interface download_complete {
		public void get_data(String data);
	}	
	  
    WebFilteredDownload(download_complete caller) {

        this.caller = caller;
    }
    
    private String link;

    public void doRequest(String link) {
    	this.link = link;
	    Thread t = new Thread(this);
		t.start();
    }

     public void run() {
       //	caller.get_data(download(this.link));
        threadMsg(download(this.link));
     }
     
     private void threadMsg(String msg) {

         if (!msg.equals(null) && !msg.equals("")) {
             Message msgObj = handler.obtainMessage();
             Bundle b = new Bundle();
             b.putString("message", msg);
             msgObj.setData(b);
             handler.sendMessage(msgObj);
         }
     }
     private final Handler handler = new Handler() {

         public void handleMessage(Message msg) {
              
             String Response = msg.getData().getString("message");
            // get_data fills products array
             caller.get_data(Response);

         }
     };

 	public static String download(String basket) {
         URL website;
         StringBuilder response = null;
 		try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("fimblefowl.co.uk")
                    .appendPath("json")
                    .appendQueryParameter("cmd", "dumpFiltered")
                    .appendQueryParameter("basket", basket);
            String myUrl = builder.build().toString();
 			website = new URL(myUrl);
 		    Log.d("GETTing: ", myUrl);
 			HttpURLConnection connection = (HttpURLConnection) website.openConnection();

            connection.setRequestProperty("charset", "utf-8");
            Log.d("PETER", "Response " + connection.getResponseMessage());
            BufferedReader in = new BufferedReader(
                 new InputStreamReader(
                     connection.getInputStream()));
            response = new StringBuilder();
            String inputLine;
	
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                Log.d("PETER", "Got" + inputLine);
            }
            in.close();

 		} catch (Exception e) {
            Log.d("PETER", "exception" + e.toString());
            return "";
 		}
         return response.toString();
     }
}


