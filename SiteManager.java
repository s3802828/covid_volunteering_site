package com.example.covidvolunteeringsite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.google.android.gms.games.internal.player.StockProfileImageRef;

public class SiteManager {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public SiteManager(Context context) {
        this.context = context;
    }
    public SiteManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    public void addSite(String name, double latitude, double longitude, int leader_id){
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.SITE_NAME, name);
        contentValue.put(DatabaseHelper.SITE_LATITUDE, latitude);
        contentValue.put(DatabaseHelper.SITE_LONGITUDE, longitude);
        contentValue.put(DatabaseHelper.SITE_LEADER, leader_id);
        database.insert(DatabaseHelper.SITE_TABLE_NAME, null, contentValue);
    }
    public Cursor getAllSite(){
        String[] columns = new String[]{
                DatabaseHelper.SITE_ID,
                DatabaseHelper.SITE_NAME,
                DatabaseHelper.SITE_LEADER,
                DatabaseHelper.SITE_LONGITUDE,
                DatabaseHelper.SITE_LATITUDE,
                DatabaseHelper.SITE_NUM_OF_TESTED
        };
        Cursor cursor = database.query(DatabaseHelper.SITE_TABLE_NAME, columns,
                null, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }
    public Cursor getOneSite(int id){
        String[] columns = new String[]{
                DatabaseHelper.SITE_ID,
                DatabaseHelper.SITE_NAME,
                DatabaseHelper.SITE_LEADER,
                DatabaseHelper.SITE_LONGITUDE,
                DatabaseHelper.SITE_LATITUDE,
                DatabaseHelper.SITE_NUM_OF_TESTED
        };
        Cursor cursor = database.query(DatabaseHelper.SITE_TABLE_NAME, columns,
                DatabaseHelper.SITE_ID + " =" + id,
                null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getSiteByDistance(double southWestLat, double northEastLat,
                                    double southWestLng, double northEastLng){
        String[] columns = new String[]{
                DatabaseHelper.SITE_ID,
                DatabaseHelper.SITE_NAME,
                DatabaseHelper.SITE_LEADER,
                DatabaseHelper.SITE_LONGITUDE,
                DatabaseHelper.SITE_LATITUDE,
                DatabaseHelper.SITE_NUM_OF_TESTED
        };
        Cursor cursor;
        if(southWestLng <= northEastLng){
            cursor = database.query(DatabaseHelper.SITE_TABLE_NAME, columns,
                    DatabaseHelper.SITE_LATITUDE + " >= " + southWestLat + " and " +
                    DatabaseHelper.SITE_LATITUDE + " <= " + northEastLat + " and " +
                    DatabaseHelper.SITE_LONGITUDE + " >= " + southWestLng + " and " +
                    DatabaseHelper.SITE_LONGITUDE + " <= " + northEastLng,
                    null, null, null, null );
        } else {
            cursor = database.query(DatabaseHelper.SITE_TABLE_NAME, columns,
                    DatabaseHelper.SITE_LATITUDE + " >= " + southWestLat + " and " +
                            DatabaseHelper.SITE_LATITUDE + " <= " + northEastLat + " and ((" +
                            DatabaseHelper.SITE_LONGITUDE + " >= " + southWestLng + " and " +
                            DatabaseHelper.SITE_LONGITUDE + " < 180) or (" +
                            DatabaseHelper.SITE_LONGITUDE + " >= -180 and " +
                            DatabaseHelper.SITE_LONGITUDE + " <= " + northEastLng + "))",
                    null, null, null, null );
        }
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllVolunteerInOneSite(int id){
        String[] columns = new String[]{
                DatabaseHelper.VOLUNTEERING_USER_ID
        };
        Cursor cursor = database.query(DatabaseHelper.VOLUNTEERING_TABLE_NAME, columns,
                DatabaseHelper.VOLUNTEERING_SITE_ID + " =" + id, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getJoinedSitesOfOneUser(int user_id){
        String[] columns = new String[]{
                DatabaseHelper.VOLUNTEERING_SITE_ID
        };
        Cursor cursor = database.query(DatabaseHelper.VOLUNTEERING_TABLE_NAME, columns,
                DatabaseHelper.VOLUNTEERING_USER_ID + " =" + user_id, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void addNumOfTestedPeople (int id, int numOfTestedPeople){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.SITE_NUM_OF_TESTED, numOfTestedPeople);
        database.update(DatabaseHelper.SITE_TABLE_NAME, contentValues,
                DatabaseHelper.SITE_ID + " =" + id, null);
    }

    public void addVolunteering(int site_id, int user_id){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.VOLUNTEERING_SITE_ID, site_id);
        contentValues.put(DatabaseHelper.VOLUNTEERING_USER_ID, user_id);
        database.insert(DatabaseHelper.VOLUNTEERING_TABLE_NAME, null, contentValues);
    }
}
