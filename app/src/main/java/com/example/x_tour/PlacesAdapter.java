package com.example.x_tour;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.MyViewHolder> {

    Context context;
    ArrayList<PlacesIDs> placesIDsArrayList;
    private static final String apiKey = "AIzaSyCCRv9GRsmztETeNJI8iASrooelW2a1zrU";

    public PlacesAdapter(Context context, ArrayList<PlacesIDs> placesIDsArrayList) {
        this.context = context;
        this.placesIDsArrayList = placesIDsArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.places, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PlacesIDs placesIDs = placesIDsArrayList.get(position);

        // initialize Google Places SDK
        Places.initialize(context, apiKey);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(context);

        // fetch the placeID
        final String placeID = placesIDs.getPlaceID();

        // specify the fields to return
        final List<Place.Field> placeFields = Arrays.asList(
                Place.Field.PHOTO_METADATAS,
                Place.Field.NAME,
                Place.Field.TYPES,
                Place.Field.BUSINESS_STATUS
        );

        // Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            holder.tvName.setText(place.getName()); // set Place name

            List<Place.Type> placeTypes = place.getTypes();
            holder.tvType.setText(capitalize(placeTypes.get(0).toString().replaceAll("_", " "))); // set Place type
            holder.tvStatus.setText(place.getBusinessStatus().toString()); // set Place business status

            // get photo metadata
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w(TAG, "No photo metadata.");
            }
            final PhotoMetadata photoMetadata = metadata.get(0);

            // create a FetchPhotoRequest
            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                holder.ivPhoto.setImageBitmap(bitmap); // set Place photo
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                    final int statusCode = apiException.getStatusCode();
                    // TODO: Handle error with given status code.
                }
            });

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException){
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });

        holder.cvPlaces.setOnClickListener(view -> {
            Fragment fragment = new ViewPlaceDetails();
            Bundle bundle = new Bundle();
            bundle.putString("placeID", placeID);
            fragment.setArguments(bundle);
            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        });

    }

    @Override
    public int getItemCount() {
        return placesIDsArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cvPlaces;
        ImageView ivPhoto;
        TextView tvName, tvType, tvStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            cvPlaces = itemView.findViewById(R.id.cvPlaces);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvName = itemView.findViewById(R.id.tvName);
            tvType = itemView.findViewById(R.id.tvType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

    public String capitalize(String capsString) {
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capsString);
        while (matcher.find()){
            matcher.appendReplacement(stringBuffer, matcher.group(1).toUpperCase() + matcher.group(2).toLowerCase());
        }

        return matcher.appendTail(stringBuffer).toString();
    }
}
