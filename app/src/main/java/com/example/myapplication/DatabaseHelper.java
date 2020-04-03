package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
// Для упрощения работы с базами данных SQLite в Android применяется класс SQLiteOpenHelper
// создаем класса-наследник от SQLiteOpenHelper
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "wordstore.db"; // название бд
    private static final int SCHEMA = 1; // версия базы данных
    static final String TABLE = "words"; // название таблицы в бд
    // названия столбцов
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ENWORD = "enword";
    public static final String COLUMN_RUWORD = "ruword";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }
// Для выполнения запросов к SQLite используется метод execSQL()
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE words (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_ENWORD
                + " TEXT, " + COLUMN_RUWORD + " TEXT);");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);
        onCreate(db);
    }
}