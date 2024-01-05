package com.natalie.naturbb;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.natalie.naturbb.fragments.MapsFragment;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_maps);

        if (savedInstanceState == null) {
            // Get data from the Intent
            String parkName = getIntent().getStringExtra("name_extra");

            // Create a new MapsFragment instance
            MapsFragment mapsFragment = new MapsFragment();

            // Pass the data to the fragment using arguments
            Bundle args = new Bundle();
            args.putString("name_extra", parkName);
            mapsFragment.setArguments(args);

            // Load the fragment into the activity
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.map, mapsFragment) // Use your container ID
                    .commit();
        }
    }
}

