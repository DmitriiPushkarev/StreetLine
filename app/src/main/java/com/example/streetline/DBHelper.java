package com.example.streetline;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "contactDb";

    public static final String DATABASE_USERS = "user_table";
    public static final String DATABASE_AREAS = "area_table";
    public static final String DATABASE_RATES = "rating_table";

    public static final String USER_ID = "user_id";
    public static final String USER_LOGIN = "user_login";
    public static final String USER_PASSWORD = "user_password";

    public static final String AREA_ID = "area_id";
    public static final String AREA_JSON = "area_json";

    public static final String RATING_ID = "rating_id";
    public static final String SCORE = "score";
    public static final String AREA_TYPE = "area_type";
    public static final String FEEDBACK = "feedback";


    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DATABASE_USERS + "(" +
                USER_ID + " text primary key ," +
                USER_LOGIN + " text unique, " +
                USER_PASSWORD + " text" + ")");

        db.execSQL("create table " + DATABASE_AREAS + "(" +
                AREA_ID + " text primary key, " +
                AREA_JSON + " text, " +
                USER_ID + " text, " +
                " foreign key " + "("+USER_ID+")"+ " references "+
                DATABASE_USERS + "("+USER_ID+")"+ " on delete cascade " +
                ")");

        db.execSQL("create table " + DATABASE_RATES + "(" +
                RATING_ID + " serial primary key, " +
                AREA_ID + " text, " +
                USER_ID + " text, " +
                SCORE + " integer, " +
                AREA_TYPE + " text, " +
                FEEDBACK + " text, " +
                " foreign key " + "("+AREA_ID+")"+ " references "+
                DATABASE_USERS + "("+AREA_ID+")"+ " on delete cascade, " +
                " foreign key " + "("+USER_ID+")"+ " references "+
                DATABASE_USERS + "("+USER_ID+")"+ " on delete cascade " +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
//        db.execSQL("drop table if exists " + DATABASE_USERS);
//        db.execSQL("drop table if exists " + DATABASE_AREAS);
//        db.execSQL("drop table if exists " + DATABASE_RATES);
//
//        onCreate(db);
    }

    public void dropALl(SQLiteDatabase db){
        db.execSQL("drop table if exists " + DATABASE_USERS);
        db.execSQL("drop table if exists " + DATABASE_AREAS);
        db.execSQL("drop table if exists " + DATABASE_RATES);
    }
}
