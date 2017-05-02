package org.cn.plugin.airkiss.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class EspWifiUtil {

	public static String getSSID(Context ctx) {
		WifiInfo mWifiInfo = getConnectionInfo(ctx);
		String ssid = null;
		if (mWifiInfo != null && isWifiConnected(ctx)) {
			int len = mWifiInfo.getSSID().length();
			if (mWifiInfo.getSSID().startsWith("\"") && mWifiInfo.getSSID().endsWith("\"")) {
				ssid = mWifiInfo.getSSID().substring(1, len - 1);
			} else {
				ssid = mWifiInfo.getSSID();
			}
		}
		return ssid;
	}

	public static String getBSSID(Context ctx) {
		WifiInfo mWifiInfo = getConnectionInfo(ctx);
		String bssid = null;
		if (mWifiInfo != null && isWifiConnected(ctx)) {
			bssid = mWifiInfo.getBSSID();
		}
		return bssid;
	}

	public static String getSSIDAscii(Context ctx, String ssid) {
		final long timeout = 100;
		final long interval = 20;
		String ssidAscii = ssid;

		WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		wifiManager.startScan();

		boolean isBreak = false;
		long start = System.currentTimeMillis();
		do {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException ignore) {
				isBreak = true;
				break;
			}
			List<ScanResult> scanResults = wifiManager.getScanResults();
			for (ScanResult scanResult : scanResults) {
				if (scanResult.SSID != null && scanResult.SSID.equals(ssid)) {
					isBreak = true;
					try {
						Field wifiSsidfield = ScanResult.class.getDeclaredField("wifiSsid");
						wifiSsidfield.setAccessible(true);
						Class<?> wifiSsidClass = wifiSsidfield.getType();
						Object wifiSsid = wifiSsidfield.get(scanResult);
						Method method = wifiSsidClass.getDeclaredMethod("getOctets");
						byte[] bytes = (byte[]) method.invoke(wifiSsid);
						ssidAscii = new String(bytes, "ISO-8859-1");
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		} while (System.currentTimeMillis() - start < timeout && !isBreak);
		return ssidAscii;
	}

	// get the wifi info which is "connected" in wifi-setting
	private static WifiInfo getConnectionInfo(Context ctx) {
		WifiManager mWifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		return wifiInfo;
	}

	private static boolean isWifiConnected(Context ctx) {
		NetworkInfo mWiFiNetworkInfo = getWifiNetworkInfo(ctx);
		boolean isWifiConnected = false;
		if (mWiFiNetworkInfo != null) {
			isWifiConnected = mWiFiNetworkInfo.isConnected();
		}
		return isWifiConnected;
	}

	private static NetworkInfo getWifiNetworkInfo(Context ctx) {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWiFiNetworkInfo;
	}
}
