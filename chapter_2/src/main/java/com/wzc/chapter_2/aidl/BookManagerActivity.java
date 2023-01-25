package com.wzc.chapter_2.aidl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wzc.chapter_2.R;
import com.wzc.chapter_2.util.ToastUtils;
import com.wzc.chapter_2_common_lib.Book;
import com.wzc.chapter_2_common_lib.IBookManager;
import com.wzc.chapter_2_common_lib.IOnNewBookArrivedListener;

import java.util.List;

/**
 * @author wzc
 * @date 2018/4/4
 */
public class BookManagerActivity extends Activity implements View.OnClickListener {
    private static final String TAG = BookManagerActivity.class.getSimpleName();
    private boolean mIsBound;
    private IBookManager mBookManager;
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Book book = (Book) msg.obj;
//                    Toast.makeText(BookManagerActivity.this, "book:" + book, Toast.LENGTH_SHORT).show();
                    ToastUtils.showToast(BookManagerActivity.this, "book:" + book);
                    break;
                default:
                    break;
            }
        }
    };
    // 声明一个 DeathRecipient 对象
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        // 当 Binder 死亡的时候，系统就会回调 binderDied 方法, 回调在客户端的 Binder 线程池里面
        @Override
        public void binderDied() {
            Log.d(TAG, "binderDied: currentThread = " + Thread.currentThread().getName()); // Binder:25808_2

            if (mBookManager == null) {
                return;
            }
            // 移除之前设置的死亡代理
            mBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mBookManager = null;
            // 重新绑定远程服务
            Intent intent = new Intent(BookManagerActivity.this, BookManagerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 跨进程时，这里回调的 IBinder service 对象 （打印：android.os.BinderProxy@a4991f4）
            // 和 BookManagerService 里的 onBind 方法返回的 IBinder 对象
            // （打印：com.wzc.chapter_2.aidl.BookManagerService$1@ecea4f7）不是一个对象。
            // 不是跨进程时，这里回调的 IBinder service 对象和 BookManagerService 里的 onBind 方法返回的 IBinder 对象
            // 是一个对象。
            Log.d(TAG, "onServiceConnected: service = " + service);
            Log.d(TAG, "onServiceConnected: currentThread = " + Thread.currentThread().getName()); // main
            Toast.makeText(BookManagerActivity.this, "service is connected", Toast.LENGTH_SHORT).show();
            mBookManager = IBookManager.Stub.asInterface(service);
            // 演示在这里添加一本书，然后再获取，看是否正常
            // --------------------------start---------------------------------
//            try {
//                List<Book> bookList = mBookManager.getBookList();
//                Log.d(TAG, "onServiceConnected: query book list: " + bookList);
//                Book newBook = new Book(3, "Android 开发艺术探索");
//                mBookManager.addBook(newBook);
//                Log.d(TAG, "onServiceConnected: add book:" + newBook);
//                List<Book> newBookList = mBookManager.getBookList();
//                Log.d(TAG, "onServiceConnected: query new book list: " + bookList);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
            // -------------------------- end ---------------------------------
            try {
                // 在客户端绑定远程服务成功后，给 binder 设置死亡代理。
                mBookManager.asBinder().linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: currentThread = " + Thread.currentThread().getName()); // main
            Toast.makeText(BookManagerActivity.this, "service is disconnected", Toast.LENGTH_SHORT).show();
            mIsBound = false;
            mBookManager = null;
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            // 这个方法是在客户端的 Binder 线程池里中执行的。
            Log.d(TAG, "onNewBookArrived: thread = " + Thread.currentThread().getName()); //  Binder:3347_3 Binder:3347_1 Binder:3347_2
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget();
        }
    };
    private ListView mListview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_manager);

        findViewById(R.id.bind).setOnClickListener(this);

        findViewById(R.id.unbind).setOnClickListener(this);

        findViewById(R.id.add_book).setOnClickListener(this);

        findViewById(R.id.get_booklist).setOnClickListener(this);

        findViewById(R.id.register).setOnClickListener(this);

        findViewById(R.id.unregister).setOnClickListener(this);

        mListview = (ListView) findViewById(R.id.listview);

    }

    @Override
    protected void onDestroy() {
        if (mBookManager != null && mBookManager.asBinder().isBinderAlive()) {
            try {
                mBookManager.unregisterListener(mOnNewBookArrivedListener);
                Log.d(TAG, "onDestroy: unregisterListener " + mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (mIsBound) {
            unbindService(mConnection);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bind:
                if (!mIsBound) {
                    Intent intent = new Intent(BookManagerActivity.this, BookManagerService.class);
                    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    mIsBound = true;
                }
                break;
            case R.id.unbind:
                if (mIsBound) {
                    unbindService(mConnection);
                    mIsBound = false;
                    mBookManager = null;
                }
                break;
            case R.id.add_book:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isBookManagerReady()) {
                            Book book = new Book(3, "Android art research");
                            try {
                                mBookManager.addBook( book);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                break;

            case R.id.get_booklist:
                if (isBookManagerReady()) {
                    try {
                        List<Book> bookList = mBookManager.getBookList();
                        // 这里打印的是 ArrayList，虽然在服务端用的是 CopyOnWriteArrayList
                        Log.d(TAG, "getBookList bookList ListType = " + bookList.getClass().getCanonicalName());
                        ArrayAdapter<Book> bookArrayAdapter = new ArrayAdapter<>(BookManagerActivity.this, android.R.layout.simple_list_item_1, bookList);
                        mListview.setAdapter(bookArrayAdapter);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.register:
                if (isBookManagerReady()) {
                    try {
                        mBookManager.registerListener(mOnNewBookArrivedListener);
                        Log.d(TAG, "btn registerListener " + mOnNewBookArrivedListener);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.unregister:
                if (isBookManagerReady()) {
                    try {
                        mBookManager.unregisterListener(mOnNewBookArrivedListener);
                        Log.d(TAG, "btn unregisterListener " + mOnNewBookArrivedListener);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:

        }
    }

    private boolean isBookManagerReady() {
        if (mBookManager != null) {
            return true;
        } else {
            Toast.makeText(this, "BookManager is not available yet!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
