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
import java.nio.Buffer;

/**
 * Created by Marco on 13/02/2017.
 */

public class BackgroundTask extends AsyncTask<String, Void, String> {

    Context ctx;
    //Integer points1;

    BackgroundTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String connect_url = "http://192.168.1.7/insert_points_database.php";
        String codeId = params[0];
        String points = params[1];

        try {
            URL url = new URL(connect_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream OS = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter((new OutputStreamWriter(OS, "UTF-8")));
            String data = URLEncoder.encode("codeId", "UTF-8") + "=" + URLEncoder.encode(codeId, "UTF-8")+"&"+
                    URLEncoder.encode("points","UTF-8") + "="+URLEncoder.encode(String.valueOf(points),"UTF-8");
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            OS.close();

            InputStream IS = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader((new InputStreamReader(IS, "UTF-8")));

            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();
            IS.close();
            httpURLConnection.disconnect();


            /*URL url1 = new URL(points_url);
            HttpURLConnection httpURLConnection1 = (HttpURLConnection) url1.openConnection();
            httpURLConnection1.setRequestMethod("POST");
            httpURLConnection1.setDoOutput(true);
            OutputStream os = httpURLConnection1.getOutputStream();
            BufferedWriter bf = new BufferedWriter((new OutputStreamWriter(OS, "UTF-8")));
            String data_points = URLEncoder.encode("codeId", "UTF-8") + "=" + URLEncoder.encode(codeId, "UTF-8")+"&"+
                    URLEncoder.encode("points","UTF-8") + "="+URLEncoder.encode(String.valueOf(points),"UTF-8");
            bf.write(data_points);
            bf.flush();
            bf.close();
            os.close();


            httpURLConnection1.disconnect();*/


            return stringBuilder.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
  /*JSONObject jsonObject = new JSONObject(String.valueOf(stringBuilder));
            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
            JSONObject object = jsonArray.getJSONObject(0);
            points1 = object.getInt("punti");
            Log.v("RISULTATO", String.valueOf(points1));*/
    }
}