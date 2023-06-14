package com.example.messwala;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpPage extends AppCompatActivity {
    EditText mail, password, name;
    Button signIn;
    TextView goto_login_page;

    FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        // Initialize UI elements
        mail = findViewById(R.id.emailSignUp);
        password = findViewById(R.id.passwordSignUp);
        name = findViewById(R.id.usersName);
        signIn = findViewById(R.id.signInButton);
        goto_login_page = findViewById(R.id.go_to_login_page);

        // Get Firebase Authentication instance
        mauth = FirebaseAuth.getInstance();

        // Set click listener for sign-in button
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });

        // Set click listener for "Go to Login" text view
        goto_login_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpPage.this, LoginActivity.class));
            }
        });
    }

    // Method to create a new user
    private void createUser() {
        String email = mail.getText().toString();
        String pass = password.getText().toString();
        String Name = name.getText().toString();

        // Validate input fields
        if (TextUtils.isEmpty(email)) {
            mail.setError("Email cannot be empty");
            mail.requestFocus();
        } else if (TextUtils.isEmpty(pass)) {
            password.setError("Password cannot be empty");
            password.requestFocus();
        } else if (TextUtils.isEmpty(Name)) {
            password.setError("Name cannot be empty");
            password.requestFocus();
        } else {
            // Create user using Firebase Authentication
            mauth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mauth.getCurrentUser();

                        // Store the user's name in a separate Firebase Realtime Database or Firestore document
                        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                        database.child("users").child(user.getUid()).child("name").setValue(Name);

                        Toast.makeText(SignUpPage.this, "Account Created successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpPage.this, LoginActivity.class));
                    } else {
                        // Registration failed, display error message
                        Toast.makeText(SignUpPage.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
