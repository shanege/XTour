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

public class BookmarksFragment extends Fragment {

    private ArrayList<PlacesIDs> placesIDsArrayList;
    private RecyclerView bookmarkedPlacesList;
    private PlacesAdapter placesAdapter;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        bookmarkedPlacesList = v.findViewById(R.id.bookmarkedPlacesList);
        bookmarkedPlacesList.setHasFixedSize(false);
        bookmarkedPlacesList.setLayoutManager(new LinearLayoutManager(getActivity()));

        placesIDsArrayList = new ArrayList<>();
        dbHelper = new DatabaseHelper(getActivity());
        String userID = String.valueOf(getActivity().getIntent().getExtras().getInt("userID"));

        for (int i = 0; i < dbHelper.getAllBookmarks(userID).size(); i++) {
            placesIDsArrayList.add(new PlacesIDs(
                    dbHelper.getAllBookmarks(userID).get(i))
            );
        }

        placesAdapter = new PlacesAdapter(getActivity(), placesIDsArrayList);
        bookmarkedPlacesList.setAdapter(placesAdapter);

        return v;
    }
}
