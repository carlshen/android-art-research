package com.wzc.chapter_2_supplement;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

/**
 * 学习地址: https://race604.com/communicate-with-remote-service-2/
 *
 * @author wangzhichao
 * @date 2018/3/15
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service connected", Toast.LENGTH_SHORT).show();
            mService = IRemoteService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service disconnected", Toast.LENGTH_SHORT).show();
            mService = null;
        }
    };

    private IParticipateCallback mParticipateCallback = new IParticipateCallback.Stub() {

        @Override
        public void onParticipate(String name, boolean joinOrLeave) throws RemoteException {
            if (joinOrLeave) {
                mAdapter.add(name);
            } else {
                mAdapter.remove(name);
            }
        }
    };
    private boolean mIsBound = false;
    private IRemoteService mService;
    private ListView mList;
    private Button mJoin;
    private Button mRegister;
    private ArrayAdapter<String> mAdapter;
    private boolean mIsJoin = false;
    private boolean mIsRegistered = false;

    private IBinder mToken = new Binder();
    private Random mRand = new Random();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bind).setOnClickListener(this);
        findViewById(R.id.unbind).setOnClickListener(this);
        findViewById(R.id.call).setOnClickListener(this);
        findViewById(R.id.get_participators).setOnClickListener(this);
        mList = (ListView) findViewById(R.id.list);
        mJoin = (Button) findViewById(R.id.join);
        mJoin.setOnClickListener(this);
        mRegister = (Button) findViewById(R.id.register_callback);
        mRegister.setOnClickListener(this);

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mList.setAdapter(mAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsBound) {
            unbindService(mServiceConnection);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bind:
                // 在Android 5.0以后，就不允许使用非特定的Intent来绑定Service了
                Intent intent = new Intent(IRemoteService.class.getName());
                intent.setClassName("com.wzc.chapter_2_supplement.service", "com.wzc.chapter_2_supplement.RemoteService");
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                mIsBound = true;
                break;
            case R.id.unbind:
                if (isServiceReady()) {
                    unbindService(mServiceConnection);
                    mService = null;
                    mIsBound = false;
                }
                break;
            case R.id.call:
                callRemote();
                break;
            case R.id.join:
                toggleJoin();
                break;
            case R.id.get_participators:
                updateParticipators();
                break;
            case R.id.register_callback:
                toggleRegisterCallback();
                break;
            default:
        }
    }

    private void callRemote() {

        if (isServiceReady()) {
            try {
                int result = mService.someOperate(1, 2);
                Toast.makeText(this, "Remote call return: " + result, Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(this, "Remote call error!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean isServiceReady() {
        if (mService != null) {
            return true;
        } else {
            Toast.makeText(this, "Service is not available yet!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    private void toggleRegisterCallback() {
        if (!isServiceReady()) {
            return;
        }

        try {
            if (mIsRegistered) {
                mService.unregisterParticipateCallback(mParticipateCallback);
                mRegister.setText(R.string.register);
                mIsRegistered = false;
            } else {
                mService.registerParticipateCallback(mParticipateCallback);
                mRegister.setText(R.string.unregister);
                mIsRegistered = true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateParticipators() {
        if (!isServiceReady()) {
            return;
        }

        try {
            List<String> participators = mService.getParticipators();
            mAdapter.clear();
            mAdapter.addAll(participators);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void toggleJoin() {
        if (!isServiceReady()) {
            return;
        }

        try {
            if (!mIsJoin) {
                String name = "Client:" + mRand.nextInt(10);
                mService.join(mToken, name);
                mJoin.setText(R.string.leave);
                mIsJoin = true;
            } else {
                mService.leave(mToken);
                mJoin.setText(R.string.join);
                mIsJoin = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
