package com.ismobile.blaagent.menuSettings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ismobile.blaagent.R;

/**
 * Created by pbm on 2013-08-02.
 */
public class TestFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the location_preferences from an XML resource
        addPreferencesFromResource(R.xml.test_preferences);
    }
}
