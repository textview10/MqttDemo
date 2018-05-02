package com.pushnotify.wise_mqtt.bean;


/**
 * Created by xu.wang
 * Date on  2018/1/25 11:50:28.
 *
 * @Desc event 收到消息的数据结构
 */

public class WsReceiveData {
    private String event;
    private String success;
    private String data;    //data内容
    private String json;    //整条数据的结构

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
