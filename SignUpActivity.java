package com.example.covidvolunteeringsite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userManager = new UserManager(this);
        userManager.open();
    }

    public void goToLogin(View v){
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        setResult(200, intent);
        userManager.close();
        finish();
    }
    @SuppressLint("SetTextI18n")
    public void signUp(View v){
        EditText name = findViewById(R.id.name_signup);
        String nameValue = name.getText().toString().trim();

        EditText username = findViewById(R.id.username_signup);
        String usernameValue = username.getText().toString();

        EditText password = findViewById(R.id.password_signup);
        String passwordValue = password.getText().toString();

        TextView invalidUsername = findViewById(R.id.invalidUsername);
        TextView invalidPassword = findViewById(R.id.invalidPassword);
        TextView invalidName = findViewById(R.id.invalidName);


        if(nameValue.equals("")){
            invalidName.setText("Name is required");
            invalidName.setVisibility(View.VISIBLE);
        } else if (!nameValue.matches("^(?![ ]+$)[a-zA-Z .]*$")){
            invalidName.setText("Name must only contain letters and space");
            invalidName.setVisibility(View.VISIBLE);
        } else invalidName.setVisibility(View.GONE);

        if(usernameValue.equals("")){
            invalidUsername.setText("Username is required");
            invalidUsername.setVisibility(View.VISIBLE);
        } else if (!usernameValue.matches("^[a-zA-Z0-9_]+$")){
            invalidUsername.setText("Username must only contain letters, numbers, or underscores");
            invalidUsername.setVisibility(View.VISIBLE);
        } else if(userManager.isExistUsername(usernameValue)){
            invalidUsername.setText("Username is already existed");
            invalidUsername.setVisibility(View.VISIBLE);
        } else invalidUsername.setVisibility(View.GONE);

        if(passwordValue.equals("")){
            invalidPassword.setText("Password is required");
            invalidPassword.setVisibility(View.VISIBLE);
        } else if (!(8 <= passwordValue.length() && passwordValue.length() <= 16)){
            invalidPassword.setText("Password must contain 8-16 characters");
            invalidPassword.setVisibility(View.VISIBLE);
        } else if (!passwordValue.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]*$")){
            invalidPassword.setText("Password must only contain at least one letter, one number, and one special character");
            invalidPassword.setVisibility(View.VISIBLE);
        } else invalidPassword.setVisibility(View.GONE);

        if(invalidName.getVisibility() == View.GONE && invalidUsername.getVisibility() == View.GONE
        && invalidPassword.getVisibility() == View.GONE){
            userManager.addUser(nameValue.trim(), usernameValue.trim(), passwordValue.trim());
            goToLogin(v);
        }
    }
}