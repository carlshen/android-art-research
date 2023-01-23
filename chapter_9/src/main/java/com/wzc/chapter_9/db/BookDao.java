package com.wzc.chapter_9.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import android.database.Cursor;

import com.wzc.chapter_9.provider.BookStore;

@Dao
public interface BookDao {
    @Insert
    long insert(Book book);

    /**
     * 根据id删除一本书
     * @param id
     * @return
     */
    @Query("DELETE FROM " + Book.TABLE_NAME + " WHERE " + BookStore.Books._ID + " = :id")
    int deleteById(long id);

    @Update
    int update(Book book);

    /**
     * 查询所有的书
     * @return
     */
    @Query("SELECT * FROM " + Book.TABLE_NAME)
    Cursor queryAll();

    /**
     * 根据 id 来查询书
     * @param id
     * @return
     */
    @Query("SELECT * FROM " + Book.TABLE_NAME + " WHERE " + BookStore.Books._ID + " = :id")
    Cursor queryById(long id);
}
