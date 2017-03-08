package com.example.marco.mirabilandiamovenfc;

import android.os.AsyncTask;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by erik_ on 08/03/2017.
 */

public class AddActiveMissionTask extends AsyncTask<String, Void, Void> {
    int codeID;
    int missionID;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Void doInBackground(String... params) {
        String connect_url = "http://192.168.1.7/add_active_mission.php";
        codeID = Integer.parseInt(params[0]);
        missionID = Integer.parseInt(params[1]);
        try {
            URL url = new URL(connect_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            OutputStream OS = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter((new OutputStreamWriter(OS, "UTF-8")));
            String data = URLEncoder.encode("codeId", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(codeID), "UTF-8")+"&"+
                    URLEncoder.encode("missionId","UTF-8") + "="+URLEncoder.encode(String.valueOf(missionID),"UTF-8");
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            OS.close();
            InputStream IS = httpURLConnection.getInputStream();
            IS.close();

            httpURLConnection.disconnect();

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
}
