package ir.khaled.mydictionary;


import android.app.Activity;
import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import ir.khaled.mydictionary.R;


public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);

//        CharSequence[] entries = {"504 essential words"};
//        CharSequence[] entryValues = { "504words"};
//        ListPreference lp = (ListPreference)findPreference(R.);
//        lp.setEntries(entries);
//        lp.setEntryValues(entryValues);
//        lp.setDefaultValue("504words");
    }
}
