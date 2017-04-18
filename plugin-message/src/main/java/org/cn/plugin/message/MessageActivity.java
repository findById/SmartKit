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
import android.os.Handler;
import android.os.Looper;
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

import com.alibaba.fastjson.JSON;

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
import org.cn.plugin.voice.TextToAudio;
import org.cn.plugin.voice.VoiceHandler;

import java.util.List;

public class MessageActivity extends AppCompatActivity {
    public static final String ACTION_MESSAGE = "action.iot.message.refresh.smartkit";
    public static final String EXTRA_MESSAGE_DATA = "extra.message.data";
    public static final String EXTRA_CONSUMER_DATA = "extra.consumer.data";

    private ActivityMessageBinding mBinding;

    private MessageAdapter mMessageAdapter;

    private VoiceHandler handler;

    public static String userId = "me";
    public String consumer;

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

        handler = new VoiceHandler(this);

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
        handler.destroy();
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
                handler.start();
            }
        });

        handler.setListener(new VoiceHandler.OnRecognitionListener() {
            @Override
            public void handleMessage(int statusCode, String message) {
                sendMessage(message);
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
            mMessageAdapter.setData(list);
        }
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
        } else if ("关灯".equals(message) || "close".equals(message)) {
            MessageService.publish(this, consumer, "050");
        }

//        JSONObject param = new JSONObject();
//        param.put("userId", MessageActivity.userId);
//        param.put("content", message);
//        param.put("timestamp", System.currentTimeMillis());
//        RpcEngine.post(MessageConst.API_HOST + "/iot/message", param.toString(), new ResponseListener<Response>() {
//            @Override
//            public void onResponse(final Response response) {
//                try {
//                    JSONObject obj = JSON.parseObject(response.result);
//                    int statusCode = obj.getInteger("statusCode");
//                    if (statusCode != 200) {
//                        handleMessage(new Message("sys", "", MessageType.NOTIFY, obj.getString("message")));
//                        return;
//                    }
//                    JSONObject result = obj.getJSONObject("result");
//
//                    String msgType = result.getString("msgType");
//                    String msg = result.getString("content");
//
//                    handleMessage(new Message("ai", userId, msgType, msg));
//
//                    if (MessageType.TEXT.equals(msgType)) {
//                        playMessage(msg);
//                    }
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }
//            }
//        });

    }

    private void handleMessage(Message message) {
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
                    for (Logic logic : logicList) {
                        if (LogicType.RELAY.value.equals(logic.type)) {
                            message.msgType = MessageType.TEXT;
                            message.body = logic.metadata;
                            break;
                        }
                    }
                }
                break;
            }
            default:
                break;
        }
        mMessageAdapter.add(message);
        OrmHelper.getInstance().insert(message);
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

    private Handler mHandler = new Handler(Looper.getMainLooper());
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
                handleMessage(message);
            }
        }
    };
}
