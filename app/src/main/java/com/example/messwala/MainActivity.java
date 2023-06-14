package com.example.messwala;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.messwala.adapters.MainRecyclerAdapter;
import com.example.messwala.modal.Mess;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private FusedLocationProviderClient fusedLocationClient;
    TextView user_location_tv;
    Location userLocation;
    String cityName;
    ImageView profile_avtar;
    ProgressBar progressBar;
    public static String Uname, Uemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the missing permissions
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, 0);

        }

        profile_avtar = findViewById(R.id.profile_avtar_pic);
        user_location_tv = findViewById(R.id.user_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());


        recyclerView = findViewById(R.id.main_recycler_view);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            progressBar.setVisibility(View.GONE);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                userLocation = location;
                try {

                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    cityName = addresses.get(0).getLocality();
                    String pincode = addresses.get(0).getPostalCode();
                    String stateName = addresses.get(0).getAdminArea();
                    String countryName = addresses.get(0).getCountryName();
                    user_location_tv.setText(cityName);
                    cityName = addresses.get(0).getLocality() + ", " + pincode + ", " + stateName + ", " + countryName;


                    // Retrieve data from firebase

                    DatabaseReference messesRef = FirebaseDatabase.getInstance().getReference().child("messes");
                    messesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            List<Mess> messList = new ArrayList<>();
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                // Retrieve the data from each child snapshot
                                String messName = childSnapshot.child("MESS_NAME").getValue(String.class);
                                String foodType = childSnapshot.child("FOOD_TYPE").getValue(String.class);
                                String price = childSnapshot.child("PRICE").getValue(String.class);
//                                    String dist = childSnapshot.child("DISTANCE").getValue(String.class);
                                String lati = childSnapshot.child("Latitude").getValue(String.class);
                                String longi = childSnapshot.child("Longitude").getValue(String.class);
                                String Rating = childSnapshot.child("RATING").getValue(String.class);
                                String imgUrl = childSnapshot.child("IMAGE_URL").getValue(String.class);
                                String ID = childSnapshot.getKey();
                                String mobile = childSnapshot.child("MOBILE_NO").getValue(String.class);

                                // Create a Mess object with the retrieved data
                                assert Rating != null;
                                assert longi != null;
                                assert lati != null;

                                Location locationB = new Location("location.getProvider()");
                                locationB.setLatitude(Double.parseDouble(lati));
                                locationB.setLongitude(Double.parseDouble(longi));

                                float dist = userLocation.distanceTo(locationB) / 1000;


                                Mess mess = new Mess(messName, foodType, price, dist + "", imgUrl, Double.parseDouble(Rating), Double.parseDouble(longi), Double.parseDouble(lati), ID,mobile);
                                // Add the Mess object to the list
                                messList.add(mess);
                            }

                            // Pass the messList to the RecyclerView adapter and update the RecyclerView
                            messList.sort(new Comparator<Mess>() {
                                @Override
                                public int compare(Mess o1, Mess o2) {
                                    Double d1 = Double.valueOf(o1.getDISTANCE());
                                    Double d2 = Double.valueOf(o2.getDISTANCE());
                                    return d1.compareTo(d2);
                                }
                            });
                            MainRecyclerAdapter adapter = new MainRecyclerAdapter(messList, MainActivity.this, userLocation);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            recyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle any errors that occur during data retrieval
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Uname = getIntent().getStringExtra("User Name");
        Uemail = getIntent().getStringExtra("Email");

        profile_avtar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfilePage.class);
                intent.putExtra("Name", getIntent().getStringExtra("User Name"));
                intent.putExtra("mail", getIntent().getStringExtra("Email"));
                intent.putExtra("city", cityName);
                startActivity(intent);
            }
        });


        ImageButton sortButton = findViewById(R.id.sortButton);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortMenu(v);

            }
        });

    }

    private void showSortMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, anchorView);
        popupMenu.inflate(R.menu.sort_menu); // Create a menu resource file named sort_menu.xml

        // Set click listeners for the menu items
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle menu item click events
                if (item.getItemId() == R.id.menu_sort_ratings) {
                    SortByRating();
                    Toast.makeText(MainActivity.this, "Sorted By Ratings", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.menu_sort_distance) {
                    SortByDistance();
                    Toast.makeText(MainActivity.this, "Sorted By Distance", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.menu_sort_price) {
                    SortByPrice();
                    Toast.makeText(MainActivity.this, "Sorted By Price", Toast.LENGTH_SHORT).show();
                    return true;
                } else return false;
            }
        });

        popupMenu.show();
    }

    void SortByDistance() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        progressBar.setVisibility(View.VISIBLE);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            progressBar.setVisibility(View.GONE);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                userLocation = location;
                try {

                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    cityName = addresses.get(0).getLocality();
                    String pincode = addresses.get(0).getPostalCode();
                    String stateName = addresses.get(0).getAdminArea();
                    String countryName = addresses.get(0).getCountryName();
                    user_location_tv.setText(cityName);
                    cityName = addresses.get(0).getLocality() + ", " + pincode + ", " + stateName + ", " + countryName;


                    // Retrieve data from firebase

                    DatabaseReference messesRef = FirebaseDatabase.getInstance().getReference().child("messes");
                    messesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            List<Mess> messList = new ArrayList<>();
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                // Retrieve the data from each child snapshot
                                String messName = childSnapshot.child("MESS_NAME").getValue(String.class);
                                String foodType = childSnapshot.child("FOOD_TYPE").getValue(String.class);
                                String price = childSnapshot.child("PRICE").getValue(String.class);
//                                    String dist = childSnapshot.child("DISTANCE").getValue(String.class);
                                String lati = childSnapshot.child("Latitude").getValue(String.class);
                                String longi = childSnapshot.child("Longitude").getValue(String.class);
                                String Rating = childSnapshot.child("RATING").getValue(String.class);
                                String imgUrl = childSnapshot.child("IMAGE_URL").getValue(String.class);
                                String ID = childSnapshot.getKey();
                                String mobile = childSnapshot.child("MOBILE_NO").getValue(String.class);

                                // Create a Mess object with the retrieved data
                                assert Rating != null;
                                assert longi != null;
                                assert lati != null;

                                Location locationB = new Location("location.getProvider()");
                                locationB.setLatitude(Double.parseDouble(lati));
                                locationB.setLongitude(Double.parseDouble(longi));

                                float dist = userLocation.distanceTo(locationB) / 1000;


                                Mess mess = new Mess(messName, foodType, price, dist + "", imgUrl, Double.parseDouble(Rating), Double.parseDouble(longi), Double.parseDouble(lati), ID,mobile);
                                // Add the Mess object to the list
                                messList.add(mess);
                            }

                            // Pass the messList to the RecyclerView adapter and update the RecyclerView
                            messList.sort(new Comparator<Mess>() {
                                @Override
                                public int compare(Mess o1, Mess o2) {
                                    Double d1 = Double.valueOf(o1.getDISTANCE());
                                    Double d2 = Double.valueOf(o2.getDISTANCE());
                                    return d1.compareTo(d2);
                                }
                            });
                            MainRecyclerAdapter adapter = new MainRecyclerAdapter(messList, MainActivity.this, userLocation);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            recyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle any errors that occur during data retrieval
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    void SortByRating() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        progressBar.setVisibility(View.VISIBLE);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            progressBar.setVisibility(View.GONE);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                userLocation = location;
                try {

                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    cityName = addresses.get(0).getLocality();
                    String pincode = addresses.get(0).getPostalCode();
                    String stateName = addresses.get(0).getAdminArea();
                    String countryName = addresses.get(0).getCountryName();
                    user_location_tv.setText(cityName);
                    cityName = addresses.get(0).getLocality() + ", " + pincode + ", " + stateName + ", " + countryName;


                    // Retrieve data from firebase

                    DatabaseReference messesRef = FirebaseDatabase.getInstance().getReference().child("messes");
                    messesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            List<Mess> messList = new ArrayList<>();
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                // Retrieve the data from each child snapshot
                                String messName = childSnapshot.child("MESS_NAME").getValue(String.class);
                                String foodType = childSnapshot.child("FOOD_TYPE").getValue(String.class);
                                String price = childSnapshot.child("PRICE").getValue(String.class);
//                                    String dist = childSnapshot.child("DISTANCE").getValue(String.class);
                                String lati = childSnapshot.child("Latitude").getValue(String.class);
                                String longi = childSnapshot.child("Longitude").getValue(String.class);
                                String Rating = childSnapshot.child("RATING").getValue(String.class);
                                String imgUrl = childSnapshot.child("IMAGE_URL").getValue(String.class);
                                String ID = childSnapshot.getKey();
                                String mobile = childSnapshot.child("MOBILE_NO").getValue(String.class);

                                // Create a Mess object with the retrieved data
                                assert Rating != null;
                                assert longi != null;
                                assert lati != null;

                                Location locationB = new Location("location.getProvider()");
                                locationB.setLatitude(Double.parseDouble(lati));
                                locationB.setLongitude(Double.parseDouble(longi));

                                float dist = userLocation.distanceTo(locationB) / 1000;


                                Mess mess = new Mess(messName, foodType, price, dist + "", imgUrl, Double.parseDouble(Rating), Double.parseDouble(longi), Double.parseDouble(lati), ID, mobile);
                                // Add the Mess object to the list
                                messList.add(mess);
                            }

                            // Pass the messList to the RecyclerView adapter and update the RecyclerView
                            messList.sort(new Comparator<Mess>() {
                                @Override
                                public int compare(Mess o1, Mess o2) {
                                    Double d1 = o1.getRATING();
                                    Double d2 = o2.getRATING();
                                    return Double.compare(d2,d1);
                                }
                            });
                            MainRecyclerAdapter adapter = new MainRecyclerAdapter(messList, MainActivity.this, userLocation);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            recyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle any errors that occur during data retrieval
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    void SortByPrice() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        progressBar.setVisibility(View.VISIBLE);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            progressBar.setVisibility(View.GONE);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                userLocation = location;
                try {

                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    cityName = addresses.get(0).getLocality();
                    String pincode = addresses.get(0).getPostalCode();
                    String stateName = addresses.get(0).getAdminArea();
                    String countryName = addresses.get(0).getCountryName();
                    user_location_tv.setText(cityName);
                    cityName = addresses.get(0).getLocality() + ", " + pincode + ", " + stateName + ", " + countryName;


                    // Retrieve data from firebase

                    DatabaseReference messesRef = FirebaseDatabase.getInstance().getReference().child("messes");
                    messesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            List<Mess> messList = new ArrayList<>();
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                // Retrieve the data from each child snapshot
                                String messName = childSnapshot.child("MESS_NAME").getValue(String.class);
                                String foodType = childSnapshot.child("FOOD_TYPE").getValue(String.class);
                                String price = childSnapshot.child("PRICE").getValue(String.class);
//                                    String dist = childSnapshot.child("DISTANCE").getValue(String.class);
                                String lati = childSnapshot.child("Latitude").getValue(String.class);
                                String longi = childSnapshot.child("Longitude").getValue(String.class);
                                String Rating = childSnapshot.child("RATING").getValue(String.class);
                                String imgUrl = childSnapshot.child("IMAGE_URL").getValue(String.class);
                                String ID = childSnapshot.getKey();
                                String mobile = childSnapshot.child("MOBILE_NO").getValue(String.class);

                                // Create a Mess object with the retrieved data
                                assert Rating != null;
                                assert longi != null;
                                assert lati != null;

                                Location locationB = new Location("location.getProvider()");
                                locationB.setLatitude(Double.parseDouble(lati));
                                locationB.setLongitude(Double.parseDouble(longi));

                                float dist = userLocation.distanceTo(locationB) / 1000;


                                Mess mess = new Mess(messName, foodType, price, dist + "", imgUrl, Double.parseDouble(Rating), Double.parseDouble(longi), Double.parseDouble(lati), ID,mobile);
                                // Add the Mess object to the list
                                messList.add(mess);
                            }

                            // Pass the messList to the RecyclerView adapter and update the RecyclerView
                            messList.sort(new Comparator<Mess>() {
                                @Override
                                public int compare(Mess o1, Mess o2) {
                                    Double d1 = Double.valueOf(o1.getPRICE());
                                    Double d2 = Double.valueOf(o2.getPRICE());
                                    return d1.compareTo(d2);
                                }
                            });
                            MainRecyclerAdapter adapter = new MainRecyclerAdapter(messList, MainActivity.this, userLocation);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            recyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle any errors that occur during data retrieval
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}