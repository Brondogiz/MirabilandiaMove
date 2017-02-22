package com.example.marco.mirabilandiamovenfc;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by erik_ on 22/02/2017.
 */

public class ManagementSharedPreference {
    private static final String PREF_NAME="TotemPref";
    private static final String PREF_KEY ="TotemPref_Key";

    public ManagementSharedPreference save(Context context, int totemId) {
        SharedPreferences managementPreference;
        SharedPreferences.Editor editor;
        managementPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = managementPreference.edit();

        editor.putInt(PREF_KEY, totemId);
        editor.commit();
        return null;
    }

    public int getValue(Context context){
        SharedPreferences managementPreference;
        int totemId;
        managementPreference = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        totemId = managementPreference.getInt(PREF_KEY, -1);

        return totemId;
    }

}
