package ru.ayurmar.arduinocontrol;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;

import com.takisoft.fix.support.v7.preference.PreferenceCategory;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

public class PreferencesActivity extends AppCompatActivity {

    public static final String KEY_PREF_PHONE_NUMBER = "pref_phone_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = findViewById(R.id.preferences_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.ui_menu_settings_text);

        boolean isDevMode = getIntent().getBooleanExtra(MainActivity.DEV_MODE, false);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.preferences_activity_fragment_container,
                        PreferencesFragment.newInstance(isDevMode))
                .commit();
    }

    public static class PreferencesFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {

        private static final String sIsDevMode = "IS_DEV_MODE_PREF_FRAGMENT";

        public static PreferencesFragment newInstance(boolean isDevMode){
            Bundle args = new Bundle();
            args.putBoolean(sIsDevMode, isDevMode);
            PreferencesFragment fragment = new PreferencesFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            boolean isDevMode = getArguments().getBoolean(sIsDevMode, false);

            SharedPreferences sharedPref = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

            Preference pref = findPreference(KEY_PREF_PHONE_NUMBER);
            pref.setSummary(sharedPref.getString(KEY_PREF_PHONE_NUMBER,
                    getString(R.string.ui_pref_phone_number_default_value)));
            pref.setOnPreferenceChangeListener(this);

            if(!isDevMode){
                //hide "Developer settings" category
                PreferenceScreen prefScreen = (PreferenceScreen) findPreference(getString(R.string.preferenceScreen));
                PreferenceCategory devCategory = (PreferenceCategory) findPreference(
                        getString(R.string.devPreferencesCategory));
                prefScreen.removePreference(devCategory);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue){
            preference.setSummary(newValue.toString());
            return true;
        }
    }
}