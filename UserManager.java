package com.example.covidvolunteeringsite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class UserManager {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public UserManager(Context c){
        context =  c;
    }

    public UserManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    public void addUser(String name, String username, String password){
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.USER_NAME, name);
        contentValue.put(DatabaseHelper.USER_USERNAME, username);
        contentValue.put(DatabaseHelper.USER_PASSWORD, password);
        contentValue.put(DatabaseHelper.USER_IS_SUPER, false);
        database.insert(DatabaseHelper.USER_TABLE_NAME, null, contentValue);
    }

    public Cursor getOneUser(int id){
        String[] columns = new String[]{
                DatabaseHelper.USER_NAME
        };
        Cursor cursor = database.query(DatabaseHelper.USER_TABLE_NAME, columns,
                DatabaseHelper.USER_ID + " =" + id,
                null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }
}
