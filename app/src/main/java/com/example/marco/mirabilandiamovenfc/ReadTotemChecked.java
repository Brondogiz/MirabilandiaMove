package com.example.marco.mirabilandiamovenfc;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by erik_ on 16/03/2017.
 */

public class ReadTotemChecked extends AsyncTask<String, Void, String> {
    private Context context;
    private int codeId;
    private int currentTotemId;
    private String currentTotemType;

    public ReadTotemChecked(Context context, int currentTotemId, String currentTotemType) {
        this.context = context;
        this.currentTotemId = currentTotemId;
        this.currentTotemType = currentTotemType;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(String... params) {
        String connect_url = "http://192.168.1.7/read_totem_checked.php";
        codeId = Integer.parseInt(params[0]);
        try {
            URL url = new URL(connect_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream OS = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter((new OutputStreamWriter(OS, "UTF-8")));
            String data = URLEncoder.encode("codeId", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(codeId), "UTF-8");
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
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
        Calendar currentTime = Calendar.getInstance();
        try {
            JSONObject jsonObject = new JSONObject(String.valueOf(result));
            JSONArray jsonArray = jsonObject.getJSONArray("last_totem_checked");

                int lastTotemId = jsonArray.getJSONObject(0).getInt("totemId");
                SimpleDateFormat activationDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date activationDate = activationDateFormat.parse(jsonArray.getJSONObject(0).getString("activationDate"));
                String lastTotemType = jsonArray.getJSONObject(0).getString("type");
                if (currentTotemId == lastTotemId + 1 && lastTotemType.equals("Totem inizio fila") && currentTotemType.equals("Totem fine fila")) {
                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds(currentTime.getTime().getTime() - activationDate.getTime());
                    BackgroundTask backgroundTask = new BackgroundTask(context);
                    if (diffInSec <= 600) {
                        backgroundTask.execute(String.valueOf(codeId), String.valueOf(Utilities.MIN_POINT_OF_TOTEM_QUEUE));
                    } else if (diffInSec > 600 && diffInSec <= 1800) {
                        backgroundTask.execute(String.valueOf(codeId), String.valueOf(Utilities.AVERAGE_LOW_POINT_OF_TOTEM_QUEUE));
                    } else if (diffInSec > 1800 && diffInSec <= 5400) {
                        backgroundTask.execute(String.valueOf(codeId), String.valueOf(Utilities.AVERAGE_HIGH_POINT_OF_TOTEM_QUEUE));
                    } else {
                        backgroundTask.execute(String.valueOf(codeId), String.valueOf(Utilities.HIGH_POINT_OF_TOTEM_QUEUE));
                    }
                    //new DeleteTotemCheckedTask().execute(codeId, lastTotemId);
                } else if (lastTotemType.equals("Totem inizio fila") && currentTotemType.equals("Totem inizio fila")) {
                    new DeleteTotemCheckedTask().execute(codeId, lastTotemId);
                } else if (lastTotemType.equals("Totem inizio fila") && currentTotemType.equals("Totem standard") || lastTotemType.equals("Totem inizio fila") && currentTotemType.equals("Totem venditore")) {
                    new DeleteTotemCheckedTask().execute(codeId, lastTotemId);
                }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}
