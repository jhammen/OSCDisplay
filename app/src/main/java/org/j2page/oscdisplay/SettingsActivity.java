package org.j2page.oscdisplay;


import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.util.Patterns;
import android.widget.Toast;

import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final String KEY_PREF_PORT = "key_port";
    public static final String KEY_PREF_MULTICAST_IP = "key_multicast_ip";
    public static final String KEY_PREF_MULTICAST_SWITCH = "key_multicast_switch";
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || NetworkPreferenceFragment.class.getName().equals(fragmentName)
                || AddressesPreferenceFragment.class.getName().equals(fragmentName)
                || TemplatesPreferenceFragment.class.getName().equals(fragmentName);
    }

    @NonNull
    private static String getPrefValue(Preference pref) {
        return getDefaultSharedPreferences(pref.getContext()).getString(pref.getKey(), "");
    }

    public static class NetworkPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_network);
            // validations
            EditTextPreference portPref = (EditTextPreference) findPreference(KEY_PREF_PORT);
            portPref.setSummary(getPrefValue(portPref));
            portPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final int port = Integer.parseInt((String) newValue);
                    if (port < 65535) {
                        preference.setSummary(Integer.toString(port));
                        return true;
                    }
                    Toast.makeText(preference.getContext(), "Port must be a number below 65535", Toast.LENGTH_LONG).show();
                    return false;
                }
            });

            SwitchPreference multiPref = (SwitchPreference) findPreference(KEY_PREF_MULTICAST_SWITCH);
            boolean multi = PreferenceManager.getDefaultSharedPreferences(multiPref.getContext()).getBoolean(multiPref.getKey(), false);

            final EditTextPreference ipPref = (EditTextPreference) findPreference(KEY_PREF_MULTICAST_IP);
            ipPref.setEnabled(multi);
            ipPref.setSummary(getPrefValue(ipPref));
            ipPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final String value = (String) newValue;
                    if (Patterns.IP_ADDRESS.matcher(value).matches()) {
                        preference.setSummary(value);
                        return true;
                    }
                    return false;
                }
            });

            multiPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ipPref.setEnabled((Boolean) newValue);
                    return true;
                }
            });

        }
    }

    public static class AddressesPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_addresses);
        }
    }

    public static class TemplatesPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_templates);
        }
    }
}
