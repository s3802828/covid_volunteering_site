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

public class LoginActivity extends AppCompatActivity {
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userManager = new UserManager(this);
        userManager.open();
    }

    public void goToSignUp(View v){
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivityForResult(intent, 200);
    }
    @SuppressLint("SetTextI18n")
    public void goToMap(View v){
        TextView invalidUsername = findViewById(R.id.invalidUsername);
        TextView invalidPassword = findViewById(R.id.invalidPassword);

        EditText username = findViewById(R.id.username_login);
        String usernameValue = username.getText().toString();

        EditText password = findViewById(R.id.password_login);
        String passwordValue = password.getText().toString();

        if(usernameValue.equals("")){
            invalidUsername.setText("Username is required");
            invalidUsername.setVisibility(View.VISIBLE);
        } else if(!userManager.isExistUsername(usernameValue)){
            invalidUsername.setText("Username is not existed");
            invalidUsername.setVisibility(View.VISIBLE);
        } else invalidUsername.setVisibility(View.GONE);

        if(passwordValue.equals("")){
            invalidPassword.setText("Password is required");
            invalidPassword.setVisibility(View.VISIBLE);
        } else if(!userManager.isLoginCorrect(usernameValue, passwordValue)){
            invalidPassword.setText("Incorrect Password");
            invalidPassword.setVisibility(View.VISIBLE);
        } else invalidPassword.setVisibility(View.GONE);

        if(invalidUsername.getVisibility() == View.GONE
                && invalidPassword.getVisibility() == View.GONE) {
            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
            Cursor cursor = userManager.getUserByUsername(usernameValue);
            intent.putExtra("user_id", cursor.getInt(0));
            intent.putExtra("user_name", cursor.getShort(1));
            intent.putExtra("user_username", usernameValue);
            intent.putExtra("is_super", cursor.getInt(2));
            startActivityForResult(intent, 300);
        }
    }

}