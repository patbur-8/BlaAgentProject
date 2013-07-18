package com.ismobile.blaagent;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by ats on 2013-07-18.
 */
public class UserSettingActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.Preferences);

    }
}