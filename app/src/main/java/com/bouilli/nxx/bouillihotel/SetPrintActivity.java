package com.bouilli.nxx.bouillihotel;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;

import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.util.List;

public class SetPrintActivity extends AppCompatPreferenceActivity {
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof RingtonePreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    preference.setSummary(R.string.pref_ringtone_silent);
                    SharedPreferencesTool.addOrUpdate(preference.getContext(), "BouilliSetInfo", "printVolSource", "-");
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        preference.setSummary(null);
                        SharedPreferencesTool.addOrUpdate(preference.getContext(), "BouilliSetInfo", "printVolSource", "");
                    } else {
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                        SharedPreferencesTool.addOrUpdate(preference.getContext(), "BouilliSetInfo", "printVolSource", stringValue);
                    }
                }
            } else if (preference instanceof SwitchPreference) {
                if(stringValue.equals("true")){
                    SharedPreferencesTool.addOrUpdate(preference.getContext(), "BouilliSetInfo", "printUseVol", true);
                }else{
                    SharedPreferencesTool.addOrUpdate(preference.getContext(), "BouilliSetInfo", "printUseVol", false);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindBooleanPreferenceSummaryToValue(Preference preference, Context context) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        boolean printUseVol = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getKey(), false);
        SharedPreferencesTool.addOrUpdate(context, "BouilliSetInfo", "printUseVol", printUseVol);
    }

    private static void bindPreferenceSummaryToValue(Preference preference, Context context) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));

        String printVolSource = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), "");
        SharedPreferencesTool.addOrUpdate(context, "BouilliSetInfo", "printVolSource", printVolSource);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        String userPermission = SharedPreferencesTool.getFromShared(SetPrintActivity.this, "BouilliProInfo", "userPermission");
        if(ComFun.strNull(userPermission)){
            if(Integer.parseInt(userPermission) == 0 || Integer.parseInt(userPermission) == 1){
                loadHeadersFromResource(R.xml.pref_headers_manager, target);
            }else{
                loadHeadersFromResource(R.xml.pref_headers, target);
            }
        }else{
            // 测试号
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if (header.fragment == null) {
            if(header.id == R.id.print_area_set_header){
                Intent printAreaSetIntent = new Intent(SetPrintActivity.this, PrintAreaActivity.class);
                startActivity(printAreaSetIntent);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            SetPrintActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            setHasOptionsMenu(true);

            bindBooleanPreferenceSummaryToValue(findPreference("notifications_new_message"), getActivity());
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"), getActivity());
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

}
