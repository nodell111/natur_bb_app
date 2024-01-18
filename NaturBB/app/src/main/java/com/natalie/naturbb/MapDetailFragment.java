package com.natalie.naturbb;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon;
import com.google.maps.android.data.geojson.GeoJsonPolygon;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class MapDetailFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String parkName;
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

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentwindow, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));

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
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10));
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
        Log.d("in map detail fragment", "");

        dbCursor.moveToFirst();
        LatLngBounds.Builder builder = LatLngBounds.builder();
        LatLng point_pos = null;
//        some parks do not have point of interests
        if (dbCursor.getCount() > 0) {
            ClusterManager clusterManager = new ClusterManager<Park>(getContext(), googleMap);
            DefaultClusterRenderer mapRenderer = new MapMarkersRenderer(getContext(), googleMap, clusterManager);
            clusterManager.setRenderer(mapRenderer);
            googleMap.setOnCameraIdleListener(clusterManager);
            googleMap.setOnMarkerClickListener(clusterManager);

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
                clusterManager.cluster();
                //need to move cursor to next line until the end
                dbCursor.moveToNext();
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        } else {
            dbCursor = database.rawQuery("SELECT * FROM natur_table_park WHERE region = ?",
                    new String[]{parkName});
            dbCursor.moveToFirst();
            point_pos = new LatLng(dbCursor.getDouble(4), dbCursor.getDouble(5));
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_dot_solid))
                    .position(point_pos)
                    .title(parkName)
                    .snippet("Sorry there is no information. Explore yourself, have fun!")
            );
            builder.include(point_pos);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point_pos, 11));
        }
        boolean parkBoundaryAdded = false;
        try {
            GeoJsonLayer layer = new GeoJsonLayer(mMap, R.raw.naturbb_parkboundary, getActivity());;
            for (GeoJsonFeature feature : layer.getFeatures()) {
                GeoJsonPolygonStyle polygonStyle = new GeoJsonPolygonStyle();
                Log.d("match feature name", String.valueOf(feature.getProperty("name").matches(parkName)));
                if (feature.getProperty("name").matches(parkName) && feature.hasGeometry() && feature.getGeometry().getGeometryType().equals("MultiPolygon")) {
                    for (GeoJsonPolygon polygon : ((GeoJsonMultiPolygon) feature.getGeometry()).getPolygons()) {
                        List<? extends List<LatLng>> polygonList = ((GeoJsonPolygon) polygon).getCoordinates();
                        for (List<LatLng> list : polygonList) {
                            for (LatLng latLng : list) {
                                builder.include(latLng);
                            }
                        }
                    }
                    polygonStyle.setFillColor(Color.argb(60, 88, 129, 89));
                    polygonStyle.setStrokeColor(Color.argb(80, 54, 100, 14));
                    polygonStyle.setStrokeWidth(9);
                    parkBoundaryAdded = true;
                } else {
                    polygonStyle.setVisible(false);
                }
                feature.setPolygonStyle(polygonStyle);
            }
            layer.addLayerToMap();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if(parkBoundaryAdded) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        }
    }
}
