package com.example.x_tour;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private ArrayList<PlacesIDs> placesIDsArrayList;
    private RecyclerView nearbyPlacesList;
    private PlacesAdapter placesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        nearbyPlacesList = v.findViewById(R.id.nearbyPlacesList);
        nearbyPlacesList.setHasFixedSize(false);
        nearbyPlacesList.setLayoutManager(new LinearLayoutManager(getActivity()));

        placesIDsArrayList = new ArrayList<>();

        for (int i = 0; i < NearbyPlacesIDs.placeIDs.length; i++) {
            placesIDsArrayList.add(new PlacesIDs(
                    NearbyPlacesIDs.placeIDs[i]
            ));
        }

        placesAdapter = new PlacesAdapter(getActivity(), placesIDsArrayList);
        nearbyPlacesList.setAdapter(placesAdapter);

        return v;
    }
}
