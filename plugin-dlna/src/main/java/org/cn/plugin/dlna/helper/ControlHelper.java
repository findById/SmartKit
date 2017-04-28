package org.cn.plugin.dlna.helper;

import android.os.Looper;

import org.cn.plugin.rpc.RealService;
import org.cn.plugin.rpc.Request;
import org.cn.plugin.rpc.Response;
import org.cn.plugin.rpc.ResponseListener;

/**
 * Created by chenning on 17-1-13.
 */

public class ControlHelper {

    public static void setUrl(String url, String media, ResponseListener<Response> listener) {
        String body = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" +
                "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <s:Body>\n" +
                "        <u:SetAVTransportURI xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">\n" +
                "            <InstanceID>0</InstanceID>\n" +
                "            <CurrentURI>" + media + "</CurrentURI>\n" +
                "            <CurrentURIMetaData />" +
                "        </u:SetAVTransportURI>\n" +
                "    </s:Body>\n" +
                "</s:Envelope>";

        sendMessage(url, body, "SetAVTransportURI", listener);
    }

    public static void play(String url, ResponseListener<Response> listener) {
        String body = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" +
                "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <s:Body>\n" +
                "        <u:Play xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">\n" +
                "            <InstanceID>0</InstanceID>\n" +
                "            <Speed>1</Speed>\n" +
                "        </u:Play>\n" +
                "    </s:Body>\n" +
                "</s:Envelope>";

        sendMessage(url, body, "Play", listener);
    }

    public static void pause(String url, ResponseListener<Response> listener) {
        String body = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" +
                "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <s:Body>\n" +
                "        <u:Pause xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">\n" +
                "            <InstanceID>0</InstanceID>\n" +
                "        </u:Pause>\n" +
                "    </s:Body>\n" +
                "</s:Envelope>";
        sendMessage(url, body, "Pause", listener);
    }

    private static void sendMessage(String url, String body, String action, ResponseListener<Response> listener) {
        Request.Builder builder = Request.create();
        builder.post(url).body(body);
        builder.contentType("text/xml;charset=utf8");
        builder.addHeader("SOAPACTION", String.format("\"urn:schemas-upnp-org:service:AVTransport:1#%s\"", action));

        RealService real = new RealService(builder.build());
        real.enqueue(Looper.getMainLooper(), listener);
    }

}
