package com.toraleap.lyrics;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class LyricsPreferenceActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
	}
}
