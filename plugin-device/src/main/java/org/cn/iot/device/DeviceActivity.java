package org.cn.iot.device;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.alibaba.fastjson.JSONObject;

import org.cn.iot.device.databinding.ActivityDeviceBinding;
import org.cn.iot.device.internal.BaseActivity;
import org.cn.plugin.rpc.Response;
import org.cn.plugin.rpc.ResponseListener;
import org.cn.plugin.rpc.RpcEngine;

/**
 * Created by chenning on 2016/8/25.
 *
 * @Description 设备管理
 */
public class DeviceActivity extends BaseActivity {
    public static final String ACTION_DEVICE_OPERATE = "action.device.operate";

    private ActivityDeviceBinding mBinding;
    private String action = "Add";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_device);

        action = getIntent().getStringExtra(ACTION_DEVICE_OPERATE);

        initView();
        initData();
    }

    private void initView() {
        switch (action) {
            case "Add": {
                mBinding.setTitle("Add Device");
                break;
            }
            case "Edit": {
                mBinding.setTitle("Edit Device");
                break;
            }
            default:
                break;
        }

        mBinding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = "";
                switch (action) {
                    case "Add": {
                        path = "/iot/device/save";
                        break;
                    }
                    case "Edit": {
                        path = "/iot/device/update";
                        break;
                    }
                    default:
                        return;
                }

                JSONObject param = new JSONObject();
                param.put("name", mBinding.deviceName);
                param.put("description", mBinding.deviceDescription);
                param.put("openId", mBinding.deviceOpenId);
                param.put("type", mBinding.deviceType);
                RpcEngine.post(DeviceConst.API_HOST + path, param.toString(), new ResponseListener<Response>() {
                    @Override
                    public void onResponse(Response response) {
                        try {
                            if (!response.isSuccess()) {
                                return;
                            }

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void initData() {
    }
}
