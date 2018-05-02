package com.pushnotify.wise_mqtt.interf;

import com.pushnotify.wise_mqtt.bean.WsReceiveData;

/**
 * Created by xu.wang
 * Date on  2018/2/5 17:18:04.
 *
 * @Desc
 */

public interface OnWiseRequestListener {
    void onSuccess(String event, String json, WsReceiveData data);
    void onError(String error);
}
