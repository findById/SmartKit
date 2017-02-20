package org.cn.iot.smartkit;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cn.iot.device.DeviceListActivity;
import org.cn.iot.smartkit.utils.PermissionUtils;
import org.cn.plugin.airkiss.SmartConfigActivity;
import org.cn.plugin.message.MessageActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends BaseFragment implements View.OnClickListener {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        view.findViewById(R.id.smart_config).setOnClickListener(this);
        view.findViewById(R.id.message).setOnClickListener(this);
        view.findViewById(R.id.device).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.smart_config: {
                PermissionUtils.requestPermissions(getActivity(), new PermissionUtils.OnPermissionsCallback() {
                    @Override
                    public void onRequestPermissionsResult(boolean success, String[] permission, int[] grantResult, boolean[] showRequestRationale) {
                        if (success) {
                            Intent intent = new Intent(getActivity(), SmartConfigActivity.class);
                            startActivity(intent);
                        } else {
                            PermissionUtils.toAppSetting(getActivity());
                        }
                    }
                }, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            }
            case R.id.message: {
                Intent intent = new Intent(getContext(), MessageActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.device: {
                Intent intent = new Intent(getContext(), DeviceListActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }

}
