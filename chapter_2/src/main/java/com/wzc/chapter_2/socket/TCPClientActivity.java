package com.wzc.chapter_2.socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wzc.chapter_2.R;
import com.wzc.chapter_2.util.MyUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;

/**
 * @author wzc
 * @date 2018/5/4
 */
public class TCPClientActivity extends Activity implements View.OnClickListener {
    private static final String TAG = TCPClientActivity.class.getSimpleName();
    private static final int MSG_RECEIVE_NEW_MSG = 1;
    private static final int MSG_SOCKET_CONNECTED = 2;
    private TextView mTvMsgContainer;
    private EditText mEtMsg;
    private Button mBtnConnect;
    private Button mBtnSend;
    private Socket mClientSocket;
    private PrintWriter mPrintWriter;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_RECEIVE_NEW_MSG:
                    mTvMsgContainer.setText(mTvMsgContainer.getText() + ((String) msg.obj));
                    break;
                case MSG_SOCKET_CONNECTED:
                    mBtnSend.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_client);
        // 消息内容
        mTvMsgContainer = (TextView) findViewById(R.id.msg_container);
        // 消息编辑框
        mEtMsg = (EditText) findViewById(R.id.msg);
        // 发送按钮
        mBtnSend = (Button) findViewById(R.id.send);
        mBtnSend.setOnClickListener(this);
        mBtnConnect = (Button) findViewById(R.id.connect);
        mBtnConnect.setOnClickListener(this);

//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName("com.wzc.chapter_2_socket_server", "com.wzc.chapter_2_socket_server.TCPServerService"));
////        startService(intent);
//        // 这里改为 bindService，修复在 Android 8.0 启动后台service 出错 IllegalStateException: Not allowed to start service Intent
//        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                connectTCPServer();
//            }
//        }).start();
    }

    @Override
    protected void onDestroy() {

        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    private void connectTCPServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                // 客户端创建一个Socket实例
                socket = new Socket("localhost", 8688);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mHandler.sendEmptyMessage(MSG_SOCKET_CONNECTED);
                Log.d(TAG, "connectTCPServer: " + "connect server success");
            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
                Log.d(TAG, "connectTCPServer: " + "connect tcp server failed, retry...");
            }
        }

        // 接收服务端的消息
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!TCPClientActivity.this.isFinishing()) {
                String msg = bufferedReader.readLine();
                Log.d(TAG, "connectTCPServer: " + "receive: " + msg);
                if (msg != null) {
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showMsg = "server " + time + ": " + msg + "\n";
                    mHandler.obtainMessage(MSG_RECEIVE_NEW_MSG, showMsg).sendToTarget();
                }
            }
            Log.d(TAG, "connectTCPServer: " + "quit...");
            MyUtils.closeQuietly(bufferedReader);
            MyUtils.closeQuietly(mPrintWriter);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDateTime(long l) {
        return new SimpleDateFormat("HH:mm:ss").format(l);
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnSend) {
            final String msg = mEtMsg.getText().toString();
            if (!TextUtils.isEmpty(msg) && mPrintWriter != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mPrintWriter.println(msg);
                    }
                }).start();
                mEtMsg.setText("");
                String time = formatDateTime(System.currentTimeMillis());
                final String showMsg = "self " + time + ": " + msg + "\n";
                mTvMsgContainer.setText(mTvMsgContainer.getText() + showMsg);
            }
        } else if (v == mBtnConnect) {
            Log.d(TAG, "connectTCPServer: bindService ...");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.wzc.chapter_2_socket_server", "com.wzc.chapter_2_socket_server.TCPServerService"));
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connectTCPServer();
                }
            }).start();
        }
    }
}
