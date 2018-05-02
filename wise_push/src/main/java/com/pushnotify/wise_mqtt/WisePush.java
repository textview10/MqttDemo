package com.pushnotify.wise_mqtt;

import android.app.Application;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pushnotify.wise_mqtt.bean.WsSendData;
import com.pushnotify.wise_mqtt.interf.OnWisePushConnectListener;
import com.pushnotify.wise_mqtt.interf.OnWiseRequestListener;
import com.pushnotify.wise_mqtt.io.Imanager;
import com.pushnotify.wise_mqtt.io.WiseMqttAndroidConnect;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by xu.wang
 * Date on  2018/1/24 17:47:07.
 *
 * @Desc WiseClass Project 长连接接收推送 模块 的启动,功能操作类
 */

public class WisePush implements Imanager {
    private static final String TAG = "WisePush";
    /**
     * WiseClass Project 教师端Mqtt的topic关键字
     */
    public static final String APP_TEACHER_NAME = "WTeacher";

    /**
     * WiseClass Project 学生端Mqtt的topic关键字
     */
    public static final String APP_STUDENT_NAME = "WStudent";

    /**
     * Wiseclass Project 弹幕事件的关键字
     */
    public static final String EVENT_DANMUKU = "event_danmuku";
    /**
     * Wiseclass Project 测试事件的关键字
     */
    public static final String EVENT_TEST = "event_test";
    /**
     * Wiseclass Project 测试事件的关键字
     */
    public static final String EVENT_TEST_1 = "event_test_1";

    /**
     * Wiseclass Project 测试事件的关键字
     */
    public static final String EVENT_NOTIFY = "ZK/S/Message/Notification/getNotify";

    // --------当前的topic--------------------
    public static final int WISECLASS_TEACHER = 0;
    public static final int WISECLASS_STUDENT = 1;
    public static String mCurAppName = APP_TEACHER_NAME;
    // -------------------------------------
    public static String ip;
    public static int port;
    public static Handler mHandler;
    public static String mCourseCode = "";  //课程码
    public static String mTokenId;  // tokenId
    public static int mUserId;   //登录id,由服务器返回
    public static Application mApp; //当前应用的Application
    public static String mUserName; //用户名
    public static String mUserPwd;  //用户密码

    private WiseMqttAndroidConnect mWsConnect;
    private static WisePush mInstance;

    public static WisePush getInstance() {
        synchronized (WisePush.class) {
            if (mInstance == null) {
                mInstance = new WisePush();
            }
        }
        return mInstance;
    }

    private WisePush() {
        mWsConnect = new WiseMqttAndroidConnect();
    }

    /**
     * @param application 当前Application
     */
    public WisePush init(Application application) {
        mApp = application;
        mHandler = new Handler(application.getMainLooper());
        return mInstance;
    }

    /**
     * @param tokenId 设置toeknId
     * @return
     */
    public WisePush setTokenId(String tokenId) {
        mTokenId = tokenId;
        return mInstance;
    }

    /**
     * @param uid 设置用户id
     * @return
     */
    public WisePush setUserId(int uid) {
        mUserId = uid;
        return mInstance;
    }

    /**
     * @param serverIp 服务端的ip
     * @return
     */
    public WisePush setServerIp(String serverIp) {
        ip = serverIp;
        return mInstance;
    }

    /**
     * @param serverPort 服务端的port
     * @return
     */
    public WisePush setServerPort(int serverPort) {
        port = serverPort;
        return mInstance;
    }

    /**
     * 本逻辑是为了设置监听相应的TOPIC;
     *
     * @param currentApp 当前Application   ,0是教师端,1,是学生端
     * @return
     */
    public WisePush setCurrentApp(int currentApp) {
        if (currentApp == WISECLASS_TEACHER) {
            mCurAppName = WisePush.APP_TEACHER_NAME;
        } else if (currentApp == WISECLASS_STUDENT) {
            mCurAppName = WisePush.APP_STUDENT_NAME;
        }
        return mInstance;
    }

    /**
     * 设置用户名
     *
     * @param userName
     * @return
     */
    public WisePush setUserName(String userName) {
        mUserName = userName;
        return mInstance;
    }

    /**
     * 设置用户密码
     *
     * @param userPwd
     * @return
     */
    public WisePush setUserPwd(String userPwd) {
        mUserPwd = userPwd;
        return mInstance;
    }

    /**
     * 用于筛选消息,如果消息的data中存在courseCode值,并与当前courseCode值不同,则拦截消息
     *
     * @param courseCode
     * @return
     */
    public WisePush setCourseCode(String courseCode) {
        mCourseCode = courseCode;
        Log.e(TAG, "set CourseCode = " + courseCode);
        return mInstance;
    }

