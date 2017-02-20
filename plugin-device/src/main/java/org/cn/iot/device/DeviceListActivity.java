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

import org.cn.iot.device.databinding.ActivityDeviceListBinding;
import org.cn.iot.device.fragment.DeviceListFragment;
import org.cn.iot.device.internal.BaseActivity;
import org.cn.iot.device.model.DeviceGroup;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenning on 2016/8/24.
 *
 * @Description 设备组
 */
public class DeviceListActivity extends BaseActivity {

    private ActivityDeviceListBinding mDeviceListBinding;

    private LayoutAdapter mLayoutAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDeviceListBinding = DataBindingUtil.setContentView(this, R.layout.activity_device_list);

        initView();
        initData();
        requestData();
    }

    private void initView() {
        mDeviceListBinding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mDeviceListBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mDeviceListBinding.viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mLayoutAdapter = new LayoutAdapter(getSupportFragmentManager());
        mDeviceListBinding.viewPager.setAdapter(mLayoutAdapter);

        mDeviceListBinding.tabLayout.setupWithViewPager(mDeviceListBinding.viewPager);
        mDeviceListBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mDeviceListBinding.tabLayout));

        mDeviceListBinding.setTitle("Devices");
        mDeviceListBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mDeviceListBinding.btnDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DeviceActivity.class);
                intent.putExtra(DeviceActivity.ACTION_DEVICE_OPERATE, "Add");
                startActivity(intent);
            }
        });
    }

    private void initData() {

    }

    private void requestData() {

        List<DeviceGroup> list = new ArrayList<>();
        try {
            InputStream is = getResources().getAssets().open("device_group.json");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 8];
            int len;
            while ((len = is.read(buffer)) >= 0) {
                baos.write(buffer, 0, len);
            }
            list.addAll(JSON.parseArray(baos.toString(), DeviceGroup.class));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        mLayoutAdapter.updateData(list);
    }

    class LayoutAdapter extends FragmentPagerAdapter {

        private List<DeviceGroup> mData = new ArrayList<>();

        public LayoutAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putString(DeviceListFragment.DEVICE_GROUP_ID, mData.get(position).getId());

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
            return mData.get(position).getName();
        }

        public void updateData(List<DeviceGroup> list) {
            mData.clear();
            if (list != null) {
                mData.addAll(list);
            }
            if (mData.size() <= 3) {
                mDeviceListBinding.tabLayout.setTabMode(TabLayout.MODE_FIXED);
            }
            notifyDataSetChanged();
        }
    }

}
