package com.example.marco.mirabilandiamovenfc;

import android.os.AsyncTask;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by erik_ on 07/03/2017.
 */

public class ReadMissionsTask extends AsyncTask<Void, Void, String> {
    List<Integer> missionsId;
    Integer codeId;

    public ReadMissionsTask(Integer codeId) {
        this.codeId = codeId;
        this.missionsId = new ArrayList<>();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String connect_url = "http://192.168.1.7/read_missions.php";

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
        Random random = new Random();
        Set<Integer> missions = new HashSet<>();
        try {
            JSONObject jsonObjectMissionsId= new JSONObject(String.valueOf(result));
            JSONArray jsonArrayMissionsId = jsonObjectMissionsId.getJSONArray("missions_id");
            for (int i = 0; i < jsonArrayMissionsId.length(); i++) {
                JSONObject object = jsonArrayMissionsId.getJSONObject(i);
                missionsId.add(object.getInt("missionId"));
            }
            while (missions.size() < 2) {
                missions.add(random.nextInt(missionsId.size()));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONObject jsonObjectMissions1Id = new JSONObject(String.valueOf(result));
            JSONArray jsonArrayActiveMission = jsonObjectMissions1Id.getJSONArray("active_mission");
            for (int j = 0; j <= jsonArrayActiveMission.length(); j++) {
                if(jsonArrayActiveMission.length() == 0){
                    Log.v("RISULTATO", "Non ha nessuna missione");
                    for (Integer missionId : missions) {
                        new AddActiveMissionTask().execute(codeId, missionsId.get(missionId));
                    }
                } else if(jsonArrayActiveMission.length() == 1 && jsonArrayActiveMission.getJSONObject(j).getInt("numberMission") < 2){
                    Log.v("RISULTATO", "Ha una missione");
                    for (Integer missionId : missions) {
                        new AddActiveMissionTask().execute(codeId, missionsId.get(missionId));
                        //break;
                    }
                    break;

                } else if(jsonArrayActiveMission.length() == 2 || jsonArrayActiveMission.getJSONObject(j).getInt("numberMission") >= 2){
                    Log.v("RISULTATO", "Ha già due missioni");
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
