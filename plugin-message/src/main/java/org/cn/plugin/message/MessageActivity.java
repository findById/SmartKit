package org.cn.plugin.message;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.cn.plugin.message.adapter.MessageAdapter;
import org.cn.plugin.message.databinding.ActivityMessageBinding;
import org.cn.plugin.message.model.Logic;
import org.cn.plugin.message.model.LogicType;
import org.cn.plugin.message.model.Message;
import org.cn.plugin.message.model.MessageType;
import org.cn.plugin.message.service.MessageService;
import org.cn.plugin.message.utils.KeyboardUtil;
import org.cn.plugin.message.utils.OrmHelper;
import org.cn.plugin.message.utils.PermissionManager;
import org.cn.plugin.rpc.Response;
import org.cn.plugin.rpc.ResponseListener;
import org.cn.plugin.rpc.RpcEngine;
import org.cn.plugin.voice.EventHandler;
import org.cn.plugin.voice.TextToAudio;
import org.cn.plugin.voice.VoiceHandler;

import java.util.List;

public class MessageActivity extends AppCompatActivity implements VoiceHandler.OnRecognitionListener, EventHandler.OnEventSpeechListener {
    public static final String ACTION_MESSAGE = "action.iot.message.refresh.smartkit";
    public static final String EXTRA_MESSAGE_DATA = "extra.message.data";
    public static final String EXTRA_CONSUMER_DATA = "extra.consumer.data";

    private ActivityMessageBinding mBinding;

    private MessageAdapter mMessageAdapter;

    private VoiceHandler voiceHandler;
    private EventHandler eventHandler;

    public static String userId = "me";
    public String consumer;

