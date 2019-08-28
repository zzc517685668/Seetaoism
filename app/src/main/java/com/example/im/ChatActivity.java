package com.example.im;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView userInfo;
    private EditText edit;
    private Button sendTextMsg;
    private Button start;
    private Button play;
    private RecyclerView lv;
    private String user;
    private String my;
    private List<EMMessage> list;
    private ChatAdapter adapter;
    private EMMessageListener msgListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initView();
        initReceiver();
        initHistory();
    }

    private void initHistory() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                EMConversation conversation = EMClient.getInstance().chatManager().getConversation(user);
                //获取此会话的所有消息
                final List<EMMessage> messages = conversation.getAllMessages();
                //SDK初始化加载的聊天记录为20条，到顶时需要去DB里获取更多
                //获取startMsgId之前的pagesize条消息，此方法获取的messages SDK会自动存入到此会话中，APP中无需再次把获取到的messages添加到会话中
//                List<EMMessage> messages = conversation.loadMoreMsgFromDB(startMsgId, pagesize);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //收到消息
                        if (messages!=null&&messages.size()>0){
                            list.addAll(messages);
                            adapter.setList(list);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }.start();
    }

    private void initReceiver() {
        msgListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息
                if (messages!=null&&messages.size()>0){
                    list.addAll(messages);
                    adapter.setList(list);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
            }

            @Override
            public void onMessageRead(List<EMMessage> messages) {
                //收到已读回执
            }

            @Override
            public void onMessageDelivered(List<EMMessage> message) {
                //收到已送达回执
            }
            @Override
            public void onMessageRecalled(List<EMMessage> messages) {
                //消息被撤回
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                //消息状态变动
            }
        };

        new Thread(){
            @Override
            public void run() {
                super.run();
                EMClient.getInstance().chatManager().addMessageListener(msgListener);
            }
        }.start();
    }

    private void initView() {
        user = getIntent().getStringExtra("user");
        my = getIntent().getStringExtra("my");

        userInfo = (TextView) findViewById(R.id.userInfo);
        edit = (EditText) findViewById(R.id.edit);
        sendTextMsg = (Button) findViewById(R.id.sendTextMsg);
        start = (Button) findViewById(R.id.start);
        play = (Button) findViewById(R.id.play);
        lv = (RecyclerView) findViewById(R.id.lv);

        sendTextMsg.setOnClickListener(this);
        start.setOnClickListener(this);
        play.setOnClickListener(this);

        userInfo.setText(my +"正在与 ： "+user + " 聊天中。。。。");

        list = new ArrayList<>();
        adapter = new ChatAdapter(this,list,my);
        lv.setAdapter(adapter);
        lv.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String localUrl) {
                if (!TextUtils.isEmpty(localUrl)){
                    play(localUrl);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendTextMsg:
                sendTextMsg();
                break;
            case R.id.start:
                record();
                break;
            case R.id.play:
                //play(mAudioPath);
                sendAudioMsg();
                break;
        }
    }

    private void sendAudioMsg() {
        new  Thread(){
            @Override
            public void run() {
                super.run();

                //filePath为语音文件路径，length为录音时间(秒)
                final EMMessage message = EMMessage.createVoiceSendMessage(mAudioPath, (int) mDuration, user);
                //如果是群聊，设置chattype，默认是单聊
//                if (chatType == CHATTYPE_GROUP)
//                    message.setChatType(ChatType.GroupChat);
                //发送消息
                EMClient.getInstance().chatManager().sendMessage(message);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        list.add(message);
                        adapter.setList(list);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    private void play(String mAudioPath) {
        //1.创建MediaPlayer
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            //2.设置资源
            mediaPlayer.setDataSource(mAudioPath);
            //3.加载资源
            //同步加载,如果不加载ok,后面的代码不会调用
            mediaPlayer.prepare();
            //异步加载
            //mediaPlayer.prepareAsync();
            //4.播放
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private long mDuration;
    private String mAudioPath;
    private void record() {
        if (AudioUtil.isRecording){
            //开始录制
            start.setText("开始录制");
            //停止
            AudioUtil.stopRecord();
        }else {
            //停止录制
            start.setText("停止录制");
            //停止状态
            AudioUtil.startRecord(new AudioUtil.ResultCallBack() {

                @Override
                public void onSuccess(String path, long time) {
                    mAudioPath = path;
                    mDuration = time;
                }

                @Override
                public void onFail(String msg) {

                }
            });
        }
    }

    private void sendTextMsg() {

        final String editString = edit.getText().toString().trim();
        if (TextUtils.isEmpty(editString)){
            Toast.makeText(this,"正文不能为空",Toast.LENGTH_SHORT).show();
            return;
        }

        new  Thread(){
            @Override
            public void run() {
                super.run();

                //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
                final EMMessage message = EMMessage.createTxtSendMessage(editString, user);
                //如果是群聊，设置chattype，默认是单聊
//                if (chatType == CHATTYPE_GROUP)
//                    message.setChatType(ChatType.GroupChat);
                //发送消息
                EMClient.getInstance().chatManager().sendMessage(message);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        list.add(message);
                        adapter.setList(list);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //记得在不需要的时候移除listener，如在activity的onDestroy()时
        new Thread(){
            @Override
            public void run() {
                super.run();
                EMClient.getInstance().chatManager().removeMessageListener(msgListener);
            }
        }.start();
    }
}
