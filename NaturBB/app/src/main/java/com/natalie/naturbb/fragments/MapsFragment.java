package com.natalie.naturbb.fragments;


import androidx.fragment.app.Fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.natalie.naturbb.MainActivity;
import com.natalie.naturbb.R;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private String intent_extra;
    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        // Retrieve the value from arguments
//        intent_extra = getArguments().getString("name_extra");

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        return view;
    }


    //KEEP THIS CODE FOR PROGRAMMING ONCLICK LIST ITEM THEN EXPLORE FULL PARK MAP
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//        mMap.setOnMarkerClickListener(this);
//
//        SQLiteDatabase database = listfragment.dbHelper.getDataBase();
//
//        //app crashes is trying to run Show All maps and Cursor single query at the same time
//        //if intent_extra is not null, let Cursor query run
//        if (intent_extra != null) {
//
//            Cursor dbCursor = database.rawQuery("SELECT * FROM natur_table_park WHERE Name LIKE '"+ intent_extra +"';", null);
//
//            dbCursor.moveToFirst();
//
//            LatLng park_pos = new LatLng (dbCursor.getDouble(4),dbCursor.getDouble(5));
//
//            mMap.addMarker(new MarkerOptions()
//                    .position(park_pos)
//                    //position marker at latlng of park_pos
//                    .title(intent_extra)
//                    //set title at name that has been stored in intent_extra
//                    .snippet(dbCursor.getString(1)));
//            //set snippet as url which is at index 1
//
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(park_pos, 14));
//            //zoom into clicked position park_pos
//        }
//        // only for when we add all universities to the map
//        else {
//            //query everything from table
//            Cursor dbCursor = database.rawQuery("SELECT * FROM natur_table_park;", null);
//
//            //after the query the cursor is at the bottom
//            // bring the cursor back to first record because you need to iterate through again
//            dbCursor.moveToFirst();
//
//            LatLngBounds.Builder builder = LatLngBounds.builder();
//            // so far empty but then can feed it in the for loop
//
//            for (int i = 0; i < dbCursor.getCount(); i++) {
//
//                LatLng park_pos = new LatLng(dbCursor.getDouble(4), dbCursor.getDouble(5));
//
//                mMap.addMarker(new MarkerOptions()
//                                .position(park_pos)
//                                //position marker at latlng of park_pos
//                                .title(dbCursor.getString(0))
//                                //set title at name that has been stored in intent_extra
//                                .snippet(dbCursor.getString(1)))
//                        .setTag(0);
//
//                //include bounds of the data record
//                builder.include(park_pos);
//
//                //need to move cursor to next line until the end
//                dbCursor.moveToNext();
//            }
//
//            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),200));
//            //zoom into bounds of all park_pos points
//        }
//
//    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
//        mMap.setOnMarkerClickListener(this);

        // Enable the "My Location" layer, which will show the user's current location on the map
//        mMap.setMyLocationEnabled(true);

        Location userLocation = ((MainActivity) requireActivity()).getUserLocation();

        if (userLocation != null) {
            LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

            // Add a marker for the user's current location
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .position(userLatLng)
                    .title("Your Location"));

            // Move the camera to the user's location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
        } else {
            // Handle the case when user location is not available
            Toast.makeText(requireContext(), "User location not available", Toast.LENGTH_SHORT).show();
        }


        //Remeber to change to lisfragment or listfragment_copy when testing sort distance
        SQLiteDatabase database = listfragment_dist.dbHelper.getDataBase();

        //query everything from table
        Cursor dbCursor = database.rawQuery("SELECT * FROM natur_table_park;", null);

        //after the query the cursor is at the bottom
        // bring the cursor back to first record because you need to iterate through again
        dbCursor.moveToFirst();

        LatLngBounds.Builder builder = LatLngBounds.builder();
        // so far empty but then can feed it in the for loop

        for (int i = 0; i < dbCursor.getCount(); i++) {

            LatLng park_pos = new LatLng(dbCursor.getDouble(4), dbCursor.getDouble(5));

            mMap.addMarker(new MarkerOptions()
//                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_dot_solid))
                                    .position(park_pos)
                            //position marker at latlng of park_pos
                            .title(dbCursor.getString(0))
                            //set title at name that has been stored in intent_extra
//                            .snippet(dbCursor.getString(1))
                    )
                    .setTag(0);

            //include bounds of the data record
            builder.include(park_pos);

            //need to move cursor to next line until the end
            dbCursor.moveToNext();

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
            //zoom into bounds of all park_pos points
        }

    }

    @Override
    public void onDestroy() {
        listfragment.dbHelper.close();
        super.onDestroy();
    }

//    @Override
//    //true: NOT centering on the marker;
//    //false: DOES centering on the marker;
//    public boolean onMarkerClick(@NonNull Marker marker) {
//        if (intent_extra != null) {
//            return false;
//        } else {
//
//            // change count from get tag into integer with (Integer)
////            Integer clickCount = (Integer) marker.getTag();
////            clickCount++;
////            marker.setTag(clickCount);
////            Toast.makeText(this,marker.getTitle()+" has been clicked " + clickCount + " times.", Toast.LENGTH_SHORT).show();
//
//            return true;
//
//        }
//    }
}