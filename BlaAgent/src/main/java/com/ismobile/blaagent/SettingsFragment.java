package com.ismobile.blaagent;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by ats on 2013-07-19.
 */
public static class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
