package com.pushnotify.wise_mqtt.bean;


/**
 * Created by xu.wang
 * Date on  2018/1/26 14:10:08.
 *
 * @Desc 发送的数据格式
 */

public class WsSendData {
    private int userId;
    private String action;
    private Object data;
    private String tokenId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}
