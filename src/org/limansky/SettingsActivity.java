package org.limansky;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d("Settings", "Shit happens");
		addPreferencesFromResource(R.xml.settings);
	}
}
