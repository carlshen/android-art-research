package com.wzc.chapter_2_supplement;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wzc
 * @date 2018/3/15
 */
public class RemoteService extends Service {
    private static final String TAG = RemoteService.class.getSimpleName();
    private List<Client> mClients = new ArrayList<>();
    private RemoteCallbackList<IParticipateCallback> mCallbacks = new RemoteCallbackList<>();
    private IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        @Override
        public int someOperate(int a, int b) throws RemoteException {
            Log.d(TAG, "called RemoteService someOperate()");
            return a + b;
        }
        // 加入游戏
        @Override
        public void join(IBinder token, String username) throws RemoteException {
            int idx = findClient(token);
            if (idx > 0) {
                Log.d(TAG, "already joined");
                return;
            }

            Client client = new Client(token, username);

            // 注册客户端死掉的通知
            token.linkToDeath(client, 0);
            mClients.add(client);

            // 通知client加入
            notifyParticipate(username, true);
        }
        // 退出游戏
        @Override
        public void leave(IBinder token) throws RemoteException {
            int idx = findClient(token);
            if (idx < 0) {
                Log.d(TAG, "already leaved");
                return;
            }

            Client client = mClients.get(idx);
            mClients.remove(client);

            // 取消注册
            client.mToken.unlinkToDeath(client, 0);

            // 通知client离开
            notifyParticipate(client.mName, false);
        }
        // 获取当前参与进来的成员列表
        @Override
        public List<String> getParticipators() throws RemoteException {
            ArrayList<String> names = new ArrayList<>();
            for (int i = 0; i < mClients.size(); i++) {
                names.add(mClients.get(i).mName);
            }
            return names;
        }

        @Override
        public void registerParticipateCallback(IParticipateCallback cb) throws RemoteException {
            mCallbacks.register(cb);
        }

        @Override
        public void unregisterParticipateCallback(IParticipateCallback cb) throws RemoteException {
            mCallbacks.unregister(cb);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消所有的回调
        mCallbacks.kill();
    }

    // 这个类用来保存Client的信息
    // 让Client实现IBinder.DeathRecipient接口的原因是每个IBinder都要注册一个IBinder.DeathRecipient的回调
    private final class Client implements IBinder.DeathRecipient {
        public final IBinder mToken; // 作为客户端的唯一标识
        public final String mName;

        public Client(IBinder token, String name) {
            mToken = token;
            mName = name;
        }

        @Override
        public void binderDied() {
            // 当客户端死掉,执行此回调
            int index = mClients.indexOf(this); // 获取当前对象在列表中的索引
            if (index < 0) {
                return;
            }
            Log.d(TAG, "client died : " + mName);
            mClients.remove(this);

            // 通知client离开
            notifyParticipate(mName, false);
        }
    }

    private void notifyParticipate(String name, boolean joinOrLeave) {
        // 循环开始前，使用mCallbacks.beginBroadcast();，来准备开始通知Callbacks，此函数返回值是mCallbacks中回调对象个数.
        final int len = mCallbacks.beginBroadcast();
        for (int i = 0; i < len; i++) {
            // 通知回调
            try {
                mCallbacks.getBroadcastItem(i).onParticipate(name, joinOrLeave);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        // 循环结束的时候，调用mCallbacks.finishBroadcast();来宣告完成.
        mCallbacks.finishBroadcast();
    }

    /**
     * 根据token,从列表中找到对应client的索引
     *
     * @param token
     * @return
     */
    private int findClient(IBinder token) {
        for (int i = 0; i < mClients.size(); i++) {
            if (mClients.get(i).mToken == token) {
                return i;
            }
        }
        return -1;
    }
}
