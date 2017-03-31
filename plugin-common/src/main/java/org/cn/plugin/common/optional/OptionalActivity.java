package org.cn.plugin.common.optional;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import org.cn.plugin.common.R;
import org.cn.plugin.common.databinding.ActivityOptionalBinding;

public class OptionalActivity extends AppCompatActivity {
    public static final String TITLE = "optional.title";
    public static final String ACTION_OPTIONAL = "action.optional";

    private ActivityOptionalBinding mBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String title = getIntent().getStringExtra(TITLE);
        if (TextUtils.isEmpty(title)) {
            title = "Settings";
        }

        mBind = DataBindingUtil.setContentView(this, R.layout.activity_optional);
        mBind.setTitle(title);

        setSupportActionBar(mBind.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PreferenceFragment fragment = new PreferFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ACTION_OPTIONAL, getIntent().getStringExtra(ACTION_OPTIONAL));
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.optional_container, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PreferFragment extends PreferenceFragment {

        public String action = "";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Bundle bundle = getArguments();
            if (bundle != null) {
                action = bundle.getString(ACTION_OPTIONAL, "all");
            }

            switch (action) {
                default: {
                    addPreferencesFromResource(R.xml.prefile);
                    break;
                }
            }

            initData();
        }

        private void initData() {
            Preference api = findPreference(OptionalConst.KEY_API_HOST);
            api.setOnPreferenceChangeListener(listener);
            api.setSummary(OptionalManager.getString(OptionalConst.KEY_API_HOST, ""));

            // mqtt server
            Preference mqtt_message_server = findPreference(OptionalConst.KEY_MQTT_SERVER_ADDR);
            mqtt_message_server.setOnPreferenceChangeListener(listener);
            mqtt_message_server.setSummary(OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_ADDR, ""));

            Preference mqtt_message_username = findPreference(OptionalConst.KEY_MQTT_SERVER_USERNAME);
            mqtt_message_username.setOnPreferenceChangeListener(listener);
            mqtt_message_username.setSummary(OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_USERNAME, ""));

            Preference mqtt_message_password = findPreference(OptionalConst.KEY_MQTT_SERVER_PASSWORD);
            mqtt_message_password.setOnPreferenceChangeListener(listener);
            mqtt_message_password.setSummary(OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_PASSWORD, ""));

            findPreference(OptionalConst.KEY_CLEAN_MESSAGE_CACHE).setOnPreferenceClickListener(clickListener);
        }

        private Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(String.valueOf(newValue));
                return true;
            }
        };

        private Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                switch (preference.getKey()) {
                    case OptionalConst.KEY_CLEAN_MESSAGE_CACHE: {
                        break;
                    }
                    default:
                        return false;
                }
                return true;
            }
        };

    }

}
