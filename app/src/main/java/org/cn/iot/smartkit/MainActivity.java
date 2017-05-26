package org.cn.iot.smartkit;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.cn.iot.device.DeviceListActivity;
import org.cn.iot.smartkit.common.BaseActivity;
import org.cn.iot.smartkit.common.OptionalActivity;
import org.cn.iot.smartkit.simple.SimpleActivity;
import org.cn.plugin.airkiss.SmartConfigActivity;
import org.cn.plugin.common.permission.PermissionManager;
import org.cn.plugin.dlna.utils.DeviceUtil;
import org.cn.plugin.message.MessageActivity;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceUtil.search();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // close drawer layout first
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // Handle navigation view item clicks here.
        final int id = item.getItemId();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onNavigationItemSelected(id);
            }
        }, 300);

        if (id != R.id.nav_home) {
            return false;
        }
        return true;
    }

    private void onNavigationItemSelected(int id) {
        switch (id) {
            case R.id.nav_home: {

                break;
            }
            case R.id.nav_settings: {
                Intent intent = new Intent(this, OptionalActivity.class);
                intent.putExtra(OptionalActivity.ACTION_OPTIONAL, "all");
                startActivity(intent);
                break;
            }
            case R.id.nav_air_kiss: {
                PermissionManager.requestPermissions(this, new PermissionManager.OnPermissionsCallback() {
                    @Override
                    public void onRequestPermissionsResult(boolean success, String[] permission, int[] grantResult, boolean[] showRequestRationale) {
                        if (success) {
                            Intent intent = new Intent(MainActivity.this, SmartConfigActivity.class);
                            startActivity(intent);
                        } else {
                            PermissionManager.toAppSetting(MainActivity.this);
                        }
                    }
                }, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            }
            case R.id.nav_message: {
                Intent intent = new Intent(this, MessageActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_device: {
                Intent intent = new Intent(this, DeviceListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_timeline: {

                break;
            }
            case R.id.nav_simple: {
                Intent intent = new Intent(MainActivity.this, SimpleActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }
}
