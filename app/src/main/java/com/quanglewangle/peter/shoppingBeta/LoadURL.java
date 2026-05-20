package com.quanglewangle.peter.shopping3;
import android.os.AsyncTask;
import android.util.Log;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadURL extends AsyncTask<String, Process, String> {

    private AsyncTaskCompleteListener<String> callback;
    private int postion;

    public LoadURL(AsyncTaskCompleteListener<String> cb) {
        this.callback = cb;
    }

    protected void onPreExecute() {}

    protected String doInBackground(String... urls) {
        OkHttpClient client = new OkHttpClient();
        Request request =
                new Request.Builder()
                        .url(urls[0])
                        .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();

            }
        } catch (Exception e) {
            Log.e("ASYNC", e.toString());
            return "Fail";
        }
        return "can't happen";
    }


    protected void onPostExecute(String content) {
        if (callback != null)
            callback.onTaskComplete(content);
    }
}