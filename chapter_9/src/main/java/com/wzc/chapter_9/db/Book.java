package com.wzc.chapter_9.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.content.ContentValues;

import com.wzc.chapter_9.provider.BookStore;

@Entity(tableName = Book.TABLE_NAME)
public class Book {
    public static final String TABLE_NAME = "books";
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = BookStore.Books._ID)
    private long id;
    @ColumnInfo(name = BookStore.Books.NAME)
    private String name;
    @ColumnInfo(name = BookStore.Books.PRICE)
    private double price;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public static Book fromContentValues(ContentValues values) {
        final Book book = new Book();
        if (values != null && values.containsKey(BookStore.Books._ID)) {
            book.setId(values.getAsLong(BookStore.Books._ID));
        }
        if (values != null && values.containsKey(BookStore.Books.NAME)) {
            book.setName(values.getAsString(BookStore.Books.NAME));
        }
        if (values != null && values.containsKey(BookStore.Books.PRICE)) {
            book.setPrice(values.getAsDouble(BookStore.Books.PRICE));
        }
        return book;
    }
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
