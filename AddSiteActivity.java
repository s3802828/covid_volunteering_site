package com.example.covidvolunteeringsite;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;

public class AddSiteActivity extends AppCompatActivity {
    private SiteManager siteManager;
    private EditText siteName, siteLongitude, siteLatitude, siteLeader;
    private int leader_id;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_site);
        Intent intent = getIntent();
        double longitude = intent.getDoubleExtra("longitude", 0);
        double latitude = intent.getDoubleExtra("latitude", 0);
        leader_id = intent.getIntExtra("leader_id", 0);

        siteLongitude = findViewById(R.id.longitudeAdd);
        siteLongitude.setText(longitude + "");
        siteLatitude = findViewById(R.id.latitudeAdd);
        siteLatitude.setText(latitude + "");

        siteLeader = findViewById(R.id.leaderNameAdd);
        siteLeader.setText(intent.getStringExtra("leader_name"));

        siteManager = new SiteManager(this);
        siteManager.open();
    }

    public void onConfirmAddSite(View v){
        siteName = findViewById(R.id.nameAdd);
        siteManager.addSite(siteName.getText().toString(),
                Double.parseDouble(siteLatitude.getText().toString()),
                Double.parseDouble(siteLongitude.getText().toString()),
                leader_id);
        Intent intent = new Intent(AddSiteActivity.this, MapsActivity.class);
        intent.putExtra("successfully_added", true);
        setResult(200, intent);
        finish();
    }
}