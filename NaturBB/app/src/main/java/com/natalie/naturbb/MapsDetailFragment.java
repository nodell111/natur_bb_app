package com.natalie.naturbb;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.natalie.naturbb.fragments.listfragment_dist;

public class MapsDetailFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    private boolean isMarkerAdded = false;


    public MapsDetailFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapsdetail, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.mapsdetail, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        return view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        Location userLocation = ((MainActivity) requireActivity()).getUserLocation();

        if (userLocation != null) {
            LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

            LatLngBounds.Builder builder = LatLngBounds.builder();
            builder.include(userLatLng);

            // Move the camera to the user's location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));

            mMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
                public void onMyLocationClick(@NonNull Location location) {
                    if (!isMarkerAdded) { // Check if the marker has not been added
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        // Add a marker for the user's current location
                        Marker userLocationMarker = mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                .alpha(0.7f)
                                .position(userLatLng)
                                .title("Your Location"));

                        userLocationMarker.showInfoWindow(); // Show the info window

                        isMarkerAdded = true; // Set the marker added flag to true
                    }
                }
            });


            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10));

                    return true;
                }
            });

        } else {
            // Handle the case when user location is not available
            Toast.makeText(requireContext(), "User location not available", Toast.LENGTH_SHORT).show();
        }

        SQLiteDatabase database = listfragment_dist.dbHelper.getDataBase();

        Cursor dbCursor = database.rawQuery("SELECT * FROM natur_table", null);
//
        dbCursor.moveToFirst();

        LatLngBounds.Builder builder = LatLngBounds.builder();

        for (int i = 0; i < dbCursor.getCount(); i++) {
            String latLngString = dbCursor.getString(5); // Assuming column 5 contains "lat,lng"

            if (latLngString != null && !latLngString.isEmpty()) {
                String[] latLngArray = latLngString.split(",");
                if (latLngArray.length == 2) {
                    double lat = Double.parseDouble(latLngArray[0].trim());
                    double lng = Double.parseDouble(latLngArray[1].trim());

                    LatLng poi_pos = new LatLng(lat, lng);

                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_dot_solid))
                            .position(poi_pos)
                            .title(dbCursor.getString(0))
                            .snippet(dbCursor.getString((12)))
                    );

                    // Include bounds of the data record
                    builder.include(poi_pos);
                }
            }
            //need to move cursor to next line until the end
            dbCursor.moveToNext();

        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));

        }



    }
