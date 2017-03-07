package org.cn.iot.smartkit.optional;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.cn.iot.smartkit.R;
import org.cn.iot.smartkit.databinding.ActivityOptionalBinding;
import org.cn.plugin.message.utils.OrmHelper;

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
            Preference api = findPreference(OptionalConst.KEY_API_HOST);
            api.setOnPreferenceChangeListener(listener);
            api.setSummary(OptionalManager.getString(OptionalConst.KEY_API_HOST, "http://127.0.0.1:8080"));

            // mqtt server
            Preference mqtt_message_server = findPreference(OptionalConst.KEY_MQTT_SERVER_ADDR);
            mqtt_message_server.setOnPreferenceChangeListener(listener);
            mqtt_message_server.setSummary(OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_ADDR, "tcp://192.168.99.111:61613"));

            Preference mqtt_message_username = findPreference(OptionalConst.KEY_MQTT_SERVER_USERNAME);
            mqtt_message_username.setOnPreferenceChangeListener(listener);
            mqtt_message_username.setSummary(OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_USERNAME, "admin"));

            Preference mqtt_message_password = findPreference(OptionalConst.KEY_MQTT_SERVER_PASSWORD);
            mqtt_message_password.setOnPreferenceChangeListener(listener);
            mqtt_message_password.setSummary(OptionalManager.getString(OptionalConst.KEY_MQTT_SERVER_PASSWORD, "password"));

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
                        try {
                            OrmHelper.getInstance().getWritableDatabase().execSQL("DELETE FROM iot_message");
                        } catch (Throwable e) {
                        }
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
