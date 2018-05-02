package com.pushnotify.wise_mqtt.io;

import android.util.Log;

import com.pushnotify.wise_mqtt.WisePush;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * Created by xu.wang
 * Date on  2018/2/6 11:11:38.
 *
 * @Desc WebSocket长连接及回调的处理
 */

public class WebSocketConnect extends BaseConnect {

    private WebSocketClient webSocketClient;

    public WebSocketConnect() {
        TAG = "WebSocketConnect";
    }

    @Override
    protected void startConnect() {
        URI uri = null;
        String URL_FORMAT = "http://%s:%d";
        try {
            uri = new URI(String.format(URL_FORMAT, WisePush.ip, WisePush.port));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (uri == null){
            Log.e(TAG,"连接服务器失败...");
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.e(TAG, "与服务器连接成功 = " + handshakedata.getHttpStatusMessage());
            }

            @Override
            public void onMessage(String message) {

            }

            @Override
            public void onClose(int code, String reason, boolean remote) {

            }

            @Override
            public void onError(Exception ex) {

            }
        };
        webSocketClient.connect();
    }

    @Override
    protected boolean isConnected() {
        if (webSocketClient == null){
            return false;
        }
        if (webSocketClient.isClosed()){
            return false;
        }
        if (webSocketClient.isOpen()){
            return true;
        }
        return false;
    }

    @Override
    protected void disConnect() {

    }

    @Override
    protected void publish(String topic, String json) throws Exception {

    }

    @Override
    protected void subscribeTopic() {

    }
}
