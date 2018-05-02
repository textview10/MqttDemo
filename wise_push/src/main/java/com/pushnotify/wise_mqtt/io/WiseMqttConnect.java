package com.pushnotify.wise_mqtt.io;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.pushnotify.wise_mqtt.WisePush;
import com.pushnotify.wise_mqtt.bean.WsReceiveData;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by xu.wang
 * Date on  2018/1/24 17:49:41.
 *
 * @Desc 控制WiseMqttConnect的线程
 */

public class WiseMqttConnect extends BaseConnect {
    private MqttClient client;
    private MqttConnectOptions options;

    public WiseMqttConnect() {
        TAG = "WiseMqttConnect";
    }

    @Override
    protected void startConnect() {
        try {
            String URL_FORMAT = "tcp://%s:%d";
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(String.format(URL_FORMAT, WisePush.ip, WisePush.port), String.valueOf(WisePush.mUserId), new MemoryPersistence());
            showLog(String.format(URL_FORMAT, WisePush.ip, WisePush.port));
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(120);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(2);
            options.setAutomaticReconnect(true);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    connectFailCallBack(cause.getMessage().toString());
                    try {
                        client.reconnect();
                        if (isConnected()) {
                            connectSuccessCallBack(true);
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里

//                    Log.e(TAG, "deliveryComplete---------" + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message) throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    String msg = new String(message.getPayload()).toString();
                    String json = msg.replace("\\", "");
                    showLog("receive a msg topicName = " + topicName + " message name =  " + json);
                    try {
                        WsReceiveData wsReceiveData = JSONObject.parseObject(json, WsReceiveData.class);
                        showLog("jsonObject sucess");
                        onDataReceiveCallBack(wsReceiveData.getEvent(), JSONObject.toJSONString(wsReceiveData.getData()), wsReceiveData);
                    } catch (Exception e) {
                        showLog("messageArrived exception = " + e.toString());
                    }
                }
            });
            long startTime = System.currentTimeMillis();
            client.connect(options);
            if (isConnected()) {
                showLog("connect success spend time = " + (System.currentTimeMillis() - startTime));
                connectSuccessCallBack(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception = " + e.toString());
        }
    }

    @Override
    public boolean isConnected() {
        if (client == null) {
            return false;
        }
        return client.isConnected();
    }

    @Override
    public void disConnect() {
        if (client != null) {
            try {
                client.disconnect();
                client.close();
                disConnectCallBack();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void publish(String topic, String json) throws Exception {
        MqttMessage mqttMsg = new MqttMessage();
        mqttMsg.setPayload(json.getBytes());
        client.publish(topic, mqttMsg);
    }

    @Override
    protected void subscribeTopic() {
        try {
            String topic1 = String.format(FORMAT_TOPIC_1, WisePush.mUserId);
            String topic2 = String.format(FORMAT_TOPIC_2, WisePush.mCurAppName, WisePush.mUserId);
            client.subscribe(topic1, 1);
            client.subscribe(topic2, 1);
            showLog("subscribe topic1 = " + topic1 + " topic2 = " + topic2);
        } catch (MqttException e) {
            Log.e(TAG, "subscribe error = " + e.toString());
        }
    }


}
