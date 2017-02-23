package com.example.marco.mirabilandiamovenfc;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by erik_ on 22/02/2017.
 */

public class ManagementSharedPreference {
    private static final String PREF_NAME = "TotemPref";
    private static final String PREF_KEY = "TotemPref_Key";
    private static final String PREF_KEY_TYPE = "TotemPref_Key_Type";

    public ManagementSharedPreference save(Context context, int totemId, String type) {
        SharedPreferences managementPreference;
        SharedPreferences.Editor editor;
        managementPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = managementPreference.edit();

        editor.putInt(PREF_KEY, totemId);
        editor.putString(PREF_KEY_TYPE, type);
        editor.commit();
        return null;
    }

    public int getTotemID(Context context) {
        SharedPreferences managementPreference;
        int totemId;
        managementPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        totemId = managementPreference.getInt(PREF_KEY, -1);
        return totemId;
    }

    public String getTotemType(Context context){
        SharedPreferences managementPreference;
        String type;
        managementPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        type = managementPreference.getString(PREF_KEY_TYPE, null);
        return type;
    }

    public void removePreference(Context context) {
        SharedPreferences managementPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        managementPreference.edit().clear().commit();
    }

}