    private boolean restartVoice = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_message);
        mBinding.setTitle("Message");

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PermissionManager.init(this);
        PermissionManager.requestPermissions(this, new PermissionManager.OnPermissionsCallback() {
            @Override
            public void onRequestPermissionsResult(boolean success, String[] permission, int[] grantResult, boolean[] showRequestRationale) {
                if (!success) {
                    new AlertDialog.Builder(MessageActivity.this)
                            .setCancelable(false)
                            .setMessage("必须打开录音和SD卡权限才能正常使用\n请到\"应用详情\" > \"权限管理\"设置")
                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getApplicationContext().startActivity(intent);
                                }
                            })
                            .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    finish();
                                }
                            })
                            .show();
                }
            }
        }, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        voiceHandler = new VoiceHandler(this);
        voiceHandler.setListener(this);

        eventHandler = new EventHandler(this);
        eventHandler.initSpeechEvent();
        eventHandler.setListener(this);
        eventHandler.startSpeechEvent();

        initView();
        initData();

        registerReceiver(receiver, new IntentFilter(ACTION_MESSAGE));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        voiceHandler.destroy();
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBinding.speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventHandler.stopSpeechEvent();
                voiceHandler.start();
            }
        });

        initInputView();
    }

    private void initData() {
        consumer = getIntent().getStringExtra(EXTRA_CONSUMER_DATA);
        if (TextUtils.isEmpty(consumer)) {
            consumer = "ESP8266";
        }
        getSupportActionBar().setSubtitle(String.format("@%s", consumer));

        mMessageAdapter = new MessageAdapter(this, mBinding.recyclerView);

        mBinding.recyclerView.setAdapter(mMessageAdapter);

        List<Message> list = OrmHelper.getInstance().query(Message.class).toList();
        if (list != null && !list.isEmpty()) {
            // mMessageAdapter.setData(list);
            for (Message msg : list) {
                handleMessage(msg, false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!showAttachmentLayout) {
            changeInputLayout(false);
            return;
        }
        super.onBackPressed();
    }

    boolean showAttachmentLayout = false;

    private void initInputView() {
        mBinding.btnSend.setImageResource(R.drawable.ic_send_grey);
        mBinding.btnSend.setEnabled(false);
        mBinding.text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mBinding.btnSend.setImageResource(R.drawable.ic_send_grey);
                    mBinding.btnSend.setEnabled(false);
                } else {
                    mBinding.btnSend.setImageResource(R.drawable.ic_send_green);
                    mBinding.btnSend.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mBinding.text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!showAttachmentLayout) {
                        changeInputLayout(true);
                    }
                }
                return false;
            }
        });

        mBinding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mBinding.text.getText().toString())) {
                    return;
                }
                sendMessage(mBinding.text.getText().toString());
                mBinding.text.setText("");
            }
        });

        changeInputLayout(false);
        mBinding.btnAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputLayout(true);
            }
        });
    }

    private void changeInputLayout(boolean showSoftInput) {
        if (showAttachmentLayout) {
            mBinding.btnAttachment.setImageResource(R.drawable.ic_keyboard);
            mBinding.attachmentLayout.setVisibility(View.VISIBLE);

            KeyboardUtil.hide(mBinding.text);
        } else {
            mBinding.btnAttachment.setImageResource(R.drawable.ic_attachment);
            mBinding.attachmentLayout.setVisibility(View.GONE);

            if (showSoftInput) {
                mBinding.text.requestFocus();
                KeyboardUtil.show(mBinding.text);
            }
        }
        showAttachmentLayout = !showAttachmentLayout;
    }

    private void sendMessage(String message) {
        if ("停".equals(message)) {
            if (mp != null && mp.isPlaying()) {
                mp.pause();
                return;
            }
        } else if ("继续".equals(message)) {
            if (mp != null && !mp.isPlaying()) {
                mp.start();
                return;
            }
        } else if ("再说一遍".equals(message)) {
            if (mp != null && !mp.isPlaying() && !TextUtils.isEmpty(lastAudio)) {
                playAudio(lastAudio);
                return;
            }
        }

        Message bean = new Message(userId, "", MessageType.TEXT, message);
        bean.read = "1";

        mMessageAdapter.add(bean);

        OrmHelper.getInstance().insert(bean);

        if ("开灯".equals(message) || "open".equals(message)) {
            MessageService.publish(this, consumer, "051");
            restartVoice = true;
            playMessage("命令已发送");
        } else if ("关灯".equals(message) || "close".equals(message)) {
            MessageService.publish(this, consumer, "050");
            restartVoice = true;
            playMessage("命令已发送");
        } else {
            JSONObject param = new JSONObject();
            param.put("key", "");
            param.put("userid", "1");
            param.put("info", message);

            RpcEngine.post("http://www.tuling123.com/openapi/api", param.toJSONString(), new ResponseListener<Response>() {
                @Override
                public void onResponse(Response response) {
                    try {
                        JSONObject obj = JSON.parseObject(response.result);
                        int code = obj.getInteger("code");
                        if (code == 100000) {
                            restartVoice = true;
                            String msg = obj.getString("text");
                            handleMessage(new Message("turing", userId, MessageType.TEXT, msg), true);
                            playMessage(msg);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void handleMessage(Message message, boolean store) {
        switch (message.msgType) {
            case MessageType.REPORT: {
                List<Logic> logicList = JSON.parseArray(message.body, Logic.class);
                if (logicList != null && !logicList.isEmpty()) {
                    for (Logic logic : logicList) {
                        if (LogicType.RELAY.value.equals(logic.type)) {
                            message.msgType = MessageType.NOTIFY;
                            message.body = logic.metadata;
                        }
                    }
                }
                return;
            }
            case MessageType.NOTIFY: {
                List<Logic> logicList = JSON.parseArray(message.body, Logic.class);
                if (logicList != null && !logicList.isEmpty()) {
                    message.body = "";
                    for (Logic logic : logicList) {
                        if (LogicType.RELAY.value.equals(logic.type)) {
                            message.msgType = MessageType.TEXT;
                            if (TextUtils.isEmpty(message.body)) {
                                message.body = String.format("P%s: %s", logic.pin, "0".equals(logic.metadata) ? "closed" : "opened");
                            } else {
                                message.body += String.format("\nP%s: %s", logic.pin, "0".equals(logic.metadata) ? "closed" : "opened");
                            }
                        } else if (LogicType.HT.value.equals(logic.type)) {
                            message.msgType = MessageType.TEXT;
                            if (logic.metadata.contains(",")) {
                                String[] temp = logic.metadata.split(",");
                                if (temp != null && temp.length > 2 && "1".equals(temp[0])) {
                                    if (TextUtils.isEmpty(message.body)) {
                                        message.body = String.format("P%s: %s°C, %s%%", logic.pin, temp[1], temp[2]);
                                    } else { // °C °F %
                                        message.body += String.format("\nP%s: %s°C, %s%%", logic.pin, temp[1], temp[2]);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
            default:
                break;
        }
        mMessageAdapter.add(message);
        if (store) {
            OrmHelper.getInstance().insert(message);
        }
    }

    private void playMessage(String text) {
        final String path = Environment.getExternalStorageDirectory() + "/audio.mp3";
        TextToAudio.send(text, path, new TextToAudio.HttpListener() {
            @Override
            public void onResult(int statusCode, String result) {
                if (statusCode != 200) {
                    TextToAudio.getToken(null);
                    return;
                }

                playAudio(path);
            }
        });
    }

    MediaPlayer mp = new MediaPlayer();
    String lastAudio;

    private void playAudio(String path) {
        try {
            lastAudio = path;
            mp.reset();
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                    if (restartVoice) {
                        restartVoice = false;
                        voiceHandler.start();
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_MESSAGE.equals(intent.getAction())) {
                Message message = (Message) intent.getSerializableExtra(EXTRA_MESSAGE_DATA);
                handleMessage(message, true);
            }
        }
    };

    @Override
    public void onEventSpeech(int statusCode, String data) {
        switch (statusCode) {
            case EventHandler.CODE_START: {
                Toast.makeText(MessageActivity.this, "语音唤醒已开启", Toast.LENGTH_SHORT).show();
                break;
            }
            case EventHandler.CODE_STOP: {
                Toast.makeText(MessageActivity.this, "语音唤醒已关闭", Toast.LENGTH_SHORT).show();
                break;
            }
            case EventHandler.CODE_DATA: {
                if ("开始工作".equals(data)) {
                    eventHandler.stopSpeechEvent();
                    voiceHandler.start();
                } else if ("停止工作".equals(data)) {
                    voiceHandler.stop();
                    eventHandler.startSpeechEvent();
                    break;
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onSpeechMessage(int statusCode, String message) {
        switch (statusCode) {
            case VoiceHandler.CODE_DATA: {
                if ("停止工作".equals(message)) {
                    voiceHandler.stop();
                    eventHandler.startSpeechEvent();

                    restartVoice = false;
                    playMessage("语音唤醒已开启");
                    break;
                }

                sendMessage(message);
                break;
            }
            case VoiceHandler.CODE_ERROR: {
                restartVoice = true;
                playMessage(message);
                break;
            }
            case VoiceHandler.CODE_ERROR_RETRY: {
                voiceHandler.start();
                break;
            }
            default:
                break;
        }
    }
}
