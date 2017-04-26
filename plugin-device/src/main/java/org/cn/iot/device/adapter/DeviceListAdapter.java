package org.cn.iot.device.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import org.cn.iot.device.DeviceActivity;
import org.cn.iot.device.R;
import org.cn.iot.device.databinding.ListDeviceItemBinding;
import org.cn.iot.device.model.DeviceItem;
import org.cn.plugin.message.service.MessageService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenning on 2016/8/24.
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    private Context mContext;

    private LayoutInflater mInflater;

    private List<DeviceItem> mData = new ArrayList<>();

    public DeviceListAdapter(Context ctx) {
        this.mContext = ctx;
        mInflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getItemViewType(int position) {
        switch (mData.get(position).getType()) {
            case "relay": {
                return 0;
            }
            default:
                return -1;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListDeviceItemBinding mBinding;
        switch (viewType) {
            case 0: { //
                mBinding = DataBindingUtil.inflate(mInflater, R.layout.list_device_item, parent, false);
                break;
            }
            case 1: { //
                mBinding = DataBindingUtil.inflate(mInflater, R.layout.list_device_item, parent, false);
                break;
            }
            default:
                mBinding = DataBindingUtil.inflate(mInflater, R.layout.list_device_item, parent, false);
                break;
        }
        return new ViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.onBindData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void addAll(List<DeviceItem> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        mData.addAll(list);
        notifyDataSetChanged();
    }

    public void updateData(List<DeviceItem> list) {
        mData.clear();
        if (list != null) {
            mData.addAll(list);
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        ListDeviceItemBinding mBinding;
        DeviceItem item;

        public ViewHolder(final ListDeviceItemBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            mBinding.itemLayout.setOnClickListener(this);
        }

        public void onBindData(DeviceItem item) {
            this.item = item;

            switch (item.getType()) {
                case "relay": {
                    mBinding.btnSwitch.setOnCheckedChangeListener(this);
                    mBinding.btnSwitch.setVisibility(View.VISIBLE);
                    mBinding.btnSwitch.setChecked("opened".equals(item.getMetadata()));
                    break;
                }
                case "ht": {
                    mBinding.btnSwitch.setVisibility(View.GONE);
                    if (item.getMetadata().contains(",")) {
                        String[] temp = item.getMetadata().split(",");
                        if (temp != null && temp.length > 1) {
                            item.setMetadata(String.format("温度 %s°C, 湿度 %s%%", temp[0], temp[1]));
                        }
                    }
                    break;
                }
                default:
                    mBinding.btnSwitch.setVisibility(View.GONE);
                    break;
            }

            mBinding.setDeviceItem(item);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), DeviceActivity.class);
            intent.putExtra(DeviceActivity.ACTION_DEVICE_OPERATE, DeviceActivity.ACTION_DEVICE_EDIT);
            intent.putExtra(DeviceActivity.EXTRA_DEVICE_ID, item.getId());
            view.getContext().startActivity(intent);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            item.setMetadata(isChecked ? "opened" : "closed");

            MessageService.publish(mContext, item.getDeviceId(), isChecked ? "051" : "050");
        }
    }

}
