package com.example.im;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView my;
    private RecyclerView lv;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        initPer();
        initView();
        initUser();
    }

    private void initPer() {
        String[] arr = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };

        ActivityCompat.requestPermissions(this, arr, 1);
    }


    private void initView() {
        my = (TextView) findViewById(R.id.my);
        lv = (RecyclerView) findViewById(R.id.lv);

//        List<String> usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
        List<String> list = new ArrayList<>();
        list.add("zzc1");
        list.add("zzc2");
        list.add("zzc3");


        adapter = new UserAdapter(this,list);
        lv.setAdapter(adapter);
        lv.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClick(new UserAdapter.onItemClick() {
            @Override
            public void onItem(int position, String str) {

                SharedPreferences sp = getSharedPreferences("H1812B", Context.MODE_PRIVATE);
                String name = sp.getString("name", "");
                if (!name.equals(str)){
                    Intent intent = new Intent(MainActivity.this,ChatActivity.class);
                    intent.putExtra("user",str);
                    intent.putExtra("my",name);
                    startActivity(intent);
                }else {
                    Toast.makeText(MainActivity.this,"不能喝自己聊天",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1,100,100,"登录");
        menu.add(1,200,100,"注销");
        menu.add(1,300,100,"群聊");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 100:
                login();
                break;
            case 200:
                logout();
                break;
            case 300:
                gotoChatGroup();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 群聊
     */
    private void gotoChatGroup() {

    }

    /**
     * 登出
     */
    private void logout() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                EMClient.getInstance().logout(true, new EMCallBack() {

                    @Override
                    public void onSuccess() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"注销成功",Toast.LENGTH_SHORT).show();
                                //情况用户信息
                                clearUser();
                                //展示刷新数据
                                initUser();
                            }
                        });
                    }

                    @Override
                    public void onProgress(int progress, String status) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onError(int code, String message) {
                        // TODO Auto-generated method stub
                    }
                });
            }
        }.start();
    }


    /**
     * 登录
     */
    private void login() {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getLoginMsg(User user){
        if (user!=null){
            //保存用户信息
            saveUser(user);
            //展示刷新数据
            initUser();
        }
    }

    //回显刷新用户信息
    private void initUser() {
        SharedPreferences sp = getSharedPreferences("H1810A", Context.MODE_PRIVATE);
        String name = sp.getString("name", "");
        String psw = sp.getString("psw", "");
        if (!TextUtils.isEmpty(name)){
            my.setText("当前用户名为："+ name+",密码为:" + psw);
        }else{
            my.setText("");
        }
    }

    //清空用户信息
    private void clearUser() {
        saveUser(new User("",""));
    }

    //保存用户信息
    private void saveUser(User user) {
        SharedPreferences sp = getSharedPreferences("H1810A", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("name",user.getName());
        edit.putString("psw",user.getPsw());
        edit.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
}
