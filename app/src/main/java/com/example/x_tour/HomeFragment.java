package com.example.x_tour;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

public class HomeFragment extends Fragment {

    private static final String apiKey = "AIzaSyCCRv9GRsmztETeNJI8iASrooelW2a1zrU";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // initialize Google Places SDK
        Places.initialize(getActivity().getApplicationContext(), apiKey);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(getActivity());

        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}
