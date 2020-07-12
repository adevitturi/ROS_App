package com.github.ros.android.voicecommands;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/** A fragment to display the settings. */
public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Preference locationsPrefs = findPreference(getString(R.string.locations_key));
        locationsPrefs.setOnPreferenceClickListener(this::onLocationsPrefClick);
    }

    public boolean onLocationsPrefClick(Preference preference) {
        ((MainActivity) getActivity()).navToTab(SlidePageAdapter.LOCATIONS_PAGE);
        return true;
    }
}
