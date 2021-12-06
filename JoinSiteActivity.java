package com.example.covidvolunteeringsite;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;

public class JoinSiteActivity extends AppCompatActivity {
    private EditText username, user_name, siteNum, siteName, siteLeader;
    private TextView invalidUsername;
    private UserManager userManager;
    private SiteManager siteManager;
    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_site);
        userManager = new UserManager(this);
        siteManager = new SiteManager(this);
        siteManager.open();
        userManager.open();

        Intent intent = getIntent();

        String currentUserName = intent.getStringExtra("user_name");
        String currentUserUsername = intent.getStringExtra("user_username");
        int siteNumber = intent.getExtras().getInt("site_id");
        String siteNameJoin = intent.getStringExtra("site_name");
        String siteLeaderJoin = intent.getStringExtra("site_leader");
        userID = intent.getExtras().getInt("user_id");


        username = findViewById(R.id.username_join);
        username.setText(currentUserUsername);

        user_name = findViewById(R.id.user_name_join);
        user_name.setText(currentUserName);

        siteNum = findViewById(R.id.site_num_join);
        siteNum.setText(siteNumber);

        siteName = findViewById(R.id.site_name_join);
        siteName.setText(siteNameJoin);

        siteLeader = findViewById(R.id.site_leader_join);
        siteLeader.setText(siteLeaderJoin);

        invalidUsername = findViewById(R.id.invalidUsername);

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")){
                    invalidUsername.setText("Username is required");
                    invalidUsername.setVisibility(View.VISIBLE);
                } else if(!userManager.isExistUsername(s.toString())){
                    invalidUsername.setText("Username is not existed");
                    invalidUsername.setVisibility(View.VISIBLE);
                } else {
                    invalidUsername.setVisibility(View.GONE);
                    Cursor cursor = userManager.getUserByUsername(s.toString());
                    user_name.setText(cursor.getString(1));
                    userID = cursor.getInt(0);
                };
            }
        });
    }

    public void backToMap(View v){
        Intent intent = new Intent(JoinSiteActivity.this, MapsActivity.class);
        setResult(200, intent);
        siteManager.close();
        userManager.close();
        finish();
    }

    public void onConfirm(View v){
        if(invalidUsername.getVisibility() == View.GONE){
            siteManager.addVolunteering(Integer.parseInt(siteNum.getText().toString()), userID);
            backToMap(v);
        }
    }
}