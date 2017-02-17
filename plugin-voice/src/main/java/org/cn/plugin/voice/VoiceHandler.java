package org.cn.plugin.voice;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.baidu.speech.VoiceRecognitionService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class VoiceHandler implements RecognitionListener {
    private static final String TAG = "Speech";

    private static final int EVENT_ERROR = 11;

    public static final int STATUS_None = 0;
    public static final int STATUS_WaitingReady = 2;
    public static final int STATUS_Ready = 3;
    public static final int STATUS_Speaking = 4;
    public static final int STATUS_Recognition = 5;
    private int status = STATUS_None;

    private Context mContext;

    private SpeechRecognizer mSpeechRecognizer;

    private ProgressDialog mProgressDialog;

    public VoiceHandler(Context ctx) {
        mContext = ctx;
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(ctx, new ComponentName(ctx, VoiceRecognitionService.class));
        mSpeechRecognizer.setRecognitionListener(this);
    }

    public void destroy() {
        mSpeechRecognizer.destroy();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        status = STATUS_Ready;
        mProgressDialog.setMessage("Already");
        Log.d(TAG, "准备就绪，可以开始说话");
    }

    @Override
    public void onBeginningOfSpeech() {
        status = STATUS_Speaking;
        mProgressDialog.setMessage("Speaking");
        Log.d(TAG, "检测到用户的已经开始说话");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        status = STATUS_Recognition;
        mProgressDialog.setMessage("Recognition");
        Log.d(TAG, "检测到用户的已经停止说话");
    }

    @Override
    public void onError(int error) {
        status = STATUS_None;

        boolean retry = false;
        StringBuilder sb = new StringBuilder();
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append("音频问题");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append("没有语音输入");
                retry = true;
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append("其它客户端错误");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append("权限不足");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append("网络问题");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append("没有匹配的识别结果");
                retry = true;
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append("引擎忙");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append("服务端错误");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append("连接超时");
                break;
        }
        sb.append(":" + error);
        Log.d(TAG, "识别失败：" + sb.toString());

        mProgressDialog.setMessage(sb.toString());
        if (retry) {
            start();
        }
    }

    @Override
    public void onResults(Bundle results) {
        status = STATUS_None;
        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d(TAG, "识别成功：" + Arrays.toString(nbest.toArray(new String[nbest.size()])));
        String json_res = results.getString("origin_result");
        try {
            Log.d(TAG, "origin_result=\n" + new JSONObject(json_res).toString(4));
        } catch (Exception e) {
            Log.d(TAG, "origin_result=[warning: bad json]\n" + json_res);
        }

        mProgressDialog.setMessage(nbest.get(0));
        handleMessage(nbest.get(0));
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> nbest = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (nbest.size() > 0) {
            Log.d(TAG, "----" + Arrays.toString(nbest.toArray(new String[0])));
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        switch (eventType) {
            case EVENT_ERROR:
                String reason = params.get("reason") + "";
                Log.d(TAG, "EVENT_ERROR, " + reason);
                break;
            case VoiceRecognitionService.EVENT_ENGINE_SWITCH:
                int type = params.getInt("engine_type");
                Log.d(TAG, "*引擎切换至" + (type == 0 ? "在线" : "离线"));
                break;
            default:
                break;
        }
    }

    public void start() {
        switch (status) {
            case STATUS_None:
                // start();
                break;
            case STATUS_WaitingReady:
                cancel();
                break;
            case STATUS_Ready:
                cancel();
                break;
            case STATUS_Speaking:
                stop();
                break;
            case STATUS_Recognition:
                cancel();
                break;
            default:
                break;
        }
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
        }
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog.setMessage("Beginning.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mProgressDialog.dismiss();
                switch (status) {
                    case STATUS_None:
                        // start();
                        break;
                    case STATUS_WaitingReady:
                        cancel();
                        break;
                    case STATUS_Ready:
                        cancel();
                        break;
                    case STATUS_Speaking:
                        stop();
                        break;
                    case STATUS_Recognition:
                        cancel();
                        break;
                    default:
                        break;
                }
            }
        });
        mProgressDialog.show();

        Intent intent = new Intent();
        mSpeechRecognizer.startListening(intent);
        status = STATUS_WaitingReady;
    }

    public void stop() {
        mSpeechRecognizer.stopListening();
        status = STATUS_Recognition;
    }

    public void cancel() {
        mSpeechRecognizer.cancel();
        status = STATUS_None;
    }

    private void handleMessage(String message) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (listener != null) {
            listener.handleMessage(200, message);
        }
    }

    private OnRecognitionListener listener;

    public void setListener(OnRecognitionListener listener) {
        this.listener = listener;
    }

    public interface OnRecognitionListener {
        void handleMessage(int statusCode, String message);
    }

}
