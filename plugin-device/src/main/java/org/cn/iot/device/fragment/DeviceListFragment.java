package org.cn.iot.device.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.cn.iot.device.DeviceConst;
import org.cn.iot.device.R;
import org.cn.iot.device.adapter.DeviceListAdapter;
import org.cn.iot.device.databinding.FragmentDeviceListBinding;
import org.cn.iot.device.internal.BaseFragment;
import org.cn.iot.device.model.Device;
import org.cn.iot.device.widget.DeviceListDivider;
import org.cn.plugin.rpc.Response;
import org.cn.plugin.rpc.ResponseListener;
import org.cn.plugin.rpc.RpcEngine;

/**
 * Created by chenning on 2016/8/24.
 *
 * @Description 设备列表
 */
public class DeviceListFragment extends BaseFragment {
    public static final String DEVICE_ID = "device.id";

    private String deviceId;

    private FragmentDeviceListBinding mDeviceListBinding;

    private DeviceListAdapter mDeviceListAdapter;

    private boolean isRefresh = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            deviceId = savedInstanceState.getString(DEVICE_ID, "");
        }
        if (!TextUtils.isEmpty(deviceId) && getArguments() != null) {
            deviceId = getArguments().getString(DEVICE_ID, "");
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter("UPDATE"));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(DEVICE_ID, deviceId);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDeviceListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_device_list, container, false);
        initView();
        return mDeviceListBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    private void initView() {
        showLoading(null, "Loading...", true);
        mDeviceListBinding.refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mDeviceListBinding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                requestData();
            }
        });

        mDeviceListAdapter = new DeviceListAdapter(getActivity());
        mDeviceListBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDeviceListBinding.recyclerView.addItemDecoration(new DeviceListDivider(getActivity()));
        mDeviceListBinding.recyclerView.setAdapter(mDeviceListAdapter);
    }

    private void initData() {
        requestData();
    }

    private void requestData() {
        JSONObject biz = new JSONObject();
        biz.put("deviceId", "ESP8266");

        JSONObject param = new JSONObject();
        param.put("method", "iot.device.find");
        param.put("content", biz.toJSONString());
        RpcEngine.post(DeviceConst.API_HOST + "/getaway", param.toJSONString(), new ResponseListener<Response>() {
            @Override
            public void onResponse(Response response) {
                try {
                    if (!response.isSuccess()) {
                        return;
                    }
                    JSONObject obj = JSON.parseObject(response.result).getJSONObject("result");
                    int statusCode = obj.getInteger("statusCode");
                    if (statusCode != 200) {
                        return;
                    }
                    Device device = JSON.parseObject(obj.getJSONObject("result").toJSONString(), Device.class);
                    if (device == null || device.getItems() == null) {
                        return;
                    }

                    if (isRefresh) {
                        isRefresh = false;
                        mDeviceListAdapter.updateData(device.getItems());
                    } else {
                        mDeviceListAdapter.addAll(device.getItems());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    hideLoading();
                    if (mDeviceListBinding.refreshLayout.isRefreshing()) {
                        mDeviceListBinding.refreshLayout.setRefreshing(false);
                    }
                }
            }
        });
    }

    @Override
    public void onDetach() {
        hideLoading();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onDetach();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("DLF", "=====================");
            requestData();
        }
    };

}
