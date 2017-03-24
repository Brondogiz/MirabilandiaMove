package com.example.marco.mirabilandiamovenfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    public final String TAG = "DemoNFC";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final int TAG_MESSAGES_SIZE = 3;
    private Tag tag;

    private NfcAdapter nfcAdapter;
    private TextView txtMessageNfc;
    private TextView txtMessageID;
    private Button btnReprogram;
    private Button btnSubmit;
    private RadioGroup radioGroup;
    private Map<Integer, Long> timers;
    public List<Integer> codeList;
    int codeID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtMessageNfc = (TextView) findViewById(R.id.txtMessageNfc);
        txtMessageID = (TextView) findViewById(R.id.txtMessageID);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        btnReprogram = (Button) findViewById(R.id.btnReprogram);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        timers = new HashMap<>();
        codeList = new ArrayList<>();

        if (nfcAdapter == null) {

            Toast.makeText(this, "Il dispoditivo non supporta nfc", Toast.LENGTH_LONG);
            finish();
            return;
        }

        if (nfcAdapter.isEnabled()) {

            txtMessageNfc.setText("NFC È ATTIVO");
        } else {

            txtMessageNfc.setText("NFC NON È ATTIVO");

        }

        setVisibility();
        new ReadIDTask(codeList).execute();
        handleIntent(getIntent());
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

                tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
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
                        e.printStackTrace();
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

        /*private NdefRecord createRecord(String text) throws UnsupportedEncodingException {

            //create the message in according with the standard
            String lang = "en";
            byte[] textBytes = text.getBytes();
            byte[] langBytes = lang.getBytes("US-ASCII");
            int langLength = langBytes.length;
            int textLength = textBytes.length;

            byte[] payload = new byte[1 + langLength + textLength];
            payload[0] = (byte) langLength;

            // copy langbytes and textbytes into payload
            System.arraycopy(langBytes, 0, payload, 1, langLength);
            System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

            NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
            return recordNFC;
        }

        //Metodo che permette di scrivere un tag
        private void write(String userId, String startTime, String startTotemID, Tag tag) throws IOException, FormatException {
            NdefRecord[] records;
            if (startTime == null || startTotemID == null) {
                records = new NdefRecord[]{createRecord(userId)};

            } else {
                records = new NdefRecord[]{createRecord(userId), createRecord(startTime), createRecord(startTotemID)};
            }
            NdefMessage message = new NdefMessage(records);
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            ndef.writeNdefMessage(message);
            ndef.close();
        }*/

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                txtMessageNfc.setText("CodeID: " + result);
                codeID = Integer.parseInt(result);
                Calendar currentTime = Calendar.getInstance();
                ManagementSharedPreference managementSharedPreference = new ManagementSharedPreference();
                if (!codeList.contains(Integer.parseInt(String.valueOf(codeID)))) {
                    new AddUserTask().execute(String.valueOf(Integer.parseInt(String.valueOf(codeID))));
                    codeList.add(Integer.parseInt(String.valueOf(codeID)));
                }
                new ReadMissionsTask(codeID).execute();
                if (managementSharedPreference.getTotemType(MainActivity.this) != null) {
                    final int totemId = managementSharedPreference.getTotemID(MainActivity.this);
                    String totemType = managementSharedPreference.getTotemType(MainActivity.this);
                    new ManagementMissionsTask().execute(String.valueOf(codeID), String.valueOf(totemId), totemType);
                    //Gestione totem
                    switch (totemType) {
                        case "Totem standard":
                         /*   if (result[1] != null) {
                                try {
                                    write(result[0], null, null, tag);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (FormatException e) {
                                    e.printStackTrace();
                                }
                            }*/
                            new ReadTotemChecked(getApplicationContext(), totemId, totemType).execute(String.valueOf(codeID));
                            //Parte il task per inserire su database il totem attivato con la relativa data

                            if (!timers.containsKey(codeID)) {
                                timers.put(Integer.valueOf(codeID), currentTime.getTime().getTime());
                                BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this);
                                backgroundTask.execute(String.valueOf(codeID), String.valueOf(Utilities.POINT_OF_STANDARD_TOTEM));
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        new SetTotemCheckedTask().execute(String.valueOf(codeID), String.valueOf(totemId));
                                    }
                                }, 500);

                            } else {
                                for (Map.Entry<Integer, Long> entry : timers.entrySet()) {
                                    if (Integer.parseInt(String.valueOf(entry.getKey())) == codeID) {
                                        long diffInMs = currentTime.getTime().getTime() - Long.valueOf(String.valueOf(entry.getValue()));
                                        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
                                        if (diffInSec > 20) {
                                            timers.remove(entry.getKey());
                                            timers.put(Integer.valueOf(codeID), currentTime.getTime().getTime());
                                            BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this);
                                            backgroundTask.execute(String.valueOf(codeID), String.valueOf(Utilities.POINT_OF_STANDARD_TOTEM));
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new SetTotemCheckedTask().execute(String.valueOf(codeID), String.valueOf(totemId));
                                                }
                                            }, 500);
                                        }
                                        break;
                                    }
                                }

                            }
                            break;
                        case "Totem inizio fila":
                            //CANCELLARE PRIMA EVENTUALI RECORD DI TOTEM DI INIZIO FILA DALLA TABELLA TOTEM CHECKED
                           // new DeleteTotemCheckedTask().execute(codeID, managementSharedPreference.getTotemID(MainActivity.this) - 1);
                            new ReadTotemChecked(getApplicationContext(), totemId, totemType).execute(String.valueOf(codeID));
                            //Parte il task per inserire su database il totem attivato con la relativa data
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    new SetTotemCheckedTask().execute(String.valueOf(codeID), String.valueOf(totemId));
                                }
                            }, 500);


                          /*  try {
                               write(result[0], String.valueOf(currentTime.getTime().getTime()), String.valueOf(managementSharedPreference.getTotemID(MainActivity.this)), tag);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (FormatException e) {
                                e.printStackTrace();
                            }*/
                            break;
                        case "Totem fine fila":
                            new ReadTotemChecked(getApplicationContext(), totemId, totemType).execute(String.valueOf(codeID));
                            //Log.v("RISULTATO", String.valueOf(currentTime.getTime().getTime()));
                           /* if (result[1] != null && Integer.parseInt(result[2]) + 1 == managementSharedPreference.getTotemID(MainActivity.this)) {
                                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(currentTime.getTime().getTime() - Long.valueOf(result[1]));
                                BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this);
                                if (diffInSec <= 600) {
                                    backgroundTask.execute(result[0], String.valueOf(Utilities.MIN_POINT_OF_TOTEM_QUEUE));
                                } else if (diffInSec > 600 && diffInSec <= 1800) {
                                    backgroundTask.execute(result[0], String.valueOf(Utilities.AVERAGE_LOW_POINT_OF_TOTEM_QUEUE));
                                } else if (diffInSec > 1800 && diffInSec <= 5400) {
                                    backgroundTask.execute(result[0], String.valueOf(Utilities.AVERAGE_HIGH_POINT_OF_TOTEM_QUEUE));
                                } else {
                                    backgroundTask.execute(result[0], String.valueOf(Utilities.HIGH_POINT_OF_TOTEM_QUEUE));
                                }
                                try {
                                    write(result[0], null, null, tag);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (FormatException e) {
                                    e.printStackTrace();
                                }
                            }*/
                            break;
                        case "Totem venditore":
                            new ReadTotemChecked(getApplicationContext(), totemId, totemType).execute(String.valueOf(codeID));
                            //Parte il task per inserire su database il totem attivato con la relativa data
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    new SetTotemCheckedTask().execute(String.valueOf(codeID), String.valueOf(totemId));
                                }
                            }, 500);
                            new BackgroundTask(MainActivity.this).execute(result, String.valueOf(Utilities.POINT_OF_SELLER_TOTEM));
                            break;
                    }
                }
            }
        }
    }

    public void onSubmitClicked(View view) {
        int selectId = radioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = (RadioButton) findViewById(selectId);
        if (selectId == -1) {
            Toast.makeText(getBaseContext(), "Selezionare la tipologia del totem", Toast.LENGTH_SHORT).show();
        } else {
            if (radioButton.getText().equals("Totem inizio fila") || radioButton.getText().equals("Totem fine fila")) {
                new AlertDialog.Builder(this)
                        .setTitle("Totem in fila")
                        .setMessage("Se si programma un totem di inizio fila, il totem successivo da programmare DEVE essere un totem di fine fila. " +
                                "Non è possibile programmare un totem di fine fila prima di un totem di inizio fila. Procedere con l'operazione?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new SetTotemTask(MainActivity.this).execute(String.valueOf(radioButton.getText()));
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                new SetTotemTask(this).execute(String.valueOf(radioButton.getText()));
            }
        }

    }

    public void onReprogramClicked(View view) {
        ManagementSharedPreference managementSharedPreference = new ManagementSharedPreference();
        new ReprogrammingTask().execute(managementSharedPreference.getTotemID(this));
        managementSharedPreference.removePreference(this);
        Toast.makeText(this, "Totem resettato correttamente", Toast.LENGTH_SHORT).show();
        setVisibility();
    }

    public void setVisibility() {
        if (new ManagementSharedPreference().getTotemID(this) == -1) {
            btnReprogram.setVisibility(View.GONE);
            txtMessageID.setVisibility(View.GONE);
            radioGroup.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
        } else {
            txtMessageID.setVisibility(View.VISIBLE);
            txtMessageID.setText("L'id del totem è: " + new ManagementSharedPreference().getTotemID(this));
            radioGroup.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.INVISIBLE);
            btnReprogram.setVisibility(View.VISIBLE);

        }
    }

}
