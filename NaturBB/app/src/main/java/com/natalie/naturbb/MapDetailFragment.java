package com.natalie.naturbb;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class MapDetailFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String parkName;
    private Park clickedClusterItem;
    SupportMapFragment mapFragment;
    private boolean isMarkerAdded = false;

    public MapDetailFragment(String parkName) {
        this.parkName = parkName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapdetail, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.fragmentwindow, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        return view;
    }

    @SuppressLint({"MissingPermission", "PotentialBehaviorOverride"})
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        Location userLocation = ((MainActivity) requireActivity()).getUserLocation();

        if (userLocation != null) {
            LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

            LatLngBounds.Builder builder = LatLngBounds.builder();
            builder.include(userLatLng);

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

        SQLiteDatabase database = ListFragment.dbHelper.getDataBase();

        Cursor dbCursor = database.rawQuery("SELECT * FROM natur_table WHERE region = ?",
                new String[]{parkName});

        dbCursor.moveToFirst();
        LatLngBounds.Builder builder = LatLngBounds.builder();
        LatLng point_pos = null;
        // some parks do not have point of interests
        ClusterManager clusterManager = new ClusterManager<Park>(getContext(), googleMap);
        boolean onePoint = (dbCursor.getCount() > 1) ? false : true;
        if (dbCursor.getCount() > 0) {

            for (int i = 0; i < dbCursor.getCount(); i++) {
                String latLngString = dbCursor.getString(5); // Assuming column 5 contains "lat,lng"
                if (latLngString != null && !latLngString.isEmpty()) {
                    String[] latLngArray = latLngString.split(",");
                    if (latLngArray.length == 2) {
                        double lat = Double.parseDouble(latLngArray[0].trim());
                        double lng = Double.parseDouble(latLngArray[1].trim());

                        point_pos = new LatLng(lat, lng);
                        builder.include(point_pos);
                        clusterManager.addItem(new Park(point_pos, dbCursor.getString(0), dbCursor.getString(12)));
                    }
                }
                // need to move cursor to next line until the end
                dbCursor.moveToNext();
            }

        } else {
            // position of the park is shown instead when there is no point of interest data
            dbCursor = database.rawQuery("SELECT * FROM natur_table_park WHERE region = ?",
                    new String[]{parkName});
            dbCursor.moveToFirst();
            point_pos = new LatLng(dbCursor.getDouble(4), dbCursor.getDouble(5));
            clusterManager.addItem(new Park(point_pos, parkName, "Sorry there is no information. Explore yourself, have fun!"));
            builder.include(point_pos);
        }
        clusterManager.cluster();
        mMap.setOnCameraIdleListener(clusterManager);
        // to customise the markers
        DefaultClusterRenderer mapRenderer = new MapMarkersRenderer(getContext(), googleMap, clusterManager);
        clusterManager.setRenderer(mapRenderer);
        clusterManager.setOnClusterClickListener((ClusterManager.OnClusterClickListener<Park>) cluster -> false);
        // to pass point of interest into the cluster
        clusterManager.setOnClusterItemClickListener((ClusterManager.OnClusterItemClickListener<Park>) item -> {
            clickedClusterItem = item;
            return false;
        });
        // to customise marker pop up window
        clusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        clusterManager.setOnClusterItemInfoWindowClickListener((ClusterManager.OnClusterItemInfoWindowClickListener<Park>) clusterItem -> {
            showMapBottomSheetFragment(clusterItem.name);
        });
        mMap.setOnInfoWindowClickListener(clusterManager);
        if(onePoint) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point_pos, 10));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        }
    }

    private void showMapBottomSheetFragment(String poiName) {

        MapBottomSheetFragment bottomSheetFragment = new MapBottomSheetFragment();
        // Retrieve other details for the poi (description, info, etc.)
        GetBottomSheetDataMapDetail getBottomSheetDataMapDetail = new GetBottomSheetDataMapDetail(poiName);
        String description = getBottomSheetDataMapDetail.description;
        String category = getBottomSheetDataMapDetail.category;
        String city = getBottomSheetDataMapDetail.city;

        // Pass data to the fragment using a bundle
        Bundle bundle = new Bundle();
        bundle.putString("poi_name", poiName);
        bundle.putString("description", description);
        bundle.putString("category", category);
        bundle.putString("city", city);
        bottomSheetFragment.setArguments(bundle);

        // Show the bottom sheet fragment
        bottomSheetFragment.show(getParentFragmentManager(), bottomSheetFragment.getTag());
    }
    public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            // Getting view from the layout file info_window_layout
            View v = LayoutInflater.from(getContext()).inflate(R.layout.window_layout, null, false);
            TextView tvName = v.findViewById(R.id.tv_name);
            TextView tvSnippet = v.findViewById(R.id.tv_snippet);
            String parkName = "Not Found";
            String snippet = "";
            if (clickedClusterItem != null) {
                parkName = clickedClusterItem.getTitle().trim();
                snippet = clickedClusterItem.getSnippet().trim();
            }
            tvName.setText(parkName);
            tvSnippet.setText(snippet);
            return v;
        }

        @Override
        public View getInfoContents(@NonNull Marker marker) {
            return null;
        }
    }

}
