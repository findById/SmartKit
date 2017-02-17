package org.cn.iot.smartkit.optional;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.cn.iot.smartkit.R;
import org.cn.iot.smartkit.databinding.ActivityOptionalBinding;

public class OptionalActivity extends AppCompatActivity {
    public static final String ACTION_OPTIONAL = "action.optional";

    private ActivityOptionalBinding mBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBind = DataBindingUtil.setContentView(this, R.layout.activity_optional);
        mBind.setTitle(getString(R.string.action_settings));

        PreferenceFragment fragment = new PreferFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ACTION_OPTIONAL, getIntent().getStringExtra(ACTION_OPTIONAL));
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.optional_container, fragment).commit();
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
            Preference api = findPreference("pre_api_host");
            api.setOnPreferenceChangeListener(listener);
            api.setSummary(OptionalManager.getString("pre_api_host", "http://127.0.0.1:8080"));

            // mqtt server
            Preference mqtt_message_server = findPreference("mqtt_message_server");
            mqtt_message_server.setOnPreferenceChangeListener(listener);
            mqtt_message_server.setSummary(OptionalManager.getString("mqtt_message_server", "tcp://192.168.99.111:61613"));

            Preference mqtt_message_username = findPreference("mqtt_message_username");
            mqtt_message_username.setOnPreferenceChangeListener(listener);
            mqtt_message_username.setSummary(OptionalManager.getString("mqtt_message_username", "admin"));

            Preference mqtt_message_password = findPreference("mqtt_message_password");
            mqtt_message_password.setOnPreferenceChangeListener(listener);
            mqtt_message_password.setSummary(OptionalManager.getString("mqtt_message_password", "password"));
        }

        private Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(String.valueOf(newValue));
                return true;
            }
        };

    }

}
