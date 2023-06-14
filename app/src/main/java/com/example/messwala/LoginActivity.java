package com.example.messwala;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class LoginActivity extends AppCompatActivity {
    EditText mail, password;
    Button signIn;
    TextView creatAccount, add_mess;
    FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mauth = FirebaseAuth.getInstance();

        mail = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);
        signIn = findViewById(R.id.login_button);
        creatAccount = findViewById(R.id.creat_account);
        add_mess = findViewById(R.id.mess_owner_login);

        // Set click listener for "Add Mess" text view
        add_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, AddMessShop.class);
                startActivity(intent);
            }
        });

        // Request location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, 0);
        }

        // Set click listener for sign-in button
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // Set click listener for "Create Account" text view
        creatAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpPage.class));
            }
        });
    }

    // Method to handle user login
    private void loginUser() {
        // Find the progress bar in the layout file
        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        String email = mail.getText().toString();
        String pass = password.getText().toString();

        if (TextUtils.isEmpty(email)) {
            mail.setError("Email cannot be empty");
            mail.requestFocus();
        } else if (TextUtils.isEmpty(pass)) {
            password.setError("Password cannot be empty");
            password.requestFocus();
        } else {
            // Authenticate user using Firebase Authentication
            mauth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String userId = user.getUid();
                        String email = user.getEmail();

                        // Retrieve user's name from Firebase Realtime Database
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String userName = dataSnapshot.child("name").getValue(String.class);
                                    // Do something with userName
                                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                    // Pass user name and email to the MainActivity
                                    intent.putExtra("User Name", userName);
                                    intent.putExtra("Email", email);

                                    startActivity(intent);
                                    progressBar.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle error
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Failed To Login due to: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
