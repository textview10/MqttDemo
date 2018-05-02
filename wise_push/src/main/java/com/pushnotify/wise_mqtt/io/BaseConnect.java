package com.pushnotify.wise_mqtt.io;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.pushnotify.wise_mqtt.WisePush;
import com.pushnotify.wise_mqtt.bean.WsListenerData;
import com.pushnotify.wise_mqtt.bean.WsReceiveData;
import com.pushnotify.wise_mqtt.bean.WsTcpListenerData;
import com.pushnotify.wise_mqtt.interf.OnWisePushConnectListener;
import com.pushnotify.wise_mqtt.interf.OnWiseRequestListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by xu.wang
 * Date on  2018/1/25 17:17:37.
 *
 * @Desc 所有长连接的base
 */

public abstract class BaseConnect extends Thread {
    public String FORMAT_TOPIC_1 = "ZK/Users/%s";
    public String FORMAT_TOPIC_2 = "ZK/Apps/%s/%s";
    public String FORMAT_CLIENT_ID = "ZK_%s_Android_%s";
    protected String TAG = "BaseConnect";
    private ArrayList<WsListenerData> mDataLists = new ArrayList<>();   //界面注册的数据和listener
    protected ArrayList<WsTcpListenerData> mRequestLists = new ArrayList<>();     //请求接口的list
    private static final int TIME_DELAY = 8000; //请求时间超时

    private boolean isDebug = true;
    private Toast toast;

    protected abstract void startConnect(); //开始连接

    protected abstract boolean isConnected();   //判断是否可连接

    protected abstract void disConnect();   //断开连接

    protected abstract void publish(String topic, String json) throws Exception;  //发送消息

    protected abstract void subscribeTopic();   //订阅主题

    public BaseConnect() {
        Collections.synchronizedList(mRequestLists);
    }

    @Override
    public void run() {
        super.run();
        startConnect();
    }

    //发送无须在本次回应中回调的消息
    public void sendMsg(String topic, String body) {
        try {
            publish(topic, body);
        } catch (Exception e) {
            Log.e(TAG, "send msg Exception = " + e.toString());
        }
    }

    //发送需要在本次回应中回调的消息
    public void sendMsgReponse(String topic, String event, String json, final OnWiseRequestListener listener) {
        if (!isConnected()) {
            Log.e(TAG, "send msg error: 还没有建立有效连接");
            return;
        }
        final WsTcpListenerData data = new WsTcpListenerData();
        data.setEvent(event);
        data.setListener(listener);
        mRequestLists.add(data);
        try {
            publish(topic, json);
        } catch (final Exception e) {
            //发送失败,回调异常
            Log.e(TAG, "send response msg exception = " + e.toString());
            WisePush.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestLists.contains(data)) {
                        mRequestLists.remove(data);
                        WisePush.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                data.getListener().onError(e.toString());
                            }
                        });
                    }
                }
            });
        }
        sendDelayMsg(data);
    }

    //发送延时取消的消息
    private void sendDelayMsg(final WsTcpListenerData data) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                SystemClock.sleep(TIME_DELAY);
                if (mRequestLists.contains(data)) {
                    mRequestLists.remove(data);
                    WisePush.mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            data.getListener().onError("connect time out " + TIME_DELAY + "ms");
                        }
                    });
                }
            }
        }.start();
    }

    public void regeisterServerMsg(ArrayList<String> lists, OnWisePushConnectListener listener) {
        for (int i = 0; i < lists.size(); i++) {
            WsListenerData data = new WsListenerData();
            data.setEvent(lists.get(i));
            data.setListener(listener);
            mDataLists.add(data);
            showLog("regeister event " + data.getEvent() + " + listener" + data.getListener());
        }
    }

    public void unRegeisterServerMsg(OnWisePushConnectListener listener) {
        for (int i = 0; i < mDataLists.size(); i++) {
            WsListenerData data = mDataLists.get(i);
            if (data.getListener() == listener) {
                showLog("unregeister event " + data.getEvent() + " + listener" + data.getListener() + " + size = " + mDataLists.size());
                mDataLists.remove(i--);
            }
        }
    }

    /**
     * 对相应注册回调模块进行json回调
     *
     * @param event
     * @param json
     */
    protected void onDataReceiveCallBack(final String event, final String json, final WsReceiveData wsReceiveData) {
        for (int i = 0; i < mDataLists.size(); i++) {
            final WsListenerData data = mDataLists.get(i);
            if (data == null || TextUtils.isEmpty(data.getEvent()) || data.getListener() == null) {
                showLog("error data == null || TextUtils.isEmpty(data.getKey()) || data.getListener() == null");
                continue;
            }
            if (TextUtils.equals(data.getEvent(), event)) {
                WisePush.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        data.getListener().onDataReceive(event, json, wsReceiveData);
                    }
                });
            }
        }
        for (int i = 0; i < mRequestLists.size(); i++) {    //用于模拟Tcp请求的超时和回应
            final WsTcpListenerData data = mRequestLists.get(i);
            if (data == null) {
                continue;
            }
            if (TextUtils.isEmpty(data.getEvent())) {
                if (data.getListener() != null) {
                    mRequestLists.remove(data);
                    WisePush.mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            data.getListener().onError("event  == null");
                        }
                    });
                }
                continue;
            }
            if (TextUtils.equals(data.getEvent(), event)) {
                mRequestLists.remove(data);
                WisePush.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        data.getListener().onSuccess(event, json, wsReceiveData);
                    }
                });
            }
        }
    }

    /**
     * 断开连接的回调
     */
    protected void disConnectCallBack() {
        showToast("与消息服务器断开连接");
        ArrayList<OnWisePushConnectListener> totalLists = getAvailListener();
        for (int i = 0; i < totalLists.size(); i++) {
            final OnWisePushConnectListener onWisePushConnectListener = totalLists.get(i);
            WisePush.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onWisePushConnectListener.disConnect();
                }
            });
        }
    }

    /**
     * 连接错误会对所有listener进行回调
     */
    protected void connectFailCallBack(final String exception) {
        showLog("disConnectCallBack = " + exception + " connected state = " + isConnected());
        ArrayList<OnWisePushConnectListener> totalLists = getAvailListener();
        for (int i = 0; i < totalLists.size(); i++) {
            final OnWisePushConnectListener onWebSocketConnectListener = totalLists.get(i);
            WisePush.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onWebSocketConnectListener.onConnectFail(exception);
                }
            });
        }
    }

    /**
     * 连接成功会对所有listener进行回调
     */
    protected void connectSuccessCallBack(boolean reconnect) {
        if (!reconnect) {
            subscribeTopic();
        }
        showToast("与消息服务器连接成功");
        ArrayList<OnWisePushConnectListener> totalLists = getAvailListener();
        for (int i = 0; i < totalLists.size(); i++) {
            final OnWisePushConnectListener onWebSocketConnectListener = totalLists.get(i);
            WisePush.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onWebSocketConnectListener.connect();
                }
            });
        }
    }

    private ArrayList<OnWisePushConnectListener> getAvailListener() {
        ArrayList<OnWisePushConnectListener> list = new ArrayList<>();
        for (int i = 0; i < mDataLists.size(); i++) {   //去掉重复的回调
            WsListenerData data = mDataLists.get(i);
            if (data.getListener() == null) {
                showLog(" data.getListener() == null");
                continue;
            }
            if (list.contains(data.getListener())) {
                continue;
            }
            list.add(data.getListener());
        }
        return list;
    }

    protected void showLog(String msg) {
        if (isDebug) Log.i(TAG, "" + msg);
    }

    protected void showToast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(WisePush.mApp, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
}
