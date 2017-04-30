package org.cn.plugin.dlna.utils;

import android.content.Context;
import android.content.Intent;

import org.cn.plugin.dlna.common.DLNAConst;
import org.cn.plugin.dlna.model.DeviceInfo;
import org.cn.plugin.dlna.model.DeviceMessage;
import org.cn.plugin.rpc.RpcEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenning on 17-1-13.
 */

public class DeviceUtil {
    static String[] TYPE_NT = {
            "urn:schemas-upnp-org:service:AVTransport:1",
            "urn:schemas-upnp-org:service:RenderingControl:1",
            "urn:schemas-upnp-org:device:MediaRenderer:1"
    };

    public static final Map<String, DeviceInfo> devices = new HashMap<>();

    private static MulticastSocket mMulticastSocket;

    public static DeviceInfo get(String key) {
        return devices.get(key);
    }

    public static void put(String key, DeviceInfo device) {
        devices.put(key, device);
    }

    public static boolean has(String key) {
        return devices.containsKey(key);
    }

    public static void remove(String key) {
        devices.remove(key);
    }

    public static void search() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        baos.write("M-SEARCH * HTTP/1.1".getBytes());
                        baos.write("\r\n".getBytes());
                        baos.write("HOST: 239.255.255.250:1900".getBytes());
                        baos.write("\r\n".getBytes());
                        baos.write("MAN: \"ssdp:discover\"".getBytes());
                        baos.write("\r\n".getBytes());
                        baos.write("MX: 5".getBytes());
                        baos.write("\r\n".getBytes());
                        baos.write("ST: \"ssdp:all\"".getBytes());
                        baos.write("\r\n".getBytes());

                        DatagramPacket packet = new DatagramPacket(baos.toByteArray(), 0, baos.size(), InetAddress.getByName("239.255.255.250"), 1900);

                        if (mMulticastSocket != null) {
                            mMulticastSocket.send(packet);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void start(final Context ctx) {
        try {
            InetAddress address = InetAddress.getByName("239.255.255.250");
            if (mMulticastSocket == null) {
                mMulticastSocket = new MulticastSocket(1900);
            }
            mMulticastSocket.joinGroup(address);

            new Thread(new Runnable() {
                public void run() {
                    byte[] bytes = new byte[1024];
                    while (true) {
                        try {
                            DatagramPacket packet = new DatagramPacket(bytes, 1024);
                            mMulticastSocket.receive(packet);

                            String data = new String(bytes, 0, packet.getLength());
                            if (parsePacket(packet.getAddress().toString(), data)) {
                                sendMessage(ctx);
                            }
                        } catch (IOException ignored) {
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (mMulticastSocket != null && mMulticastSocket.isConnected()) {
            mMulticastSocket.disconnect();
            mMulticastSocket.close();
            mMulticastSocket = null;
        }
    }

    private static void sendMessage(Context ctx) {
        Intent intent = new Intent(DLNAConst.ACTION_DLNA_DEVICES_NOTIFY);
        ctx.sendBroadcast(intent);
    }

    public static boolean parsePacket(String id, String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        if (data.toUpperCase().startsWith("M-SEARCH")) {
            return false;
        }
        DeviceMessage message = new DeviceMessage();
        message.id = id;
        message.body = data;

        String nt = message.getMessage("NT");
        if (nt == null || nt.isEmpty()) {
            return false;
        }
        boolean isAvailable = false;
        for (String type : TYPE_NT) {
            if (type.equalsIgnoreCase(nt)) {
                isAvailable = true;
            }
        }
        if (!isAvailable) {
            return false;
        }

        String nts = message.getMessage("NTS");
        if (nts == null || nts.isEmpty()) {
            return false;
        }
        if (nts.toUpperCase().contains(":BYEBYE")) {
            if (DeviceUtil.has(message.id)) {
                DeviceUtil.remove(message.id);
            }
            return true;
        }
        if (nts.toUpperCase().contains(":ALIVE")) {
            if (DeviceUtil.has(message.id)) {
                return false;
            }

            String location = message.getMessage("Location");
            String xml = RpcEngine.get(location).result;

            if (xml == null || xml.isEmpty()) {
                return false;
            }

            Map<String, String> map = XMLUtil.parseSetupOne(xml);

            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.id = message.id;
            deviceInfo.friendlyName = map.get("friendlyName");
            deviceInfo.modelName = map.get("modelName");
            deviceInfo.udn = map.get("UDN");

            URI uri = URI.create(location);
            String url = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + map.get("controlURL");
            deviceInfo.controlURL = url;
            if (map.containsKey("imagepng120")) {
                deviceInfo.iconUrl = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + map.get("imagepng120");
            } else if (map.containsKey("imagejpeg120")) {
                deviceInfo.iconUrl = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + map.get("imagejpeg120");
            } else if (map.containsKey("imagepng48")) {
                deviceInfo.iconUrl = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + map.get("imagepng48");
            } else if (map.containsKey("imagejpeg48")) {
                deviceInfo.iconUrl = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + map.get("imagejpeg48");
            }
            DeviceUtil.put(message.id, deviceInfo);
            return true;
        }
        return false;
    }

}