    @Override
    public void connect() {
        if (mWsConnect.isAlive()) {
            Log.e(TAG, "WisePush connect thread has alive");
            return;
        }
        if (mWsConnect.isConnected()) {
            Log.e(TAG, "WisePush has connected");
            return;
        }
        mWsConnect.start();
    }

    @Override
    public void disConnect() {
        if (mWsConnect == null) {
            Log.e(TAG, "Wisepush should connect first");
            return;
        }
        mWsConnect.disConnect();
    }

    /**
     * 需要订阅的模块的String
     *
     * @param lists
     * @param listener
     */
    @Override
    public void regeisterServerMsg(ArrayList<String> lists, OnWisePushConnectListener listener) {
        if (lists == null || lists.size() == 0 || listener == null) {
            Log.e(TAG, "regeister WisePush msg fail... lists == null || lists.size() == 0 || listener == null");
            return;
        }
        ArrayList<String> finalLists = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) { //检查去掉重复的key
            String key = lists.get(i);
            if (finalLists.contains(key)) {
                continue;
            }
            finalLists.add(key);
        }
        mWsConnect.regeisterServerMsg(finalLists, listener);
    }

    @Override
    public void unRegeisterServerMsg(OnWisePushConnectListener listener) {
        mWsConnect.unRegeisterServerMsg(listener);
    }

    /**
     * @param topic 需要发送的模块的toppic
     * @param json  必须是一个符合WiseClass文档的Json,否则可能会被视为无效数据
     */
    @Override
    public void sendMsg(String topic, String json) {
        if (!isConnected()) {
            Toast.makeText(mApp, "与Mqtt服务器断开连接", Toast.LENGTH_SHORT).show();
            return;
        }
        mWsConnect.sendMsg(topic, json);
    }

    /**
     * 会根据event和body拼接一个符合WiseClass文档标准的JsonString(会加上tokenId和UserId)
     *
     * @param topic  需要发送的模块的topic
     * @param action 发送的event
     * @param msg    发送的body
     */
    @Override
    public void sendMsg(String topic, String action, Object msg) {
        if (TextUtils.isEmpty(action)) {
            Log.e(TAG, "WisePush,send action == null");
            return;
        }
        sendMsg(topic, getSendBody(action, msg));
    }

    /**
     * 获得发送的消息内容
     *
     * @param action
     * @param msg
     * @return
     */
    private String getSendBody(String action, Object msg) {
        WsSendData wsSendData = new WsSendData();
        wsSendData.setAction(action);
        wsSendData.setTokenId(WisePush.mTokenId);
        wsSendData.setUserId(WisePush.mUserId);
        wsSendData.setData(msg);
        if (msg == null) {
            wsSendData.setData(new JSONObject());
            return JSONObject.toJSONString(wsSendData, true);
        }
        if (msg instanceof JSONObject) {
            return JSONObject.toJSONString(wsSendData, true);
        } else if (msg instanceof org.json.JSONObject) {
            org.json.JSONObject temp = new org.json.JSONObject();
            try {
                temp.put("userId", WisePush.mUserId);
                temp.put("tokenId", WisePush.mTokenId);
                temp.put("data", (org.json.JSONObject) msg);
                temp.put("action", action);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("CAH", "sendMsg: 异常");
            }
            return temp.toString();
        } else if (msg instanceof JSONArray) {
            return JSONObject.toJSONString(wsSendData, true);
        } else if (msg instanceof org.json.JSONArray) {
            throw new UnsupportedOperationException("unSupport data type by WisePush....");
        } else if (msg instanceof String) {
            return JSONObject.toJSONString(wsSendData);
        } else {
            throw new UnsupportedOperationException("unSupport data type by WisePush....");
        }
    }

    /**
     * 发送一个会在本次回调中回应的消息,有默认超时(8000ms)
     * 会根据注册回调的event与收到的event进行比对,如果相同,则认为是收到的消息和请求存在关系,
     * 执行完成后自动反注册...
     *
     * @param topic    发送topic
     * @param action   发送action
     * @param msg      发送内容
     * @param event    注册回调的事件
     * @param listener 注册回调
     */
    @Override
    public void sendMsg(String topic, String action, String event, Object msg, OnWiseRequestListener listener) {
        WsSendData wsSendData = new WsSendData();
        wsSendData.setAction(action);
        wsSendData.setData(msg);
        wsSendData.setTokenId(WisePush.mTokenId);
        wsSendData.setUserId(WisePush.mUserId);
        mWsConnect.sendMsgReponse(topic, event, getSendBody(action, msg), listener);
    }

    /**
     * 判断是否正在连接
     *
     * @return
     */
    @Override
    public boolean isConnected() {
        if (mWsConnect == null) {
            return false;
        }
        if (mWsConnect.isConnected()) {
            return true;
        }
        return false;
    }

}
