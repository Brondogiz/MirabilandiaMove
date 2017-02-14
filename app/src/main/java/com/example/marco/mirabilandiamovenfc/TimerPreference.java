package com.example.marco.mirabilandiamovenfc;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by erik_ on 14/02/2017.
 */

public class TimerPreference {
    private static final String PREF_NAME = "TimerPreference";
    private static final String PREF_KEY = "TimerPreference_Key";

    public TimerPreference() {
        super();
    }

    public void saveTime(Context context, long millis) {
        SharedPreferences timerPreference;
        SharedPreferences.Editor editor;
        timerPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = timerPreference.edit();

        editor.putLong(PREF_KEY, millis);
        editor.commit();
    }

    public long getTime(Context context) {
        SharedPreferences timerPreference;
        long result;
        timerPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        result = timerPreference.getLong(PREF_KEY, 0);

        return result;
    }

}
