package com.example.covidvolunteeringsite;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateSiteActivity extends AppCompatActivity {
    private SiteManager siteManager;
    private NotificationAppManager notificationAppManager;
    private EditText siteName;
    private EditText siteLongitude;
    private EditText siteLatitude;
    private EditText siteTestNum;
    private int site_id, numOfTested;
    private double longitude, latitude;
    private String currentSiteName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_site);
        Intent intent = getIntent();
        currentSiteName = intent.getExtras().getString("site_name");
        site_id = intent.getExtras().getInt("site_id");
        longitude = intent.getDoubleExtra("longitude", 0);
        latitude = intent.getDoubleExtra("latitude", 0);
        numOfTested = intent.getExtras().getInt("num_of_tested");

        siteName = findViewById(R.id.nameUpdate);
        siteName.setText(currentSiteName);

        siteLongitude = findViewById(R.id.longitudeUpdate);
        siteLongitude.setText(longitude + "");
        siteLatitude = findViewById(R.id.latitudeUpdate);
        siteLatitude.setText(latitude + "");

        EditText siteLeader = findViewById(R.id.leaderNameUpdate);
        siteLeader.setText(intent.getStringExtra("leader_name"));

        siteTestNum = findViewById(R.id.tested_num);
        siteTestNum.setText(numOfTested + "");

        siteManager = new SiteManager(this);
        notificationAppManager = new NotificationAppManager(this);
        notificationAppManager.open();
        siteManager.open();
    }

    @SuppressLint("SetTextI18n")
    public void onConfirmUpdateSite(View v){
        String siteNameValue = siteName.getText().toString().trim();
        String longitudeValue = siteLongitude.getText().toString().trim();
        String latitudeValue = siteLatitude.getText().toString().trim();
        String testNumValue = siteTestNum.getText().toString().trim();

        TextView invalidName = findViewById(R.id.invalidName);
        TextView invalidPosition = findViewById(R.id.invalidPosition);
        TextView invalidTestNum = findViewById(R.id.invalidTestNum);

        if(siteNameValue.equals("")){
            invalidName.setText("Name is required");
            invalidName.setVisibility(View.VISIBLE);
        } else if (!siteNameValue.matches("^(?![ ]+$)[a-zA-Z0-9 .]*$")){
            invalidName.setText("Name must only contain letters, numbers, and space");
            invalidName.setVisibility(View.VISIBLE);
        } else invalidName.setVisibility(View.GONE);

        if(longitudeValue.equals("") || latitudeValue.equals("")){
            invalidPosition.setText("Both latitude and longitude are required");
            invalidPosition.setVisibility(View.VISIBLE);
        } else {
            double lng_double, latDouble;
            try {
                lng_double = Double.parseDouble(longitudeValue);
                latDouble = Double.parseDouble(latitudeValue);
                if (siteManager.checkExistPosition(site_id, lng_double, latDouble)){
                    invalidPosition.setText("This location has been set already");
                    invalidPosition.setVisibility(View.VISIBLE);
                } else invalidPosition.setVisibility(View.GONE);
            } catch (NumberFormatException e){
                invalidPosition.setText("Invalid number format");
                invalidPosition.setVisibility(View.VISIBLE);
            }
        }

        if(testNumValue.equals("")){
            invalidTestNum.setText("Number of tested people is required");
            invalidTestNum.setVisibility(View.VISIBLE);
        } else {
            try{
                if(Integer.parseInt(testNumValue) < 0){
                    invalidTestNum.setText("Number of people must be positive");
                    invalidTestNum.setVisibility(View.VISIBLE);
                } else invalidTestNum.setVisibility(View.GONE);
            } catch (NumberFormatException e){
                invalidTestNum.setText("Invalid positive integer format");
                invalidTestNum.setVisibility(View.VISIBLE);
            }
        }

        if(invalidName.getVisibility() == View.GONE && invalidPosition.getVisibility() == View.GONE
        && invalidTestNum.getVisibility() == View.GONE){
            if(siteNameValue.equals(currentSiteName) && Double.parseDouble(latitudeValue) == latitude
            && Double.parseDouble(longitudeValue) == longitude && Integer.parseInt(testNumValue) == numOfTested){
                Toast.makeText(UpdateSiteActivity.this, "You have not made any changes", Toast.LENGTH_SHORT).show();
            } else {
                siteManager.updateSite(site_id, siteNameValue, Double.parseDouble(latitudeValue),
                        Double.parseDouble(longitudeValue), Integer.parseInt(testNumValue));
                Cursor cursor = siteManager.getAllVolunteerAccountInOneSite(site_id);
                StringBuilder message = new StringBuilder().append("Leader has modified ");
                if(!siteNameValue.equals(currentSiteName)) message.append("name, ");
                if(Double.parseDouble(longitudeValue) != longitude || Double.parseDouble(longitudeValue) != latitude)
                    message.append("position, ");
                if(Integer.parseInt(testNumValue) != numOfTested) message.append("number of tested people ");
                message.append("of site: ").append(currentSiteName).append(" (NO.").append(site_id).append(")");
                do{
                   if(cursor.getCount() > 0){
                       notificationAppManager.addNotification(cursor.getInt(0), message.toString(), false);
                   }
                } while(cursor.moveToNext());
                backToMap(v);
            }
        }
    }

    public void backToMap(View v){
        Intent intent = new Intent(UpdateSiteActivity.this, MapsActivity.class);
        setResult(400, intent);
        siteManager.close();
        notificationAppManager.close();
        finish();
    }
}