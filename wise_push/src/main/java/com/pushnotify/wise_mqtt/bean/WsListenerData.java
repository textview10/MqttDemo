package com.pushnotify.wise_mqtt.bean;

import com.pushnotify.wise_mqtt.interf.OnWisePushConnectListener;

/**
 * Created by xu.wang
 * Date on  2018/1/25 09:57:59.
 *
 * @Desc    每个注册事件的回调
 */

public class WsListenerData {
    private String event;
    private OnWisePushConnectListener listener;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public OnWisePushConnectListener getListener() {
        return listener;
    }

    public void setListener(OnWisePushConnectListener listener) {
        this.listener = listener;
    }
}
