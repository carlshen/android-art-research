package com.wzc.chapter_9.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Process;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.wzc.chapter_9.MyUtils;
import com.wzc.chapter_9.db.AppDatabase;
import com.wzc.chapter_9.db.Book;
import com.wzc.chapter_9.db.BookDao;

public class BookProvider extends ContentProvider {
    private static final String TAG = "BookProvider";
    // 匹配器
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    // 用于 books 数据表的一些条目的匹配码
    public static final int CODE_BOOK_DIR = 1;
    // 用于 books 数据表的一个条目的匹配码
    public static final int CODE_BOOK_ITEM = 2;

    static {
        MATCHER.addURI(BookStore.AUTHORITY, Book.TABLE_NAME, CODE_BOOK_DIR);
        MATCHER.addURI(BookStore.AUTHORITY, Book.TABLE_NAME + "/*", CODE_BOOK_ITEM); // *:表示匹配任意长度的任意字符
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate: processName=" + MyUtils.getProcessName(getContext(), Process.myPid()) + ", threadName=" + Thread.currentThread().getName());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.d(TAG, "query: processName=" + MyUtils.getProcessName(getContext(), Process.myPid()) + ", threadName=" + Thread.currentThread().getName());
        final int code = MATCHER.match(uri);
        if (code == CODE_BOOK_DIR || code == CODE_BOOK_ITEM) {
            Context context = getContext();
            if (context == null) {
                return null;
            }
            BookDao bookDao = AppDatabase.getInstance(context).bookDao();
            final Cursor cursor;
            if (code == CODE_BOOK_DIR) {
                cursor = bookDao.queryAll();
            } else {
                cursor = bookDao.queryById(ContentUris.parseId(uri));
            }
            cursor.setNotificationUri(context.getContentResolver(), uri);
            return cursor;
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        Log.d(TAG, "getType: processName=" + MyUtils.getProcessName(getContext(), Process.myPid()) + ", threadName=" + Thread.currentThread().getName());
        switch (MATCHER.match(uri)) {
            case CODE_BOOK_DIR:
                return "vnd.android.cursor.dir/" + BookStore.AUTHORITY + "." + Book.TABLE_NAME;
            case CODE_BOOK_ITEM:
                return "vnd.android.cursor.item/" + BookStore.AUTHORITY + "." + Book.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.d(TAG, "insert: processName=" + MyUtils.getProcessName(getContext(), Process.myPid()) + ", threadName=" + Thread.currentThread().getName());
        switch (MATCHER.match(uri)) {
            case CODE_BOOK_DIR:
                final Context context = getContext();
                if (context == null) {
                    return null;
                }
                final long rowInserted = AppDatabase.getInstance(context).bookDao()
                        .insert(Book.fromContentValues(values));
                context.getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, rowInserted);
            case CODE_BOOK_ITEM:
                throw new IllegalArgumentException("Invalid URI, cannot insert with ID: " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "delete: processName=" + MyUtils.getProcessName(getContext(), Process.myPid()) + ", threadName=" + Thread.currentThread().getName());
        switch (MATCHER.match(uri)) {
            case CODE_BOOK_DIR:
                throw new IllegalArgumentException("Invalid URI, cannot delete without ID" + uri);
            case CODE_BOOK_ITEM:
                final Context context = getContext();
                if (context == null) {
                    return 0;
                }
                long id = ContentUris.parseId(uri);
                final int rowDeleted = AppDatabase.getInstance(context).bookDao().deleteById(id);
                context.getContentResolver().notifyChange(uri, null);
                return rowDeleted;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "update: processName=" + MyUtils.getProcessName(getContext(), Process.myPid()) + ", threadName=" + Thread.currentThread().getName());
        switch (MATCHER.match(uri)) {
            case CODE_BOOK_DIR:
                throw new IllegalArgumentException("Invalid URI, cannot update without ID" + uri);
            case CODE_BOOK_ITEM:
                final Context context = getContext();
                if (context == null) {
                    return 0;
                }
                Book book = Book.fromContentValues(values);
                book.setId(ContentUris.parseId(uri));
                final int rowUpdated = AppDatabase.getInstance(context).bookDao().update(book);
                context.getContentResolver().notifyChange(uri, null);
                return rowUpdated;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}
