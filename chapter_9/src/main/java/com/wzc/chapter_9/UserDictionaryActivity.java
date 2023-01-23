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

import java.util.Random;

public class UserDictionaryActivity extends Activity {

    private static final String TAG = "UserDictionary";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dictionary_activity);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, UserDictionaryActivity.class);
        context.startActivity(starter);
    }

    public void query(View view) {
        // 要返回数据表的哪些列
        String[] projection = {
                UserDictionary.Words._ID,
                UserDictionary.Words.WORD,
                UserDictionary.Words.LOCALE,
                UserDictionary.Words.FREQUENCY,
                UserDictionary.Words.APP_ID
        };
        // 选择子句
        String selectionClause = null;
        // 选择参数
        String[] selectionArgs = null;
        String sortOrder = UserDictionary.Words.WORD + " ASC";
        Cursor cursor = getContentResolver().query(UserDictionary.Words.CONTENT_URI, projection, selectionClause, selectionArgs, sortOrder);
        if (null == cursor) {
            Log.d(TAG, "queryUserDictionary: cursor is null");
        } else if (cursor.getCount() < 1) {
            Log.d(TAG, "queryUserDictionary: cursor.getCount() < 1");
        } else {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(UserDictionary.Words._ID));
                    String word = cursor.getString(cursor.getColumnIndex(UserDictionary.Words.WORD));
                    String locale = cursor.getString(cursor.getColumnIndex(UserDictionary.Words.LOCALE));
                    int frequency = cursor.getInt(cursor.getColumnIndex(UserDictionary.Words.FREQUENCY));
                    String appid = cursor.getString(cursor.getColumnIndex(UserDictionary.Words.APP_ID));
                    Log.d(TAG, "queryUserDictionary: id = " + id + ", word = " + word + ", locale = " + locale
                            + ", frequency = " + frequency + ", appid = " + appid);
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public void insert(View view) {
        ContentValues values = new ContentValues();
        values.put(UserDictionary.Words.APP_ID, new Random().nextInt(10));
        values.put(UserDictionary.Words.LOCALE, "en_US");
        values.put(UserDictionary.Words.WORD, "willway" + System.currentTimeMillis() / 1000 );
        values.put(UserDictionary.Words.FREQUENCY, new Random().nextInt(255));

        Uri uri = getContentResolver().insert(UserDictionary.Words.CONTENT_URI, values);
        Log.d(TAG, "insert: uri = " + uri);
        if (uri != null) {
            long id = ContentUris.parseId(uri);
            Log.d(TAG, "insert: id = " + id);
        }
    }

    public void update(View view) {
        ContentValues values = new ContentValues();
        values.put(UserDictionary.Words.FREQUENCY, 255);
        String selectionClause = UserDictionary.Words.FREQUENCY + " < ?";
        String[] selectionArgs = {"100"};
        int rowUpdated = getContentResolver().update(UserDictionary.Words.CONTENT_URI, values, selectionClause, selectionArgs);
        Log.d(TAG, "update: rowUpdated = " + rowUpdated);
    }

    public void delete(View view) {
        String selectionClause = UserDictionary.Words.FREQUENCY + " > ?";
        String[] selectionArgs = {"200"};
        int rowDeleted = getContentResolver().delete(UserDictionary.Words.CONTENT_URI, selectionClause, selectionArgs);
        Log.d(TAG, "delete: rowDeleted = " + rowDeleted);
    }
}
