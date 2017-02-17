package org.cn.plugin.message;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MessagePlugin {

    private static String host = MessageConst.host;
    private static String username = MessageConst.username;
    private static String password = MessageConst.password;
    private MqttClient mqttClient;
    private MqttTopic mqttTopic;
    private MqttConnectOptions options;

    public void onCreate() {
        try {
            if (mqttClient != null && options != null) {
                return;
            }

            mqttClient = new MqttClient(host, "AndroidMessageClient", new MemoryPersistence());
            options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    while (!mqttClient.isConnected()) {
                        System.out.println("Connection lost, try again in 5 seconds");
                        try {
                            Thread.sleep(1000 * 5);
                        } catch (InterruptedException e) {
                        }
                        connect();
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("Message arrived[" + topic + "], " + message.toString());
                    handleMessage(topic, message.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    public void connect() {
        if (mqttClient == null || options == null) {
            onCreate();
        }
        if (mqttClient.isConnected()) {
            return;
        }

        try {
            mqttClient.connect(options);
            mqttClient.subscribe("subscribe");
            mqttClient.subscribe("deviceId");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public boolean publish(String id, String message) {
        try {
            mqttTopic = mqttClient.getTopic(id);
            IMqttDeliveryToken token = mqttTopic.publish(message.getBytes(), 1, true);
            token.waitForCompletion();
            return token.isComplete();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void handleMessage(String id, String message) {
        if (listener != null) {
            listener.onHandleMessage(id, message);
        }
    }


    private OnHandleMessageListener listener;

    public void setListener(OnHandleMessageListener listener) {
        this.listener = listener;
    }

    public interface OnHandleMessageListener {
        void onHandleMessage(String id, String message);
    }

}
