package com.example.messwala;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.messwala.adapters.MainRecyclerAdapter;
import com.example.messwala.adapters.ReviewsAdapter;
import com.example.messwala.modal.Mess;
import com.example.messwala.modal.Review;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MessDetail extends AppCompatActivity {
    ImageView messBanner;
    TextView messName;
    TextView price;
    RatingBar ratingBar;
    ImageView direction;
    ImageView contact;
    RecyclerView reviewRecycler;
    Button rateAndReview;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_detail);

        // Initialize views
        messBanner = findViewById(R.id.mess_banner_image);
        messName = findViewById(R.id.messName);
        price = findViewById(R.id.pricing);
        direction = findViewById(R.id.direction);
        contact = findViewById(R.id.connect_to_owner);
        ratingBar = findViewById(R.id.rating);
        reviewRecycler = findViewById(R.id.review_recycler_view);
        rateAndReview = findViewById(R.id.rate_review);
        progressBar = findViewById(R.id.progressBar);

        // Set the rating value
        float ratingValue = (float) getIntent().getDoubleExtra("rating", 0.0);
        ratingBar.setRating(ratingValue);

        // Get the image URL and mess name from intent
        String imageUrl = getIntent().getStringExtra("image");
        String messNameText = getIntent().getStringExtra("Name");

        // Set the mess name and price text
        messName.setText(messNameText);
        price.setText("Pricing: " + getIntent().getStringExtra("price") + " Per/Month" +
                "\n\nFood Type: " + getIntent().getStringExtra("food"));

        // Load image using Volley from the provided URL
        progressBar.setVisibility(View.VISIBLE);
        ImageRequest request = new ImageRequest(imageUrl,
                response -> {
                    // Image loaded successfully
                    messBanner.setImageBitmap(response);
                    progressBar.setVisibility(View.GONE);
                },
                0, 0, ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                error -> {
                    // Error loading image
                    Log.e("MessAdapter", "Error loading image: " + error.getMessage());
                    progressBar.setVisibility(View.GONE);
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

        // Set click listeners for direction and contact buttons
        direction.setOnClickListener(v -> {
            String latitude = getIntent().getStringExtra("Latitude");
            String longitude = getIntent().getStringExtra("Longitude");

            String uri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        contact.setOnClickListener(v -> {
            startActivity(new Intent(MessDetail.this, ContactPage.class));
        });

        // Get the mess ID from intent
        String messId = getIntent().getStringExtra("ID");
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // Get the reference to the specific node based on the ID
        DatabaseReference messRef = rootRef.child("ratings").child(messId);

        messRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double totalRating = 0.0;
                    int reviewCount = 0;
                    List<Review> reviewList = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Get the rating details
                        Double rating = snapshot.child("Rating").getValue(Double.class);
                        String comment = snapshot.child("Comment").getValue(String.class);
                        String userName = snapshot.child("UserName").getValue(String.class);
                        String email = snapshot.child("Email").getValue(String.class);
                        String date = snapshot.child("DATE").getValue(String.class);

                        // Calculate total rating and review count
                        totalRating += rating;
                        reviewCount++;

                        // Create Review object and add it to the list
                        Review review = new Review(userName, rating.floatValue(), comment, date);
                        reviewList.add(review);
                    }

                    // Update the recycler view with the review list
                    updateRecycler(reviewList);

                    // Calculate average rating
                    double averageRating = totalRating / reviewCount;

                    // Update average rating in Firebase
                    updateAverageRatingInFirebase(messId, String.valueOf(averageRating));
                } else {
                    // No reviews found for the given ID
                    Toast.makeText(MessDetail.this, "No reviews found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error occurred while accessing the database
                Toast.makeText(MessDetail.this, "Error retrieving reviews", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle rate and review button click
        rateAndReview.setOnClickListener(v -> {
            // Get the custom dialog layout
            LayoutInflater inflater = LayoutInflater.from(MessDetail.this);
            View dialogView = inflater.inflate(R.layout.review_and_rate_dialog, null);

            // Create the dialog builder and set the custom layout
            AlertDialog.Builder builder = new AlertDialog.Builder(MessDetail.this);
            builder.setView(dialogView);

            // Get the rating bar and comment input field from the dialog layout
            RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
            EditText commentInput = dialogView.findViewById(R.id.comment_input);

            // Set up the submit button click listener
            builder.setPositiveButton("Submit", (dialog, which) -> {
                // Get the user's rating and comment from the dialog
                float rating = ratingBar.getRating();
                String comment = commentInput.getText().toString();

                // Get the current date
                Date currentDate = new Date();

                // Format the date using SimpleDateFormat
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                String formattedDate = dateFormat.format(currentDate);

                // Get Firebase root reference
                DatabaseReference rootRef1 = FirebaseDatabase.getInstance().getReference();

                // Check if user's rating already exists
                Query query = rootRef1.child("ratings").child(getIntent().getStringExtra("ID"))
                        .orderByChild("Email").equalTo(MainActivity.Uemail);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Email already exists, update the rating details
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String key = snapshot.getKey();
                                DatabaseReference ratingRef = rootRef.child("ratings").child(getIntent().getStringExtra("ID")).child(key);

                                // Update the rating details
                                ratingRef.child("Rating").setValue(rating);
                                ratingRef.child("Comment").setValue(comment);
                                ratingRef.child("UserName").setValue(MainActivity.Uname);
                                ratingRef.child("DATE").setValue(formattedDate);

                                Toast.makeText(MessDetail.this, "Rating Updated", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Email does not exist, create a new rating node
                            DatabaseReference messRef = rootRef.child("ratings").child(getIntent().getStringExtra("ID")).push();

                            HashMap<String, Object> rate = new HashMap<>();
                            rate.put("Rating", rating);
                            rate.put("Comment", comment);
                            rate.put("UserName", MainActivity.Uname);
                            rate.put("Email", MainActivity.Uemail);
                            rate.put("DATE", formattedDate);
                            messRef.setValue(rate)
                                    .addOnSuccessListener(aVoid -> {
                                        // Data stored successfully in the database with the custom key
                                        Toast.makeText(MessDetail.this, "Thank You", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error occurred while storing the data
                                        Toast.makeText(MessDetail.this, "Error", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Error occurred while accessing the database
                        Toast.makeText(MessDetail.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });

                // Dismiss the dialog
                dialog.dismiss();
            });

            // Set up the cancel button click listener
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // Dismiss the dialog
                dialog.dismiss();
            });

            // Show the dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void updateRecycler(List<Review> reviewList) {
        ReviewsAdapter adapter = new ReviewsAdapter(reviewList, MessDetail.this);
        reviewRecycler.setLayoutManager(new LinearLayoutManager(MessDetail.this));
        reviewRecycler.setAdapter(adapter);
    }

    private void updateAverageRatingInFirebase(String messId, String newRating) {
        // Get a reference to the root of your Firebase Realtime Database
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // Create a reference to the specific mess entry using its unique ID
        DatabaseReference messRef = rootRef.child("messes").child(messId);

        // Create a HashMap to store the updated data fields
        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("RATING", newRating);

        // Update the data for the messRef node in the database
        messRef.updateChildren(updatedData)
                .addOnSuccessListener(aVoid -> {
                    // Data updated successfully in the database
                })
                .addOnFailureListener(e -> {
                    // Error occurred while updating the data
                });
    }
}
