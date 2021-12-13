package com.example.covidvolunteeringsite;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class AddSiteActivity extends AppCompatActivity {
    private SiteManager siteManager;
    private EditText siteName, siteLongitude, siteLatitude;
    private TextView siteLeader;
    private int leader_id;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_site);
        Intent intent = getIntent();
        Double longitude = intent.getExtras().getDouble("longitude");
        Double latitude = intent.getExtras().getDouble("latitude");
        boolean fromFloating = intent.getBooleanExtra("from_floating", false);
        leader_id = intent.getIntExtra("leader_id", 0);

        siteLongitude = findViewById(R.id.longitudeAdd);
        siteLatitude = findViewById(R.id.latitudeAdd);
        if(!fromFloating){
            siteLongitude.setText(longitude + "");
            siteLatitude.setText(latitude + "");
        }
        siteLeader = findViewById(R.id.leaderNameAdd);
        siteLeader.setText(intent.getStringExtra("leader_name"));

        siteManager = new SiteManager(this);
        siteManager.open();
    }

    @SuppressLint("SetTextI18n")
    public void onConfirmAddSite(View v){
        siteName = findViewById(R.id.nameAdd);
        String siteNameValue = siteName.getText().toString().trim();
        String longitudeValue = siteLongitude.getText().toString().trim();
        String latitudeValue = siteLatitude.getText().toString().trim();

        TextView invalidName = findViewById(R.id.invalidName);
        TextView invalidPosition = findViewById(R.id.invalidPosition);

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
                if (siteManager.checkExistPosition(-1, lng_double, latDouble)){
                    invalidPosition.setText("This location has been set already");
                    invalidPosition.setVisibility(View.VISIBLE);
                } else invalidPosition.setVisibility(View.GONE);
            } catch (NumberFormatException e){
                invalidPosition.setText("Invalid number format");
                invalidPosition.setVisibility(View.VISIBLE);
            }
        }

        if(invalidName.getVisibility() == View.GONE && invalidPosition.getVisibility() == View.GONE){
            siteManager.addSite(siteNameValue,
                    Double.parseDouble(siteLatitude.getText().toString()),
                    Double.parseDouble(siteLongitude.getText().toString()),
                    leader_id);
            backToMap(v);
        }
    }

    public void backToMap(View v){
        Intent intent = new Intent(AddSiteActivity.this, MapsActivity.class);
        setResult(100, intent);
        siteManager.close();
        finish();
    }
}