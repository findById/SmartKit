package org.cn.iot.smartkit;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.cn.iot.device.DeviceListActivity;
import org.cn.iot.smartkit.simple.SimpleActivity;
import org.cn.iot.smartkit.utils.PermissionUtils;
import org.cn.plugin.airkiss.SmartConfigActivity;
import org.cn.plugin.common.optional.OptionalActivity;
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, OptionalActivity.class);
            intent.putExtra(OptionalActivity.ACTION_OPTIONAL, "all");
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            case R.id.nav_air_kiss: {
                PermissionUtils.requestPermissions(this, new PermissionUtils.OnPermissionsCallback() {
                    @Override
                    public void onRequestPermissionsResult(boolean success, String[] permission, int[] grantResult, boolean[] showRequestRationale) {
                        if (success) {
                            Intent intent = new Intent(MainActivity.this, SmartConfigActivity.class);
                            startActivity(intent);
                        } else {
                            PermissionUtils.toAppSetting(MainActivity.this);
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
            case R.id.nav_simple: {
                Intent intent = new Intent(this, SimpleActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }
}
