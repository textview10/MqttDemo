package com.pushnotify.wise_mqtt.io;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.pushnotify.wise_mqtt.WisePush;
import com.pushnotify.wise_mqtt.bean.WsReceiveData;
import com.pushnotify.wise_mqtt.util.BuildRandomNumber;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;

/**
 * Created by xu.wang
 * Date on  2018/1/25 17:29:03.
 *
 * @Desc 使用AndroidClient的Mqtt模块, 用以替代使用MqttClient的模块
 */

public class WiseMqttAndroidConnect extends BaseConnect {
    private MqttAndroidClient mqttAndroidClient;

    public WiseMqttAndroidConnect() {
        TAG = "WiseMqttAndroidConnect";
    }

    @Override
    protected void startConnect() {
        try {
            final long startTime = System.currentTimeMillis();
            String URL_FORMAT = "tcp://%s:%d";
            String randomId = BuildRandomNumber.createGUID();
            String clientId = String.format(FORMAT_CLIENT_ID, WisePush.APP_TEACHER_NAME, randomId);
            showLog("clientId = " + clientId);
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            showLog(String.format(URL_FORMAT, WisePush.ip, WisePush.port));
            mqttAndroidClient = new MqttAndroidClient(WisePush.mApp.getApplicationContext(),
                    String.format(URL_FORMAT, WisePush.ip, WisePush.port), clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        // Because Clean Session is true, we need to re-subscribe
                        showLog("It is reconnect = " + reconnect);
                    } else {
                        showLog("It is first connect...");
                    }
                    connectSuccessCallBack(reconnect);
                }

                @Override
                public void connectionLost(Throwable cause) {
                    showLog("connectionLost = " + cause == null ? "" : cause.getMessage());
                    disConnectCallBack();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String json = new String(message.getPayload()).toString();
                    showLog("receive a  msg topicName = " + topic + "origin message name = " +
                            new String(message.getPayload()).toString() + "finally message name =  " + json);
                    try {
                        WsReceiveData wsReceiveData = JSONObject.parseObject(json, WsReceiveData.class);
                        wsReceiveData.setJson(json);
                        onDataReceiveCallBack(wsReceiveData.getEvent(), wsReceiveData.getData(), wsReceiveData);
                    } catch (Exception e) {
                        Log.e(TAG, "messageArrived exception = " + e.toString());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
//                    showLog("deliveryComplete");
                }
            });

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    showLog("connectSuccess spend time = " + (System.currentTimeMillis() - startTime));
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    showLog("connect failure = " + exception.getMessage().toString());
                    connectFailCallBack(exception.getMessage().toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception = " + e.toString());
        }
    }

    @Override
    protected void subscribeTopic() {
        try {
            String topic1 = String.format(FORMAT_TOPIC_1, WisePush.mUserId);
            String topic2 = String.format(FORMAT_TOPIC_2, WisePush.mCurAppName, WisePush.mUserId);
            mqttAndroidClient.subscribe(topic1, 1);
            mqttAndroidClient.subscribe(topic2, 1);
            showLog("subscribe topic1 = " + topic1 + " topic2 = " + topic2);
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "subscribe topic exception = " + e.toString());
        }
    }

    @Override
    public boolean isConnected() {
        if (mqttAndroidClient == null) {
            return false;
        }
        return mqttAndroidClient.isConnected();
    }

    @Override
    public void disConnect() {
        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.disconnect();
                mqttAndroidClient.close();
                disConnectCallBack();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void publish(String topic, String msg) throws Exception {
        MqttMessage mqttMsg = new MqttMessage();
        mqttMsg.setPayload(msg.getBytes());
        mqttAndroidClient.publish(topic, mqttMsg);
    }

}
