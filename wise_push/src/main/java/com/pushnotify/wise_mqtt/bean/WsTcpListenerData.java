package com.pushnotify.wise_mqtt.bean;

import com.pushnotify.wise_mqtt.interf.OnWiseRequestListener;

/**
 * Created by xu.wang
 * Date on  2018/2/5 17:31:35.
 *
 * @Desc 模拟TCP请求超时, 及处理的代码结构
 */

public class WsTcpListenerData {
    private String event;
    private OnWiseRequestListener listener;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public OnWiseRequestListener getListener() {
        return listener;
    }

    public void setListener(OnWiseRequestListener listener) {
        this.listener = listener;
    }
}
