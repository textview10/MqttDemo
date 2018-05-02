package com.pushnotify.wise_mqtt.interf;

import com.pushnotify.wise_mqtt.bean.WsReceiveData;

/**
 * Created by xu.wang
 * Date on  2018/1/25 09:45:23.
 *
 * @Desc Mqtt模块收到消息的回调
 */

public abstract class OnWisePushConnectListener {
    public void connect() {

    }

    public void disConnect() {

    }

    public abstract void onDataReceive(String event, String json, WsReceiveData wsReceiveData);

    public void onConnectFail(String exception) {

    }
}
