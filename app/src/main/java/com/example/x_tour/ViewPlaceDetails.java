package com.example.x_tour;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewPlaceDetails extends Fragment {
    private static final String apiKey = "AIzaSyCCRv9GRsmztETeNJI8iASrooelW2a1zrU";
    private Toolbar toolbar;
    private FloatingActionButton btnBookmark;
    private ImageView ivPhoto;
    private TextView tvType, tvStatus, tvOpeningHours, tvIsOpen, tvTotalRatings, tvPrice, tvAddress, tvPhoneNum, tvWebsite;
    private RatingBar ratings;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.places_details, container, false);

        toolbar = v.findViewById(R.id.toolbar);
        btnBookmark = v.findViewById(R.id.btnBookmark);
        ivPhoto = v.findViewById(R.id.ivPhoto);
        tvType = v.findViewById(R.id.tvType);
        tvStatus = v.findViewById(R.id.tvStatus);
        tvOpeningHours = v.findViewById(R.id.tvOpeningHours);
        tvIsOpen = v.findViewById(R.id.tvIsOpen);
        tvTotalRatings = v.findViewById(R.id.tvTotalRatings);
        tvPrice = v.findViewById(R.id.tvPrice);
        tvAddress = v.findViewById(R.id.tvAddress);
        tvPhoneNum = v.findViewById(R.id.tvPhoneNum);
        tvWebsite = v.findViewById(R.id.tvWebsite);
        ratings = v.findViewById(R.id.ratings);
        dbHelper = new DatabaseHelper(getActivity());

        setHasOptionsMenu(true);

        String userID = String.valueOf(getActivity().getIntent().getExtras().getInt("userID"));

        // initialize Google Places SDK
        Places.initialize(getActivity(), apiKey);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(getActivity());

        // fetch the placeID
        final String placeID = getArguments().getString("placeID", "default");

        // specify the fields to return
        final List<Place.Field> placeFields = Arrays.asList(
                Place.Field.PHOTO_METADATAS,
                Place.Field.NAME,
                Place.Field.TYPES,
                Place.Field.ICON_URL,
                Place.Field.BUSINESS_STATUS,
                Place.Field.OPENING_HOURS,
                Place.Field.UTC_OFFSET,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL,
                Place.Field.PRICE_LEVEL,
                Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER,
                Place.Field.WEBSITE_URI
        );

        // Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            // set Place name
            toolbar.setTitle(place.getName());
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            // set Place photo
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w(TAG, "No photo metadata.");
            }
            final PhotoMetadata photoMetadata = metadata.get(0);

            // create a FetchPhotoRequest
            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                ivPhoto.setImageBitmap(bitmap);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                    final int statusCode = apiException.getStatusCode();
                    // TODO: Handle error with given status code.
                }
            });

            // set Place type
            List<Place.Type> placeTypes = place.getTypes();
            tvType.setText(capitalize(placeTypes.get(0).toString().replaceAll("_", " ")));

            // set Place icon
            final String iconUrl = place.getIconUrl();
            Glide.with(getActivity()).load(iconUrl).into((Target<Drawable>) (new CustomTarget(50, 50) {
                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    tvType.setCompoundDrawablesWithIntrinsicBounds(placeholder, null, null, null);
                }

                @Override
                public void onResourceReady(@NonNull Object resource, @Nullable Transition transition) {
                    tvType.setCompoundDrawablesWithIntrinsicBounds((Drawable) resource, null, null, null);
                }
            }));

            // set Place business status
            tvStatus.setText(place.getBusinessStatus().toString());

            // set Place opening hours
            tvOpeningHours.setText(place.getOpeningHours().getWeekdayText().toString()
                    .replaceAll(",", "\n")
                    .replace("[", " ")
                    .replace("]", ""));

            // set Place isOpen status
            if (place.isOpen()) {
                tvIsOpen.setText("OPEN");
                tvIsOpen.setTextColor(Color.parseColor("#599b61"));
            }
            else {
                tvIsOpen.setText("CLOSED");
                tvIsOpen.setTextColor(Color.parseColor("#95190C"));
            }

            // set Place rating
            ratings.setRating(place.getRating().floatValue());

            // set Place rating count
            tvTotalRatings.setText(place.getUserRatingsTotal() + " ratings");

            // set Place price level
            final Integer priceLevel = place.getPriceLevel();
            if (priceLevel == null)
                tvPrice.setText("No price information");
            else if (priceLevel.equals(0))
                tvPrice.setText("Free");
            else if (priceLevel.equals(1))
                tvPrice.setText("Cheap");
            else if (priceLevel.equals(2))
                tvPrice.setText("Moderate");
            else if (priceLevel.equals(3))
                tvPrice.setText("Expensive");
            else if (priceLevel.equals(4))
                tvPrice.setText("High-end");

            // set Place address
            tvAddress.setText(place.getAddress());

            // set Place phone number
            final String placePhoneNum = place.getPhoneNumber();
            if (placePhoneNum == null)
                tvPhoneNum.setText("No phone number available");
            else
                tvPhoneNum.setText(placePhoneNum);

            // set Place website uri
            final Uri placeWebsite = place.getWebsiteUri();
            if (placeWebsite == null)
                tvWebsite.setText("No website available");
            else
                tvWebsite.setText(placeWebsite.toString());

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException){
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });

        for (int i = 0; i < dbHelper.getAllBookmarks(userID).size(); i++) {
            if (dbHelper.getAllBookmarks(userID).get(i).equals(placeID))
                btnBookmark.setImageResource(R.drawable.ic_baseline_bookmark_added_24);
        }

        btnBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save place
                Boolean isBookmarked = dbHelper.checkIfBookmarked(userID, placeID);
                if (!isBookmarked) {
                    Boolean insert = dbHelper.insertBookmarkData(userID, placeID);
                    if (insert) {
                        btnBookmark.setImageResource(R.drawable.ic_baseline_bookmark_added_24);
                        Toast.makeText(getActivity().getApplicationContext(), "Bookmark saved", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Boolean delete = dbHelper.deleteBookmarkData(userID, placeID);
                    if (delete) {
                        btnBookmark.setImageResource(R.drawable.ic_baseline_bookmark_add_24);
                        Toast.makeText(getActivity().getApplicationContext(), "Bookmark removed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tvIsOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tvOpeningHours.getVisibility() == View.GONE)
                    tvOpeningHours.setVisibility(View.VISIBLE);
                else if (tvOpeningHours.getVisibility() == View.VISIBLE)
                    tvOpeningHours.setVisibility(View.GONE);
            }
        });

        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + tvAddress.getText().toString());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        tvPhoneNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tvPhoneNum.getText() != "No phone number available") {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + tvPhoneNum.getText()));
                    startActivity(intent);
                }
            }
        });

        tvWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(tvWebsite.getText().toString()));
                startActivity(Intent.createChooser(intent, "Title"));
            }
        });

        return v;
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
