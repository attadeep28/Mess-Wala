package com.example.messwala;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messwala.adapters.MainRecyclerAdapter;
import com.example.messwala.modal.Mess;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddMessShop extends AppCompatActivity {
    private RadioGroup foodTypeRadioGroup;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_PICK_IMAGE = 1;
    EditText longitudeEt, latitudeEt, messNameEt, pricePerMonth, mobileNo;
    TextView city;
    Uri selectedImage;
    ImageView messProfile;
    String selectedFoodType;
    Button add;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mess_shop);

        // Initialize views
        foodTypeRadioGroup = findViewById(R.id.foodTypeRadioGroup);
        city = findViewById(R.id.mess_city_name);
        messNameEt = findViewById(R.id.et_messName);
        pricePerMonth = findViewById(R.id.et_price);
        longitudeEt = findViewById(R.id.et_longitude);
        latitudeEt = findViewById(R.id.et_latitude);
        mobileNo = findViewById(R.id.et_contactNo);
        messProfile = findViewById(R.id.mess_profile_img);
        add = findViewById(R.id.addMess);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        // Set onCheckedChangeListener for food type radio group
        foodTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                selectedFoodType = radioButton.getText().toString();
                Toast.makeText(AddMessShop.this, "Selected food type: " + selectedFoodType, Toast.LENGTH_SHORT).show();
            }
        });

        // Request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, 0);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Set location automatically
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                longitudeEt.setText(String.valueOf(longitude));
                latitudeEt.setText(String.valueOf(latitude));

                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    String cityName = addresses.get(0).getLocality();
                    String pincode = addresses.get(0).getPostalCode();
                    String stateName = addresses.get(0).getAdminArea();
                    String countryName = addresses.get(0).getCountryName();
                    city.append(cityName + ", " + pincode + ", " + stateName);
                } catch (IOException ignored) {
                }
            }
        });

        // Open gallery on click of messProfile image
        messProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Add mess on button click
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                uploadImageToFirebase();
            }
        });
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            messProfile.setImageURI(selectedImage);
        }
    }

    private void uploadImageToFirebase() {
        // Get the image from the ImageView
        BitmapDrawable drawable = (BitmapDrawable) messProfile.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        // Create a unique file name for the image in Firebase Storage
        String filename = UUID.randomUUID().toString();

        // Get a reference to the Firebase Storage location where you want to store the image
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + filename);

        // Convert the bitmap to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Upload the byte array to Firebase Storage
        UploadTask uploadTask = storageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrl) {
                        String imageUrl = downloadUrl.toString();
                        String name = messNameEt.getText().toString();
                        String price = pricePerMonth.getText().toString();
                        String longi = longitudeEt.getText().toString();
                        String lati = latitudeEt.getText().toString();
                        String contactNo = mobileNo.getText().toString();
                        storeDataInFirebase(name, selectedFoodType, price, "0", imageUrl, "0", longi, lati, contactNo);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddMessShop.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddMessShop.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storeDataInFirebase(String messName, String foodType, String price, String distance, String imageUrl, String rating, String longitude, String latitude,String mobileNo) {
        // Get a reference to the root of your Firebase Realtime Database
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // Create a new child node under the root node and get its reference
        DatabaseReference messRef = rootRef.child("messes").push();

        // Create a HashMap to store the data fields
        HashMap<String, Object> messData = new HashMap<>();
        messData.put("MESS_NAME", messName);
        messData.put("FOOD_TYPE", foodType);
        messData.put("PRICE", price);
        messData.put("DISTANCE", distance);
        messData.put("IMAGE_URL", imageUrl);
        messData.put("RATING", rating);
        messData.put("Longitude", longitude);
        messData.put("Latitude", latitude);
        messData.put("MOBILE_NO",mobileNo);
        // Set the data to the messRef node in the database
        messRef.setValue(messData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddMessShop.this, "Mess added successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddMessShop.this, LoginActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddMessShop.this, "Failed to add mess: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
