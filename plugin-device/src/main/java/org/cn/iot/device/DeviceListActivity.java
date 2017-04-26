package org.cn.iot.device;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.cn.iot.device.databinding.ActivityDeviceListBinding;
import org.cn.iot.device.fragment.DeviceListFragment;
import org.cn.iot.device.internal.BaseActivity;
import org.cn.iot.device.model.Device;
import org.cn.plugin.rpc.Response;
import org.cn.plugin.rpc.ResponseListener;
import org.cn.plugin.rpc.RpcEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenning on 2016/8/24.
 *
 * @Description 设备组
 */
public class DeviceListActivity extends BaseActivity {

    private ActivityDeviceListBinding mBinding;

    private LayoutAdapter mLayoutAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_device_list);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        initData();
        requestData();
    }

    private void initView() {
        mBinding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mBinding.viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mLayoutAdapter = new LayoutAdapter(getSupportFragmentManager());
        mBinding.viewPager.setAdapter(mLayoutAdapter);

        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);
        mBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabLayout));

        mBinding.setTitle("Devices");
        mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mBinding.btnDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DeviceActivity.class);
                intent.putExtra(DeviceActivity.ACTION_DEVICE_OPERATE, DeviceActivity.ACTION_DEVICE_ADD);
                startActivity(intent);
            }
        });
    }

    private void initData() {

    }

    private void requestData() {
        JSONObject biz = new JSONObject();
        biz.put("userId", "client");

        JSONObject param = new JSONObject();
        param.put("method", "iot.device.list");
        param.put("content", biz.toJSONString());
        RpcEngine.post(DeviceConst.API_HOST + "/getaway", param.toJSONString(), new ResponseListener<Response>() {
            @Override
            public void onResponse(Response response) {
                if (!response.isSuccess()) {
                    return;
                }
                JSONObject obj = JSON.parseObject(response.result).getJSONObject("result");
                int statusCode = obj.getInteger("statusCode");
                if (statusCode != 200) {
                    return;
                }
                List<Device> list = JSON.parseArray(obj.getJSONArray("result").toJSONString(), Device.class);
                mLayoutAdapter.updateData(list);
            }
        });
    }

    class LayoutAdapter extends FragmentPagerAdapter {

        private List<Device> mData = new ArrayList<>();

        public LayoutAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putString(DeviceListFragment.DEVICE_ID, mData.get(position).getDeviceId());

            Fragment fragment = new DeviceListFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mData.get(position).getGroupName();
        }

        public void updateData(List<Device> list) {
            mData.clear();
            if (list != null) {
                mData.addAll(list);
            }
            if (mData.size() <= 3) {
                mBinding.tabLayout.setTabMode(TabLayout.MODE_FIXED);
            }
            notifyDataSetChanged();
        }
    }

}
