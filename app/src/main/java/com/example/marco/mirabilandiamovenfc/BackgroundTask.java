package com.example.marco.mirabilandiamovenfc;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

public class BackgroundTask extends AsyncTask<String,Void,String> {

    Context ctx;
    BackgroundTask(Context ctx){
        this.ctx=ctx;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String connect_url="http://localhost:8080/connection_database_android.php";



        String  ciao;

       // if(method.equals("register")){
            /*
            String name=params[1];
            String user_surname=params[2];
            String user_name=params[3];
            String user_pass=params[4];
            String user_mail=params[5];
            */
            try {
                URL url=new URL(connect_url);
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                InputStream IS=httpURLConnection.getInputStream();
                BufferedReader bufferedReader=new BufferedReader((new InputStreamReader(IS,"UTF-8")));

                StringBuilder stringBuilder = new StringBuilder();
                while( (ciao = bufferedReader.readLine()) !=null){

                    stringBuilder.append(ciao);
                    Log.v("Sono nel while",ciao);
                }

                Log.v("Sono fuori dal while",ciao);
                /*
                String data= URLEncoder.encode("user", "UTF-8") + "="+URLEncoder.encode(name,"UTF-8")+"&"+
                        URLEncoder.encode("user_surname","UTF-8") + "="+URLEncoder.encode(user_surname,"UTF-8")+"&"+
                        URLEncoder.encode("user_name","UTF-8") + "="+URLEncoder.encode(user_name,"UTF-8")+"&"+
                        URLEncoder.encode("user_pass","UTF-8") + "="+URLEncoder.encode(user_pass,"UTF-8")+"&"+
                        URLEncoder.encode("user_email","UTF-8") + "="+URLEncoder.encode(user_mail,"UTF-8");
                        */
                bufferedReader.close();
                IS.close();

                httpURLConnection.disconnect();

                return stringBuilder.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
       // }

        return null;
    }



    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
    }
}