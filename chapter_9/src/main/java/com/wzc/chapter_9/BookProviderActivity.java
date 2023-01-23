package com.wzc.chapter_9;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.UserDictionary;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.wzc.chapter_9.provider.BookStore;

public class BookProviderActivity extends Activity {
    private static final String TAG = "BookProviderActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_provider_activity);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, BookProviderActivity.class);
        context.startActivity(starter);
    }

    public void query(View view) {
        // 要返回数据表的哪些列
        String[] projection = {
                BookStore.Books._ID,
                BookStore.Books.NAME,
                BookStore.Books.PRICE
        };
        // 选择子句
        String selectionClause = null;
        // 选择参数
        String[] selectionArgs = null;
        String sortOrder = UserDictionary.Words._ID + " ASC";
        Cursor cursor = getContentResolver().query(BookStore.Books.CONTENT_URI, projection, selectionClause, selectionArgs, sortOrder);
        if (null == cursor) {
            Log.d(TAG, "query: cursor is null");
        } else if (cursor.getCount() < 1) {
            Log.d(TAG, "query: cursor.getCount() < 1");
        } else {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(BookStore.Books._ID));
                    String name = cursor.getString(cursor.getColumnIndex(BookStore.Books.NAME));
                    double price = cursor.getDouble(cursor.getColumnIndex(BookStore.Books.PRICE));
                    Log.d(TAG, "query: id = " + id + ", name = " + name + ", price = " + price);
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public void queryById(View view) {
        // 要返回数据表的哪些列
        String[] projection = {
                BookStore.Books._ID,
                BookStore.Books.NAME,
                BookStore.Books.PRICE
        };
        // 选择子句
        String selectionClause = null;
        // 选择参数
        String[] selectionArgs = null;
        String sortOrder = UserDictionary.Words._ID + " ASC";
        Uri uri = ContentUris.withAppendedId(BookStore.Books.CONTENT_URI, newId);
        Cursor cursor = getContentResolver().query(uri, projection, selectionClause, selectionArgs, sortOrder);
        if (cursor == null) {
            Log.d(TAG, "queryById: cursor is null");
        } else if (cursor.getCount() < 1) {
            Log.d(TAG, "queryById: cursor.getCount() < 1");
        } else {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(BookStore.Books._ID));
                    String name = cursor.getString(cursor.getColumnIndex(BookStore.Books.NAME));
                    double price = cursor.getDouble(cursor.getColumnIndex(BookStore.Books.PRICE));
                    Log.d(TAG, "queryById: id = " + id + ", name = " + name + ", price = " + price);
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }
    private long newId;
    int i = 1;
    public void insert(View view) {
        ContentValues values = new ContentValues(2);
        values.put(BookStore.Books.NAME, "Android开发艺术探索第" + i++ + "版");
        values.put(BookStore.Books.PRICE, 50 + i);
        Uri uri = getContentResolver().insert(BookStore.Books.CONTENT_URI, values);
        if (uri != null) {
            long insertedId = ContentUris.parseId(uri);
            Log.d(TAG, "insert: insertedId = " + insertedId);
            newId = insertedId;
        } else {
            Log.d(TAG, "insert: uri is null.");
        }
    }

    public void update(View view) {
        Uri uri = ContentUris.withAppendedId(BookStore.Books.CONTENT_URI, newId);
        ContentValues values = new ContentValues();
        values.put(BookStore.Books.NAME, "Android开发艺术探索珍藏版");
        values.put(BookStore.Books.PRICE, 100);
        int rowCountUpdated = getContentResolver().update(uri, values, null, null);
        Log.d(TAG, "update: rowCountUpdated = " + rowCountUpdated);
    }

    public void deleteById(View view) {
        Uri uri = ContentUris.withAppendedId(BookStore.Books.CONTENT_URI, newId);
        int rowCountDeleted = getContentResolver().delete(uri, null, null);
        Log.d(TAG, "deleteById: rowCountDeleted = " + rowCountDeleted);
    }
}
