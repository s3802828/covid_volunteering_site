package com.example.covidvolunteeringsite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;

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
    public void signUp(View v){
        EditText name = findViewById(R.id.name_signup);
        String nameValue = name.getText().toString();
        EditText username = findViewById(R.id.username_signup);
        String usernameValue = username.getText().toString();
        EditText password = findViewById(R.id.password_signup);
        String passwordValue = password.getText().toString();
        userManager.addUser(nameValue, usernameValue, passwordValue);
        goToLogin(v);
    }
}