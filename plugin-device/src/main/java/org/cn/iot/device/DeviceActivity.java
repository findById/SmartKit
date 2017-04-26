package org.cn.iot.device;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.cn.iot.device.databinding.ActivityDeviceBinding;
import org.cn.iot.device.internal.BaseActivity;
import org.cn.iot.device.model.Device;
import org.cn.iot.device.model.DeviceType;
import org.cn.plugin.rpc.Response;
import org.cn.plugin.rpc.ResponseListener;
import org.cn.plugin.rpc.RpcEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenning on 2016/8/25.
 *
 * @Description 设备管理
 */
public class DeviceActivity extends BaseActivity {
    public static final String ACTION_DEVICE_OPERATE = "action.device.operate";
    public static final String EXTRA_DEVICE_ID = "extra.device.id";

    public static final String ACTION_DEVICE_ADD = "action.device.add";
    public static final String ACTION_DEVICE_EDIT = "action.device.edit";

    private ActivityDeviceBinding mBinding;
    private String action = ACTION_DEVICE_ADD;
    private String deviceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_device);

        action = getIntent().getStringExtra(ACTION_DEVICE_OPERATE);
        deviceId = getIntent().getStringExtra(EXTRA_DEVICE_ID);

        initView();
        initData();
    }

    private void initView() {
        switch (action) {
            case ACTION_DEVICE_ADD: {
                mBinding.setTitle("Add Device");
                initSaveData();
                break;
            }
            case ACTION_DEVICE_EDIT: {
                mBinding.setTitle("Edit Device");
                initUpdateData();
                break;
            }
            default:
                break;
        }

        mBinding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String method;
                switch (action) {
                    case ACTION_DEVICE_ADD: {
                        method = "iot.device.save";
                        break;
                    }
                    case ACTION_DEVICE_EDIT: {
                        method = "iot.device.update";
                        break;
                    }
                    default:
                        return;
                }
                String deviceId = mBinding.deviceId.getText().toString();
                if (TextUtils.isEmpty(deviceId)) {
                    Toast.makeText(DeviceActivity.this, "请输入设备ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = mBinding.name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(DeviceActivity.this, "请输入设备名", Toast.LENGTH_SHORT).show();
                    return;
                }
                String type = ((DeviceType)mBinding.type.getSelectedItem()).getKey();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(DeviceActivity.this, "请选择设备类型", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject biz = new JSONObject();
                biz.put("deviceId", deviceId);
                biz.put("name", name);
                biz.put("type", type);
                biz.put("description", mBinding.description.getText().toString());
                biz.put("parentId", ((DeviceType)mBinding.parentId.getSelectedItem()).getKey());

                JSONObject param = new JSONObject();
                param.put("method", method);
                param.put("content", biz.toJSONString());
                RpcEngine.post(DeviceConst.API_HOST + "/getaway", param.toString(), new ResponseListener<Response>() {
                    @Override
                    public void onResponse(Response response) {
                        try {
                            if (!response.isSuccess()) {
                                return;
                            }
                            JSONObject obj = JSON.parseObject(response.result);
                            int statusCode = obj.getIntValue("statusCode");
                            if (statusCode != 200) {
                                Toast.makeText(DeviceActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            switch (action) {
                                case ACTION_DEVICE_ADD: {
                                    Device device = JSON.parseObject(obj.getJSONObject("result").toJSONString(), Device.class);
                                    Intent intent = new Intent(DeviceActivity.this, DeviceActivity.class);
                                    intent.putExtra(ACTION_DEVICE_OPERATE, "Edit");
                                    intent.putExtra(EXTRA_DEVICE_ID, device.getId());
                                    startActivity(intent);
                                    finish();
                                    break;
                                }
                                case ACTION_DEVICE_EDIT: {
                                    finish();
                                    break;
                                }
                                default:
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
        List<DeviceType> types = new ArrayList<>();
        types.add(new DeviceType("relay", "主动开关"));
        types.add(new DeviceType("switch", "被动开关"));
        types.add(new DeviceType("TemperatureHumidity", "温湿度"));
        types.add(new DeviceType("led", "LED"));

        ArrayAdapter<DeviceType> typesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typesAdapter.setDropDownViewResource(android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item);

        mBinding.type.setAdapter(typesAdapter);

        List<DeviceType> parent = new ArrayList<>();
        parent.add(new DeviceType("ESP8266", "ESP8266"));

        ArrayAdapter<DeviceType> parentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, parent);
        parentAdapter.setDropDownViewResource(android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item);

        mBinding.parentId.setAdapter(parentAdapter);
    }

    private void initSaveData() {
    }

    private void initUpdateData() {
        RpcEngine.get(DeviceConst.API_HOST + "/iot/device/find?id=" + deviceId, new ResponseListener<Response>() {
            @Override
            public void onResponse(Response response) {
                if (!response.isSuccess()) {
                    return;
                }
                JSONObject obj = JSON.parseObject(response.result);
                int statusCode = obj.getIntValue("statusCode");
                if (statusCode != 200) {
                    Toast.makeText(DeviceActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                    return;
                }
                Device device = JSON.parseObject(obj.getJSONObject("result").toJSONString(), Device.class);
                mBinding.deviceId.setText(device.getDeviceId());
                mBinding.name.setText(device.getName());
                mBinding.description.setText(device.getDescription());
                switch (device.getType()) {
                    case "relay": {
                        mBinding.type.setSelection(0);
                        break;
                    }
                    case "switch": {
                        mBinding.type.setSelection(1);
                        break;
                    }
                    case "TemperatureHumidity": {
                        mBinding.type.setSelection(2);
                        break;
                    }
                    case "led": {
                        mBinding.type.setSelection(3);
                        break;
                    }
                    default:
                        mBinding.type.setSelection(0);
                        break;
                }
            }
        });
    }

}
