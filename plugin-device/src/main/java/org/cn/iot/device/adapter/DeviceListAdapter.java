package org.cn.iot.device.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.alibaba.fastjson.JSONObject;

import org.cn.iot.device.DeviceActivity;
import org.cn.iot.device.DeviceConst;
import org.cn.iot.device.R;
import org.cn.iot.device.databinding.ListDeviceItemBinding;
import org.cn.iot.device.model.Device;
import org.cn.plugin.message.service.MessageService;
import org.cn.plugin.rpc.Response;
import org.cn.plugin.rpc.ResponseListener;
import org.cn.plugin.rpc.RpcEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenning on 2016/8/24.
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    private Context mContext;

    private LayoutInflater mInflater;

    private List<Device> mData = new ArrayList<>();

    public DeviceListAdapter(Context ctx) {
        this.mContext = ctx;
        mInflater = LayoutInflater.from(ctx);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListDeviceItemBinding mBinding = DataBindingUtil.inflate(mInflater, R.layout.list_device_item, parent, false);
        return new ViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mBinding.setDevice(mData.get(position));
        holder.onBindData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void addAll(List<Device> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        mData.addAll(list);
        notifyDataSetChanged();
    }

    public void updateData(List<Device> list) {
        mData.clear();
        if (list != null) {
            mData.addAll(list);
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        ListDeviceItemBinding mBinding;
        Device device;

        public ViewHolder(final ListDeviceItemBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            mBinding.itemLayout.setOnClickListener(this);
            mBinding.btnSwitch.setOnCheckedChangeListener(this);
        }

        public void onBindData(Device device) {
            this.device = device;

            switch (device.getType()) {
                case "relay": {
                    mBinding.btnSwitch.setVisibility(View.VISIBLE);
                    mBinding.btnSwitch.setChecked("opened".equals(device.getRelay()));
                    break;
                }
                case "data": {
                    mBinding.btnSwitch.setVisibility(View.GONE);

                    break;
                }
                default:
                    mBinding.btnSwitch.setVisibility(View.GONE);
                    break;
            }

        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), DeviceActivity.class);
            intent.putExtra(DeviceActivity.ACTION_DEVICE_OPERATE, DeviceActivity.ACTION_DEVICE_EDIT);
            intent.putExtra(DeviceActivity.EXTRA_DEVICE_ID, device.getId());
            view.getContext().startActivity(intent);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            device.setRelay(isChecked ? "opened" : "closed");

            MessageService.publish(mContext, device.getDeviceId(), isChecked ? "2" : "1");

            JSONObject param = new JSONObject();
            param.put("deviceId", device.getDeviceId());
            param.put("relay", device.getRelay());
            RpcEngine.post(DeviceConst.API_HOST + "/iot/device/update", param.toJSONString(), new ResponseListener<Response>() {
                @Override
                public void onResponse(Response response) {

                }
            });
        }
    }

}
