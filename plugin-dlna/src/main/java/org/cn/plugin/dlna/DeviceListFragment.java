package org.cn.plugin.dlna;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.cn.plugin.dlna.common.DLNAConst;
import org.cn.plugin.dlna.databinding.ItemMediaDeviceListBinding;
import org.cn.plugin.dlna.helper.ControlHelper;
import org.cn.plugin.dlna.model.DeviceInfo;
import org.cn.plugin.dlna.utils.DeviceUtil;
import org.cn.plugin.dlna.utils.ImageLoader;
import org.cn.plugin.dlna.widget.DeviceListDivider;
import org.cn.plugin.rpc.Response;
import org.cn.plugin.rpc.ResponseListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private DeviceListAdapter mDeviceListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_device_list, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mDeviceListAdapter = new DeviceListAdapter(getActivity());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DeviceListDivider(getActivity()));
        mRecyclerView.setAdapter(mDeviceListAdapter);

        getActivity().registerReceiver(receiver, new IntentFilter(DLNAConst.ACTION_DLNA_DEVICES_NOTIFY));
        DeviceUtil.start(getActivity());
        initData();
    }

    @Override
    public void onDestroy() {
        DeviceUtil.stop();
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("D", "broadcast receiving...");
            initData();
        }
    };

    private void initData() {
        Map<String, DeviceInfo> map = DeviceUtil.devices;
        List<DeviceInfo> list = new ArrayList<>();
        for (String key : map.keySet()) {
            list.add(map.get(key));
        }
        mDeviceListAdapter.updateData(list);
    }

    class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        private LayoutInflater mInflater;

        private List<DeviceInfo> mData = new ArrayList<>();

        public DeviceListAdapter(Context ctx) {
            mInflater = LayoutInflater.from(ctx);
            ImageLoader.init(R.drawable.ic_device);
        }

        @Override
        public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ItemMediaDeviceListBinding bind = DataBindingUtil.inflate(mInflater, R.layout.item_media_device_list, parent, false);
            return new ViewHolder(bind);
        }

        @Override
        public void onBindViewHolder(DeviceListAdapter.ViewHolder holder, int position) {
            holder.mBind.setDevice(mData.get(position));
            holder.onBindData();
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public void addAll(List<DeviceInfo> list) {
            if (list == null || list.isEmpty()) {
                return;
            }
            mData.addAll(list);
            notifyDataSetChanged();
        }

        public void updateData(List<DeviceInfo> list) {
            mData.clear();
            if (list != null && list.size() > 0) {
                mData.addAll(list);
            }
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ItemMediaDeviceListBinding mBind;

            public ViewHolder(ItemMediaDeviceListBinding mBind) {
                super(mBind.getRoot());
                this.mBind = mBind;
                mBind.layoutDevice.setOnClickListener(this);
            }

            public void onBindData() {
                ImageLoader.display(mBind.getDevice().iconUrl, mBind.itemIcon);
            }

            @Override
            public void onClick(final View v) {
                int i = v.getId();
                if (i == R.id.layout_device) {
                    final EditText editText = new EditText(v.getContext());
                    editText.setText("");
                    new AlertDialog.Builder(getActivity())
                            .setMessage("网络流")
                            .setView(editText)
                            .setPositiveButton("播放", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String url = editText.getText().toString();
                                    if (TextUtils.isEmpty(url)) {
                                        Snackbar.make(v, "'URL' must not be null", Snackbar.LENGTH_LONG).show();
                                        return;
                                    }
                                    play(url);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            }

            private void play(String url) {
                ControlHelper.setUrl(mBind.getDevice().controlURL, url, new ResponseListener<Response>() {
                    @Override
                    public void onResponse(Response response) {
                        Log.d("D", response.toString());
                        ControlHelper.play(mBind.getDevice().controlURL, new ResponseListener<Response>() {
                            @Override
                            public void onResponse(Response response) {
                                Log.d("D", response.toString());
                            }
                        });
                    }
                });
            }
        }
    }

}
