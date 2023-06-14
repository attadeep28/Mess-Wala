package com.example.messwala.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.messwala.MessDetail;
import com.example.messwala.R;
import com.example.messwala.modal.Mess;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainRecyclerAdapter.MyViewHolder> {
    List<Mess> messList;
    Context context;
    Location locationA;


    public MainRecyclerAdapter(List<Mess> messList, Context context, Location locationA) {
        this.messList = messList;
        this.context = context;
        this.locationA = locationA;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_recycler, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Mess mess = messList.get(position);




        Location locationB = new Location("location.getProvider()");
        locationB.setLatitude(mess.getLatitude());
        locationB.setLongitude(mess.getLongitude());

        float distance = locationA.distanceTo(locationB) / 1000;


        holder.messName.setText(mess.getMESS_NAME());
        holder.rating.setText(String.format("%.1f",mess.getRATING()));
        holder.distance.setText(String.format("%.2f", distance)+" Km");
        holder.foodType.setText(mess.getFOOD_TYPE());
        holder.price.setText(mess.getPRICE() + " Per/Month");

        // Load image using Volley from the provided URL
        holder.progressBar.setVisibility(View.VISIBLE);
        ImageRequest request = new ImageRequest(mess.getIMAGE_URL(),
                response -> {
                    // Image loaded successfully
                    holder.cardImage.setImageBitmap(response);
                    holder.progressBar.setVisibility(View.GONE);
                },
                0, 0, ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                error -> {
                    // Error loading image
                    Log.e("MessAdapter", "Error loading image: " + error.getMessage());
                    holder.progressBar.setVisibility(View.GONE);
                });

        RequestQueue requestQueue = Volley.newRequestQueue(holder.itemView.getContext());
        requestQueue.add(request);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();

                Intent intent = new Intent(context, MessDetail.class);
                intent.putExtra("Name",mess.getMESS_NAME());
                intent.putExtra("price",mess.getPRICE());
                intent.putExtra("Distance",mess.getDISTANCE());
                intent.putExtra("rating",mess.getRATING());
                intent.putExtra("food",mess.getFOOD_TYPE());
                intent.putExtra("Latitude",""+mess.getLatitude());
                intent.putExtra("Longitude",""+mess.getLongitude());
                intent.putExtra("image",mess.getIMAGE_URL());
                intent.putExtra("ID",mess.getID());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView cardImage;
        private TextView messName;
        private TextView rating;
        private TextView distance;
        private TextView foodType;
        private TextView price;
        ProgressBar progressBar;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.card_image);
            messName = itemView.findViewById(R.id.mess_name);
            rating = itemView.findViewById(R.id.rating);
            distance = itemView.findViewById(R.id.distance);
            foodType = itemView.findViewById(R.id.foodType);
            price = itemView.findViewById(R.id.price);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
