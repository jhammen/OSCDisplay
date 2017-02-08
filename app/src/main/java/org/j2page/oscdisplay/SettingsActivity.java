package org.j2page.oscdisplay;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
import android.util.Patterns;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

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

        private static final String KEY_PREF_TEMPLATE_PRELOAD = "key_template_preload";
        private static final String KEY_PREF_TEMPLATE_PREDEF = "key_template_predef";
        private static final String KEY_PREF_TEMPLATE_COUNT = "key_template_count";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Activity activity = getActivity();
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(activity);

            final SharedPreferences prefs = getDefaultSharedPreferences(activity);
            final boolean preloaded = prefs.getBoolean(KEY_PREF_TEMPLATE_PRELOAD, false);
            if (!preloaded) {
                loadDefaultTemplates(prefs);
            }

            Resources resources = this.getResources();
            @SuppressWarnings("ResourceType")
            XmlPullParser parser = resources.getXml(R.layout.preference_template);
            AttributeSet attributes = Xml.asAttributeSet(parser);

            TemplatePreference tpref = new TemplatePreference(activity, attributes);
            tpref.setTitle("User Template 1");
            tpref.setSummary("click to edit");
            tpref.setKey(KEY_PREF_TEMPLATE_PREDEF);
            screen.addPreference(tpref);

            Preference addPref = new Preference(activity);
            addPref.setTitle("Add New Template");
            addPref.setIcon(android.R.drawable.ic_menu_add);
            screen.addPreference(addPref);
            setPreferenceScreen(screen);
        }

        private void loadDefaultTemplates(SharedPreferences prefs) {
            prefs.edit().putString(KEY_PREF_TEMPLATE_PREDEF, getText(R.string.default_template).toString())
                    .putInt(KEY_PREF_TEMPLATE_COUNT, 1)
                    .putBoolean(KEY_PREF_TEMPLATE_PRELOAD, true).apply();
        }
    }
}
