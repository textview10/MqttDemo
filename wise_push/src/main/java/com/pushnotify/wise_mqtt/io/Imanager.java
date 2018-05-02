package com.pushnotify.wise_mqtt.io;

import com.pushnotify.wise_mqtt.interf.OnWisePushConnectListener;
import com.pushnotify.wise_mqtt.interf.OnWiseRequestListener;


import java.util.ArrayList;

/**
 * Created by xu.wang
 * Date on  2018/1/12 16:00:47.
 *
 * @Desc 网络管理的基础结构
 */

public interface Imanager {
    void connect();

    void disConnect();

    void regeisterServerMsg(ArrayList<String> lists, OnWisePushConnectListener listener);

    void unRegeisterServerMsg(OnWisePushConnectListener listener);

    void sendMsg(String topic, String json);

    void sendMsg(String topic, String action, Object body);

    void sendMsg(String topic, String action, String event, Object body, OnWiseRequestListener listener);

    boolean isConnected();
}
