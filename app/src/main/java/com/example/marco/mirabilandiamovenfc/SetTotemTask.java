package com.example.marco.mirabilandiamovenfc;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by erik_ on 22/02/2017.
 */

public class SetTotemTask extends AsyncTask<String, Void, String> {

    Context context;
    public SetTotemTask(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        String connect_url = "http://192.168.43.34/set_totem.php";
        String totem_type = params[0];

        try {
            URL url = new URL(connect_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream OS = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter((new OutputStreamWriter(OS, "UTF-8")));
            String data = URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(totem_type, "UTF-8");
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            OS.close();

            InputStream IS = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader((new InputStreamReader(IS, "UTF-8")));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();
            IS.close();
            httpURLConnection.disconnect();

            return stringBuilder.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        MainActivity activity = (MainActivity) context;
        try {
            JSONObject jsonObject = new JSONObject(String.valueOf(result));
            JSONArray jsonArray = jsonObject.getJSONArray("totem_id");
            JSONObject object = jsonArray.getJSONObject(0);
            Toast.makeText(context.getApplicationContext(), "Totem configurato correttamente con ID: " + object.getInt("totemId"), Toast.LENGTH_LONG).show();
            new ManagementSharedPreference().save(context.getApplicationContext(), object.getInt("totemId"), object.getString("type"));
            activity.setVisibility();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
