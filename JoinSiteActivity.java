package com.example.covidvolunteeringsite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class JoinSiteActivity extends AppCompatActivity {
    private TextView siteNum;
    private UserManager userManager;
    private SiteManager siteManager;
    private NotificationAppManager notificationAppManager;
    private int userID, leader_id;
    private String currentUserName, currentUserUsername, siteNameJoin;
    private Integer siteNumber;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_site);
        userManager = new UserManager(this);
        siteManager = new SiteManager(this);
        notificationAppManager = new NotificationAppManager(this);
        siteManager.open();
        userManager.open();
        notificationAppManager.open();

        Intent intent = getIntent();

        currentUserName = intent.getStringExtra("user_name");
        currentUserUsername = intent.getStringExtra("user_username");
        siteNumber = intent.getExtras().getInt("site_id");
        siteNameJoin = intent.getStringExtra("site_name");
        String siteLeaderJoin = intent.getStringExtra("site_leader");
        userID = intent.getExtras().getInt("user_id");
        leader_id = intent.getExtras().getInt("site_leader_id");


        TextView username = findViewById(R.id.username_join);
        username.setText(currentUserUsername);

        TextView user_name = findViewById(R.id.user_name_join);
        user_name.setText(currentUserName);

        siteNum = findViewById(R.id.site_num_join);
        siteNum.setText(siteNumber.toString());

        TextView siteName = findViewById(R.id.site_name_join);
        siteName.setText(siteNameJoin);

        TextView siteLeader = findViewById(R.id.site_leader_join);
        siteLeader.setText(siteLeaderJoin);

    }

    public void backToMap(View v){
        Intent intent = new Intent(JoinSiteActivity.this, MapsActivity.class);
        setResult(200, intent);
        siteManager.close();
        userManager.close();
        notificationAppManager.close();
        finish();
    }

    @SuppressLint("SetTextI18n")
    public void onConfirm(View v){
        EditText friendEmail = findViewById(R.id.friend_email_join);
        String emailValue = friendEmail.getText().toString().trim();

        EditText friendName = findViewById(R.id.friend_name_join);
        String nameValue = friendName.getText().toString().trim();

        TextView invalidEmail = findViewById(R.id.invalidEmail);
        TextView invalidName = findViewById(R.id.invalidName);
        TextView invalidVolunteer = findViewById(R.id.invalidVolunteer);

        if(emailValue.equals("") && !nameValue.equals("")){
            invalidEmail.setText("Email is required");
            invalidEmail.setVisibility(View.VISIBLE);
            invalidVolunteer.setVisibility(View.GONE);
        } else if(!emailValue.equals("") && nameValue.equals("")){
            invalidName.setText("Name is required");
            invalidName.setVisibility(View.VISIBLE);
            invalidVolunteer.setVisibility(View.GONE);
        } else {
            invalidEmail.setVisibility(View.GONE);
            invalidName.setVisibility(View.GONE);
            boolean isExistVolunteer = false, isLeader = false;
            if(emailValue.equals("")){
                if(userID == leader_id) isLeader = true;
                else isExistVolunteer = siteManager.checkExistVolunteer(Integer.parseInt(siteNum.getText().toString()), currentUserUsername);
            } else {
                isExistVolunteer = siteManager.checkExistVolunteer(Integer.parseInt(siteNum.getText().toString()), emailValue);
            }
            if(isLeader){
                invalidVolunteer.setText("Leader is already registered");
                invalidVolunteer.setVisibility(View.VISIBLE);
            } else {
                if(isExistVolunteer){
                    invalidVolunteer.setText("This volunteer is already registered for this site");
                    invalidVolunteer.setVisibility(View.VISIBLE);
                } else invalidVolunteer.setVisibility(View.GONE);
            }
        }
        if(invalidVolunteer.getVisibility() == View.GONE && invalidEmail.getVisibility() == View.GONE && invalidName.getVisibility() == View.GONE){
            if(userID != leader_id) {
                siteManager.addVolunteering(Integer.parseInt(siteNum.getText().toString()), userID, currentUserName, currentUserUsername);
                String message = currentUserName + " (" + currentUserUsername
                        + ") has recently joined your site: " + siteNameJoin
                        + " (NO." + siteNumber +")";
                notificationAppManager.addNotification(leader_id, message, true);
            }
            if(!emailValue.equals("")) {
                siteManager.addVolunteering(Integer.parseInt(siteNum.getText().toString()), userID, nameValue,emailValue);
                String message = nameValue + " (" + emailValue
                        + ") has recently joined your site: " + siteNameJoin
                        + " (NO." + siteNumber +")";
                notificationAppManager.addNotification(leader_id, message, true);
            }
            backToMap(v);
        }
    }
}