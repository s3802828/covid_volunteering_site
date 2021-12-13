package com.example.covidvolunteeringsite;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

public class VolunteerListActivity extends AppCompatActivity {
    private SiteManager siteManager;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_list);
        Intent intent = getIntent();
        int site_id = intent.getExtras().getInt("site_id");
        String site_name = intent.getStringExtra("site_name");
        siteManager = new SiteManager(this);
        siteManager.open();

        ListView listView = findViewById(R.id.volunteer_list);

        Cursor cursor = siteManager.getAllVolunteerInOneSite(site_id);
        String[] from = new String[]{
                DatabaseHelper.VOLUNTEERING_FRIEND_NAME,
                DatabaseHelper.VOLUNTEERING_FRIEND_EMAIL
        };
        int[] to = new int[]{
                R.id.list_vol_name,
                R.id.list_vol_username
        };

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(VolunteerListActivity.this,
                R.layout.volunteer_list_layout, cursor, from, to, 0);
        simpleCursorAdapter.notifyDataSetChanged();
        listView.setAdapter(simpleCursorAdapter);

        HashMap<String, String> volunteers = new HashMap<>();
        do{
            if(cursor.getCount() > 0){
                volunteers.put(cursor.getString(2), cursor.getString(1));
            }
        } while(cursor.moveToNext());
        Button downloadButton = findViewById(R.id.download);
        TextView listHeader = findViewById(R.id.list_header);

        listHeader.setText("VOLUNTEER LIST OF SITE " + site_name.toUpperCase() + " (NO." + site_id + ")");
        downloadButton.setOnClickListener(v -> {
            downloadList("volunteer_list_" + site_name + "_" + site_id + ".csv", volunteers);
        });
    }

    private void downloadList(String filename, HashMap<String, String> data){
        try{
            FileOutputStream fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            String header = "VOLUNTEER NAME,VOLUNTEER EMAIL/USERNAME\n";
            fileOutputStream.write(header.getBytes());
            for (String key: data.keySet()) {
                String row = data.get(key) + "," + key + "\n";
                fileOutputStream.write(row.getBytes());
            }
            fileOutputStream.close();
            Toast.makeText(VolunteerListActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            Toast.makeText(VolunteerListActivity.this,
                    VolunteerListActivity.this.getFilesDir().getAbsolutePath() + "", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Toast.makeText(VolunteerListActivity.this, "Error saving file",
                    Toast.LENGTH_SHORT).show();
        }
    }
    public void backToMap(View v){
        Intent intent = new Intent(VolunteerListActivity.this, MapsActivity.class);
        setResult(500, intent);
        finish();
    }
}