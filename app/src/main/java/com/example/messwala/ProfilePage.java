package com.example.messwala;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class ProfilePage extends AppCompatActivity {
    TextView name, email, card_name, location;
    public static String Uname, Uemail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        name = findViewById(R.id.profile_page_userName);
        card_name = findViewById(R.id.profile_image_card_name);
        email = findViewById(R.id.profile_mail);
        location = findViewById(R.id.profile_current_address);
        name.setText(getIntent().getStringExtra("Name"));
        email.setText(getIntent().getStringExtra("mail"));
        card_name.setText(getIntent().getStringExtra("Name"));
        location.setText(getIntent().getStringExtra("city"));
        Uname = getIntent().getStringExtra("Name");
        Uemail = getIntent().getStringExtra("mail");

    }
}