package org.cn.plugin.voice;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;

import java.util.HashMap;

public class EventHandler {
    public static final int CODE_START = 0;
    public static final int CODE_STOP = 1;
    public static final int CODE_DATA = 2;

    private Context mContext;

    private EventManager mEventManager;

    public EventHandler(Context mContext) {
        this.mContext = mContext;
    }

    public void initSpeechEvent() {
        // 1. 创建唤醒事件管理器
        mEventManager = EventManagerFactory.create(mContext, "wp");
        // 2. 注册唤醒事件监听器
        mEventManager.registerListener(new EventListener() {
            @Override
            public void onEvent(String name, String params, byte[] data, int offset, int length) {
                try {
                    JSONObject json = JSON.parseObject(params);
                    if ("wp.data".equals(name)) { // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                        String word = json.getString("word"); // 唤醒词
                        if (listener != null) {
                            listener.onEventSpeech(EventHandler.CODE_DATA, word);
                        }
                    } else if ("wp.exit".equals(name)) {
                        // 唤醒已经停止
                        if (listener != null) {
                            listener.onEventSpeech(EventHandler.CODE_STOP, null);
                        }
                    } else if ("wp.start".equals(name)) {
                        if (listener != null) {
                            listener.onEventSpeech(EventHandler.CODE_START, null);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startSpeechEvent() {
        // 3. 通知唤醒管理器, 启动唤醒功能
        HashMap params = new HashMap();
        params.put("kws-file", "assets:///WakeUp.bin"); // 设置唤醒资源, 唤醒资源请到 http://yuyin.baidu.com/wake#m4 来评估和导出
        mEventManager.send("wp.start", JSON.toJSONString(params), null, 0, 0);
    }

    public void stopSpeechEvent() {
        // 停止唤醒监听
        mEventManager.send("wp.stop", null, null, 0, 0);
    }

    private OnEventSpeechListener listener;

    public void setListener(OnEventSpeechListener listener) {
        this.listener = listener;
    }

    public interface OnEventSpeechListener {
        void onEventSpeech(int statusCode, String data);
    }
}
