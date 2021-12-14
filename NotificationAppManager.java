package com.example.covidvolunteeringsite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class NotificationAppManager {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public NotificationAppManager(Context context) {
        this.context = context;
    }
    public NotificationAppManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    public void addNotification(int receiver_id, String message, boolean toLeader){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.NOTIFICATION_RECEIVER, receiver_id);
        contentValues.put(DatabaseHelper.NOTIFICATION_MESSAGE, message);
        contentValues.put(DatabaseHelper.NOTIFICATION_TO_LEADER, toLeader);
        database.insert(DatabaseHelper.NOTIFICATION_TABLE_NAME, null, contentValues);
    }

    public void deleteNotification(int id){
        database.delete(DatabaseHelper.NOTIFICATION_TABLE_NAME, DatabaseHelper.NOTIFICATION_ID + " = " + id, null);
    }

    public Cursor getNotificationOfLeader(int user_id){
        String[] columns = new String[]{
                DatabaseHelper.NOTIFICATION_ID,
                DatabaseHelper.NOTIFICATION_RECEIVER,
                DatabaseHelper.NOTIFICATION_MESSAGE,
                DatabaseHelper.NOTIFICATION_TO_LEADER
        };

        Cursor cursor = database.query(DatabaseHelper.NOTIFICATION_TABLE_NAME, columns,
                DatabaseHelper.NOTIFICATION_RECEIVER + " = " + user_id + " and " +
                        DatabaseHelper.NOTIFICATION_TO_LEADER + " = 1;",
                null, null, null, null );
        if(cursor != null) cursor.moveToFirst();
        return cursor;
    }

    public Cursor getNotificationOfVolunteer(int user_id){
        String[] columns = new String[]{
                DatabaseHelper.NOTIFICATION_ID,
                DatabaseHelper.NOTIFICATION_RECEIVER,
                DatabaseHelper.NOTIFICATION_MESSAGE,
                DatabaseHelper.NOTIFICATION_TO_LEADER
        };

        Cursor cursor = database.query(DatabaseHelper.NOTIFICATION_TABLE_NAME, columns,
                DatabaseHelper.NOTIFICATION_RECEIVER + " = " + user_id + " and " +
                        DatabaseHelper.NOTIFICATION_TO_LEADER + " = 0;",
                null, null, null, null );
        if(cursor != null) cursor.moveToFirst();
        return cursor;
    }


}
