package com.example.covidvolunteeringsite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "covid_sites.db";
    private static final int DB_VERSION = 1;

    public static final String SITE_TABLE_NAME = "Sites";
    public static final String SITE_ID = "_id";
    public static final String SITE_LEADER = "leader_id";
    public static final String SITE_NAME = "name";
    public static final String SITE_NUM_OF_TESTED = "tested_count";
    public static final String SITE_LONGITUDE = "longitude";
    public static final String SITE_LATITUDE = "latitude";

    public static final String USER_TABLE_NAME = "Users";
    public static final String USER_ID = "_id";
    public static final String USER_NAME = "name";
    public static final String USER_USERNAME = "username";
    public static final String USER_PASSWORD = "password";
    public static final String USER_IS_SUPER = "is_super";

    public static final String VOLUNTEERING_TABLE_NAME = "Volunteering";
    public static final String VOLUNTEERING_ID = "_id";
    public static final String VOLUNTEERING_USER_ID = "user_id";
    public static final String VOLUNTEERING_SITE_ID = "site_id";
    public static final String VOLUNTEERING_FRIEND_NAME = "volunteer_name";
    public static final String VOLUNTEERING_FRIEND_EMAIL = "volunteer_email";

    public static final String NOTIFICATION_TABLE_NAME = "Notifications";
    public static final String NOTIFICATION_ID = "_id";
    public static final String NOTIFICATION_RECEIVER = "receiver_id";
    public static final String NOTIFICATION_MESSAGE = "message";
    public static final String NOTIFICATION_TO_LEADER = "to_leader";


    private static final String CREATE_TABLE_USER =
            "create table " + USER_TABLE_NAME + "(" +
                    USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    USER_NAME + " VARCHAR(255)," +
                    USER_PASSWORD + " VARCHAR(255)," +
                    USER_USERNAME + " VARCHAR(255)," +
                    USER_IS_SUPER + " BOOLEAN" + ");";
    private static final String CREATE_TABLE_SITE =
            "create table " + SITE_TABLE_NAME + "(" +
                    SITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SITE_LEADER + " INTEGER," +
                    SITE_NAME + " VARCHAR(255)," +
                    SITE_LATITUDE + " DOUBLE," +
                    SITE_LONGITUDE + " DOUBLE," +
                    SITE_NUM_OF_TESTED + " INTEGER," +
                    "foreign key (" + SITE_LEADER + ") references " + USER_TABLE_NAME +
                    "(" + USER_ID + "));";
    private static final String CREATE_TABLE_VOLUNTEERING =
            "create table " + VOLUNTEERING_TABLE_NAME + "(" +
                    VOLUNTEERING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    VOLUNTEERING_USER_ID + " INTEGER," +
                    VOLUNTEERING_SITE_ID + " INTEGER," +
                    VOLUNTEERING_FRIEND_NAME + " VARCHAR(255)," +
                    VOLUNTEERING_FRIEND_EMAIL + " VARCHAR(255)," +
                    "unique (" + VOLUNTEERING_USER_ID + "," + VOLUNTEERING_SITE_ID + "," + VOLUNTEERING_FRIEND_EMAIL + ")," +
                    "foreign key (" + VOLUNTEERING_SITE_ID + ") references " + SITE_TABLE_NAME +
                    "(" + SITE_ID + ")," +
                    "foreign key (" + VOLUNTEERING_USER_ID + ") references " + USER_TABLE_NAME +
                    "(" + USER_ID + "));";

    private static final String CREATE_TABLE_NOTIFICATION =
            "create table " + NOTIFICATION_TABLE_NAME + "(" +
                    NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    NOTIFICATION_RECEIVER + " INTEGER," +
                    NOTIFICATION_MESSAGE + " VARCHAR(255)," +
                    NOTIFICATION_TO_LEADER + " BOOLEAN," +
                    "FOREIGN KEY (" + NOTIFICATION_RECEIVER + ") REFERENCES " + USER_TABLE_NAME +
                    "(" + USER_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_SITE);
        db.execSQL(CREATE_TABLE_VOLUNTEERING);
        db.execSQL(CREATE_TABLE_NOTIFICATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + VOLUNTEERING_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SITE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION_TABLE_NAME);
        onCreate(db);
    }
}
