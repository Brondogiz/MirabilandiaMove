package com.example.marco.mirabilandiamovenfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    public final String TAG = "DemoNFC";
    public static final String MIME_TEXT_PLAIN = "text/plain";

    private NfcAdapter nfcAdapter;
    TextView textMessageNfc;
    TextView txtCounter;
    Map<Integer, Long> timers = new HashMap<>();
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textMessageNfc = (TextView) findViewById(R.id.textMessageNfc);
        txtCounter = (TextView) findViewById(R.id.textCounter);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {

            Toast.makeText(this, "Il dispoditivo non supporta nfc", Toast.LENGTH_LONG);
            finish();
            return;
        }

        if (nfcAdapter.isEnabled()) {

            textMessageNfc.setText("NFC È ATTIVO");
        } else {

            textMessageNfc.setText("NFC NON È ATTIVO");

        }


        handleIntent(getIntent());


        /*long diffInMs = endDate.getTime() - startDate.getTime();

        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);*/
        /*new CountDownTimer(1800000, 1000) {

            public void onTick(long millisUntilFinished) {
                txtCounter.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                txtCounter.setText("done!");
            }
        }.start();*/

        /*class MyTimerTask extends TimerTask {

            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                calendar.set
                SimpleDateFormat simpleDateFormat =
                        new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
                final String strDate = simpleDateFormat.format(calendar.getTime());

                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        txtCounter.setText(strDate);
                    }});
            }

        }

        Timer timer = new Timer();
        MyTimerTask  myTimerTask = new MyTimerTask();
        timer.schedule(myTimerTask, 1000, 1000);*/
    }


    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, nfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, nfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }


    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Mine Type error.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }


    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;
            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"
            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                textMessageNfc.setText("CodeID: " + result);
                BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this);
                backgroundTask.execute(result, String.valueOf(Utilities.POINT_OF_TOTEM));
                Calendar calendar = Calendar.getInstance();

                if (timers.containsKey(i-1)) {
                    Iterator it = timers.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry entry = (Map.Entry) it.next();
                        if (entry.getKey().equals(0)) {
                            Calendar calendar1 = Calendar.getInstance();
                            long diffInMs = calendar1.getTime().getTime() - Long.valueOf(String.valueOf(entry.getValue()));

                            Log.v("TEMPO", String.valueOf(entry.getValue()));
                            Log.v("TEMPO2", String.valueOf(calendar1.getTime().getTime()));
                            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
                            //Log.v("Provaaaa", String.valueOf(System.currentTimeMillis()));
                            //Log.v("VALORE", String.valueOf(Long.valueOf(String.valueOf(entry.getValue()))));
                            //Long test = Long.valueOf(String.valueOf(entry.getValue()))-System.currentTimeMillis();
                            Log.v("Provaaaa", String.valueOf(diffInSec));

                        }

                    }
                } else {
                    timers.put(i, calendar.getTime().getTime());
                    i++;
                }


                Log.v("TEMPOTOTALE", String.valueOf(timers));

                Log.v("numero", String.valueOf(i));


            }
        }
    }

}
