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
        Cursor cursor = database.rawQuery("SELECT " + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_ID+ "," + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_NAME + " AS get_site_name," + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_LONGITUDE + "," + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_LATITUDE + "," + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_NUM_OF_TESTED + "," + DatabaseHelper.USER_TABLE_NAME + "." +
                DatabaseHelper.USER_NAME +" FROM " + DatabaseHelper.SITE_TABLE_NAME + " INNER JOIN "
                + DatabaseHelper.USER_TABLE_NAME + " ON " + DatabaseHelper.SITE_TABLE_NAME
                +"." + DatabaseHelper.SITE_LEADER + " = " + DatabaseHelper.USER_TABLE_NAME
                +"." + DatabaseHelper.USER_ID, null);
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

    public Cursor getSitesByName(String pattern){
        Cursor cursor = database.rawQuery("SELECT " + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_ID+ "," + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_NAME + " AS get_site_name," + DatabaseHelper.USER_TABLE_NAME + "." +
                DatabaseHelper.USER_NAME +" FROM " + DatabaseHelper.SITE_TABLE_NAME + " INNER JOIN "
        + DatabaseHelper.USER_TABLE_NAME + " ON " + DatabaseHelper.SITE_TABLE_NAME
                +"." + DatabaseHelper.SITE_LEADER + " = " + DatabaseHelper.USER_TABLE_NAME
                +"." + DatabaseHelper.USER_ID + " WHERE " + DatabaseHelper.SITE_TABLE_NAME + "."
                + DatabaseHelper.SITE_NAME + " LIKE '%" + pattern + "%'", null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getSitesByLeaderName(String pattern){
        Cursor cursor = database.rawQuery("SELECT " + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_ID+ "," + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_NAME + " AS get_site_name," + DatabaseHelper.USER_TABLE_NAME + "." +
                DatabaseHelper.USER_NAME +" FROM " + DatabaseHelper.SITE_TABLE_NAME + " INNER JOIN "
                + DatabaseHelper.USER_TABLE_NAME + " ON " + DatabaseHelper.SITE_TABLE_NAME
                +"." + DatabaseHelper.SITE_LEADER + " = " + DatabaseHelper.USER_TABLE_NAME
                +"." + DatabaseHelper.USER_ID + " WHERE " + DatabaseHelper.USER_TABLE_NAME + "."
                + DatabaseHelper.USER_NAME + " LIKE '%" + pattern + "%'", null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }


    public void updateSite (int id, String name, double latitude, double longitude, int numOfTestedPeople){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.SITE_NAME, name);
        contentValues.put(DatabaseHelper.SITE_LATITUDE, latitude);
        contentValues.put(DatabaseHelper.SITE_LONGITUDE, longitude);
        contentValues.put(DatabaseHelper.SITE_NUM_OF_TESTED, numOfTestedPeople);
        database.update(DatabaseHelper.SITE_TABLE_NAME, contentValues,
                DatabaseHelper.SITE_ID + " =" + id, null);
    }

    public boolean checkExistPosition(int site_id, double longitude, double latitude){
        String[] columns = new String[]{
                DatabaseHelper.SITE_ID
        };
        Cursor cursor = database.query(DatabaseHelper.SITE_TABLE_NAME, columns,
                DatabaseHelper.SITE_LATITUDE + " = " + latitude + " and " +
                        DatabaseHelper.SITE_LONGITUDE + " = " + longitude + " and " +
                DatabaseHelper.SITE_ID + " != " + site_id,
                null,null, null, null);
        return cursor.getCount() > 0;
    }

    public Cursor getLeadSites(int leader_id){
        String[] columns = new String[]{
                DatabaseHelper.SITE_ID,
                DatabaseHelper.SITE_NAME,
                DatabaseHelper.SITE_LEADER,
                DatabaseHelper.SITE_LONGITUDE,
                DatabaseHelper.SITE_LATITUDE,
                DatabaseHelper.SITE_NUM_OF_TESTED
        };
        Cursor cursor = database.query(DatabaseHelper.SITE_TABLE_NAME, columns,
                DatabaseHelper.SITE_LEADER + " = " + leader_id, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllVolunteerInOneSite(int id){
        String[] columns = new String[]{
                DatabaseHelper.VOLUNTEERING_ID,
                DatabaseHelper.VOLUNTEERING_FRIEND_NAME,
                DatabaseHelper.VOLUNTEERING_FRIEND_EMAIL
        };
        Cursor cursor = database.query(DatabaseHelper.VOLUNTEERING_TABLE_NAME, columns,
                DatabaseHelper.VOLUNTEERING_SITE_ID + " =" + id, null, null, null, null);

        Cursor cursor1 = database.rawQuery("SELECT * FROM " + DatabaseHelper.USER_TABLE_NAME
                + " INNER JOIN " + DatabaseHelper.VOLUNTEERING_TABLE_NAME + " ON " + DatabaseHelper.USER_TABLE_NAME
        + "." + DatabaseHelper.USER_ID + " = " + DatabaseHelper.VOLUNTEERING_TABLE_NAME + "." + DatabaseHelper.VOLUNTEERING_USER_ID
            +    " WHERE " + DatabaseHelper.VOLUNTEERING_TABLE_NAME + "."
                + DatabaseHelper.VOLUNTEERING_SITE_ID + " = " + id, null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllVolunteerAccountInOneSite(int id){
        String[] columns = new String[]{
                DatabaseHelper.VOLUNTEERING_USER_ID
        };
        Cursor cursor = database.query(true, DatabaseHelper.VOLUNTEERING_TABLE_NAME, columns,
                DatabaseHelper.VOLUNTEERING_SITE_ID + " = " + id, null,
                null, null, null, null);
        if(cursor != null) cursor.moveToFirst();
        return cursor;
    }

    public Cursor getJoinedSitesOfOneUser(int user_id){
        Cursor cursor1 = database.rawQuery("SELECT DISTINCT " + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_ID+ "," + DatabaseHelper.SITE_TABLE_NAME + "." +
                DatabaseHelper.SITE_NAME + " AS get_site_name," + DatabaseHelper.USER_TABLE_NAME + "." +
                DatabaseHelper.USER_NAME + " FROM " + DatabaseHelper.USER_TABLE_NAME + "," + DatabaseHelper.SITE_TABLE_NAME
                + " INNER JOIN " + DatabaseHelper.VOLUNTEERING_TABLE_NAME + " ON " +
                DatabaseHelper.SITE_TABLE_NAME + "." + DatabaseHelper.SITE_ID + " = " +
                DatabaseHelper.VOLUNTEERING_TABLE_NAME + "." + DatabaseHelper.VOLUNTEERING_SITE_ID
                + " WHERE " + DatabaseHelper.VOLUNTEERING_USER_ID + " = " + user_id + " AND "
                + DatabaseHelper.SITE_LEADER + " = " + DatabaseHelper.USER_TABLE_NAME + "." +
                DatabaseHelper.USER_ID, null);
        if(cursor1 != null){
            cursor1.moveToFirst();
        }
        return cursor1;
    }

    public void addVolunteering(int site_id, int user_id, String friend_name, String friend_email){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.VOLUNTEERING_SITE_ID, site_id);
        contentValues.put(DatabaseHelper.VOLUNTEERING_USER_ID, user_id);
        contentValues.put(DatabaseHelper.VOLUNTEERING_FRIEND_NAME, friend_name);
        contentValues.put(DatabaseHelper.VOLUNTEERING_FRIEND_EMAIL, friend_email);
        if(!checkExistVolunteer(site_id, friend_email)){
            database.insert(DatabaseHelper.VOLUNTEERING_TABLE_NAME, null, contentValues);
        }
    }

    public boolean checkExistVolunteer(int site_id, String friend_email){
        String[] columns = new String[]{
                DatabaseHelper.VOLUNTEERING_USER_ID,
                DatabaseHelper.VOLUNTEERING_FRIEND_NAME,
                DatabaseHelper.VOLUNTEERING_FRIEND_EMAIL
        };
        Cursor cursor = database.query(DatabaseHelper.VOLUNTEERING_TABLE_NAME, columns,
                DatabaseHelper.VOLUNTEERING_FRIEND_EMAIL + " = '" + friend_email + "' and " +
                        DatabaseHelper.VOLUNTEERING_SITE_ID + " = " + site_id
                , null, null, null, null);
        return cursor.getCount() > 0;
    }
}
