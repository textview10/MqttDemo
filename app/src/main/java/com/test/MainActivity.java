package com.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.pushnotify.mqttdemo.R;
import com.pushnotify.wise_mqtt.WisePush;
import com.pushnotify.wise_mqtt.bean.WsReceiveData;
import com.pushnotify.wise_mqtt.interf.OnWisePushConnectListener;
import com.pushnotify.wise_mqtt.interf.OnWiseRequestListener;


import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton btn_connect;
    private String TAG = "MainActivity";
    private long startTime;
    public String TOPIC_RECEIVE = "ZK/Users/111";   //只是模拟让本地TOPIC能收到消息,真实情况下不会发送这个TOPIC
    private Listener1 l1;
    private Listener2 l2;
    private Listener3 l3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_connect = findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        testLongConnect();
                    }
                }.start();

            }
        });
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("123", "456");
        jsonObject.put("111", "456");
        jsonObject.put("222", "456");
        final org.json.JSONObject object = new org.json.JSONObject();
        try {
            object.put("333", "456");
            object.put("123", "456");
            object.put("111", "456");
            object.put("222", "456");
            object.put("333", "456");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "" + e.toString());
        }

        //这条是模拟发一条本地可以收到的消息,是按照回应的格式来写的---------------
        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WisePush.getInstance().sendMsg(TOPIC_RECEIVE, WisePush.EVENT_TEST,
                        null);
//                WisePush.getInstance().sendMsg(TOPIC_RECEIVE,
//                        "{\"event\":\"event_danmuku\",\"data\":{\"userid\":\"111\",\"msg\":\"我是一条弹幕消息\",\"time\":1099009}}");
            }
        });
        //--------------------模拟一个http请求,及相关回调------------------
        findViewById(R.id.btn_tcp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testSendMsg();
            }
        });

        //--------------------这些是直接向服务器发送消息点的,虽然本地可以收到,但不符合回应的格式,不会在listener回调-----
        findViewById(R.id.btn_send1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WisePush.getInstance().sendMsg(TOPIC_RECEIVE, WisePush.EVENT_TEST, "test发消息");
            }
        });
        findViewById(R.id.btn_send2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WisePush.getInstance().sendMsg(TOPIC_RECEIVE, WisePush.EVENT_TEST_1, "test1模块发消息");
            }
        });
        findViewById(R.id.btn_send3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WisePush.getInstance().sendMsg(TOPIC_RECEIVE, WisePush.EVENT_DANMUKU, "向zonekey3发消息");
            }
        });
        //------------------------------------------------------------------------------------------
        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WisePush.getInstance().disConnect();
            }
        });
    }

    private void testSendMsg() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("123", "{\"userid\":\"111111\",\"msg\":\"我是一条弹幕消息\",\"time\":1099009}");
        WisePush.getInstance().sendMsg(
                "ZK/Users/11134",
                "event_danmuku","event_danmuku", jsonObject
                ,
                new OnWiseRequestListener() {
                    @Override
                    public void onSuccess(String event, String json, WsReceiveData data) {
                        Log.e(TAG, "模拟类似Tcp请求回应的 event =" + event + "json = " + json);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "模拟类似Tcp请求回应的 error = " + error);
                    }
                }
        );
    }

    public void testLongConnect() {
        initialData();
        initialListener();
    }

    private void initialListener() {
        ArrayList<String> list1 = new ArrayList<>();
        list1.add(WisePush.EVENT_DANMUKU);
        l1 = new Listener1();
        WisePush.getInstance().regeisterServerMsg(list1, l1);

        ArrayList<String> list2 = new ArrayList<>();
        list2.add(WisePush.EVENT_DANMUKU);
        list2.add(WisePush.EVENT_TEST);
        l2 = new Listener2();
        WisePush.getInstance().regeisterServerMsg(list2, l2);

        ArrayList<String> list3 = new ArrayList<>();
        list3.add(WisePush.EVENT_DANMUKU);
        list3.add(WisePush.EVENT_TEST_1);
        list3.add(WisePush.EVENT_TEST);
        list3.add(WisePush.EVENT_TEST);
        list3.add(WisePush.EVENT_TEST);
        l3 = new Listener3();
        WisePush.getInstance().regeisterServerMsg(list3, l3);

    }

    private void initialData() {
        String ip = "192.168.13.40";
        int port = 1883;
//        int port = 20081;
        int uid = 111;
        String tokenId = "11111";
        //初始化自定义WiseMqtt模块的配置,并开启长连接,
        //调用代码只可以发消息,不能注册Topic.
        WisePush.getInstance()
                .init(getApplication())
                .setCurrentApp(WisePush.WISECLASS_TEACHER)  //根据App不同,去注册相应的TOPIC,根据文档要求,下层代码没有注册TOPIC的的功能,
                .setServerIp(ip)                            //Ip
                .setServerPort(port)                        //port
                .setUserId(uid)                                //服务器返回的用户Id
                .setTokenId(tokenId)                        //tokenId
                .connect();
        startTime = System.currentTimeMillis();

    }


    class Listener1 extends OnWisePushConnectListener {

        @Override
        public void onDataReceive(String event, String json, WsReceiveData wsReceiveData) {
            Log.e(TAG, "listener1 ...key = " + event + "  json = " + json);
        }

        @Override
        public void disConnect() {
            super.disConnect();
            Log.e(TAG, "listener1 ...disConnect");
        }

        @Override
        public void onConnectFail(String s) {
            super.onConnectFail(s);
            Log.e(TAG, "listener1 ...nConnectedFail + exception" + s);

        }

        @Override
        public void connect() {
            super.connect();
            Log.e(TAG, "spend time = " + (System.currentTimeMillis() - startTime));
            Log.e(TAG, "listener1... onConnect");
        }
    }

    class Listener2 extends OnWisePushConnectListener {

        @Override
        public void onDataReceive(String event, String json, WsReceiveData wsReceiveData) {
            Log.e(TAG, "listener2 ...key = " + event + "  json = " + json);
        }
    }

    class Listener3 extends OnWisePushConnectListener {

        @Override
        public void onDataReceive(String event, String json, WsReceiveData wsReceiveData) {
            Log.e(TAG, "listener3 ...key = " + event + "  json = " + json);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (l1 != null) WisePush.getInstance().unRegeisterServerMsg(l1);
        if (l2 != null) WisePush.getInstance().unRegeisterServerMsg(l2);
        if (l3 != null) WisePush.getInstance().unRegeisterServerMsg(l3);
    }
}
