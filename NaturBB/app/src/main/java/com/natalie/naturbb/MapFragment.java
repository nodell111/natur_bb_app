package com.natalie.naturbb;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;
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

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    //    private String intent_extra;
    private SearchView searchView;
    private boolean isMarkerAdded = false;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        searchView = getActivity().findViewById(R.id.searchbar);

        // Retrieve the value from arguments
//        intent_extra = getArguments().getString("name_extra");

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

//        setupSearchViewMap();

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
    @SuppressLint("MissingPermission")
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

            // Add a marker for the user's current location
//            mMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                    .position(userLatLng)
//                    .title("Your Location"));

//            LatLngBounds.Builder builder = LatLngBounds.builder();
//            builder.include(userLatLng);

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

        //Remember to change to listfragment or ListFragment when testing sort distance
        SQLiteDatabase database = ListFragment.dbHelper.getDataBase();

        //app crashes is trying to run Show All maps and Cursor single query at the same time
//        //if intent_extra is not null, let Cursor query run
//        if (intent_extra != null) {
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
        // only for when we add all parks to the map
//        else {
        //query everything from table
        Cursor dbCursor = database.rawQuery("SELECT * FROM natur_table_park;", null);

        //after the query the cursor is at the bottom
        // bring the cursor back to first record because you need to iterate through again
        dbCursor.moveToFirst();

        LatLngBounds.Builder builder = LatLngBounds.builder();
        // so far empty but then can feed it in the for loop

        ClusterManager clusterManager = new ClusterManager<Park>(getContext(), googleMap);
        DefaultClusterRenderer mapRenderer = new MapMarkersRenderer(getContext(), googleMap, clusterManager);
        clusterManager.setRenderer(mapRenderer);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);

        for (int i = 0; i < dbCursor.getCount(); i++) {

            LatLng park_pos = new LatLng(dbCursor.getDouble(4), dbCursor.getDouble(5));
            Log.d("park pos", park_pos.toString());
//            mMap.addMarker(new MarkerOptions()
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_dot_solid))
//                                    .position(park_pos)
//                                    .title(dbCursor.getString(0))
//                            //set title at name that has been stored in intent_extra
////                            .snippet(dbCursor.getString(1))
//                    )
//                    .setTag(0);
            clusterManager.addItem(new Park(park_pos, dbCursor.getString(0), dbCursor.getString(1)));

            //include bounds of the data record
            builder.include(park_pos);

            //need to move cursor to next line until the end
            dbCursor.moveToNext();

        }
        clusterManager.cluster();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        //zoom into bounds of all park_pos points
//    }
        GeoJsonLayer layer = null;
        try {
            layer = new GeoJsonLayer(mMap, R.raw.naturbb_parkboundary, getActivity());
//            GeoJsonPolygonStyle polyStyle = layer.getDefaultPolygonStyle();
//            polyStyle.setFillColor(Color.GREEN);
//            polyStyle.setStrokeColor(Color.RED);
//            polyStyle.setStrokeWidth(4f);
//            layer.addLayerToMap();
//            LatLngBounds.Builder builder = LatLngBounds.builder();


            for (GeoJsonFeature feature : layer.getFeatures()) {
                if (feature.hasGeometry() && feature.getGeometry().getGeometryType().equals("MultiPolygon")) {
                    Log.d("polygon here", "");
                    Log.d("tag", feature.getProperty("name"));

                    for (GeoJsonPolygon polygon : ((GeoJsonMultiPolygon) feature.getGeometry()).getPolygons()) {
                        List<? extends List<LatLng>> polygonList = ((GeoJsonPolygon) polygon).getCoordinates();
                        for (List<LatLng> list : polygonList) {
                            for (LatLng latLng : list) {
                                builder.include(latLng);
                            }
                        }
                    }

                    GeoJsonPolygonStyle polygonStyle = new GeoJsonPolygonStyle();
                    polygonStyle.setFillColor(Color.argb(100, 0, 255, 0));
                    polygonStyle.setStrokeColor(Color.RED);
                    feature.setPolygonStyle(polygonStyle);

                }
            }
            LatLngBounds bounds = builder.build();
            /**create the camera with bounds and padding to set into map*/
            final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
            /**call the map call back to know map is loaded or not*/
            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    /**set animated zoom camera into map*/
                    googleMap.animateCamera(cu);
                }
            });
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        GeoJsonLayer finalLayer = layer;
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                clusterManager.onCameraIdle();

                float zoomLevel = googleMap.getCameraPosition().zoom;
                Log.d("zoom level", String.valueOf(zoomLevel));
                if (zoomLevel > 8 && finalLayer != null) {
                    finalLayer.addLayerToMap();
                } else {
                    finalLayer.removeLayerFromMap();
                }
            }
        });
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


//    //Filter map when user searches for a park - STILL BUGGY, stops working for list view after
//    //search happens in map view
//    private void setupSearchViewMap() {
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // Handle the query submission if needed
//                return false;
//                public boolean onQueryTextChange (String newText){
//                    String keyword = searchView.getQuery().toString();
//
//                    SQLiteDatabase database = ListFragment.dbHelper.getDataBase();
//
//                    //query everything from table
//                    Cursor dbCursor;
//
//                    if (TextUtils.isEmpty(keyword)) {
//                        // If the keyword is empty, show all points
//                        dbCursor = database.rawQuery("SELECT * FROM natur_table_park;", null);
//                    } else {
//                        // If there is a keyword, filter the results
//                        dbCursor = database.rawQuery(
//                                "SELECT * FROM natur_table_park WHERE region LIKE ? ORDER BY region asc",
//                                new String[]{"%" + keyword + "%"}
//                        );
//                    }
//                    dbCursor.moveToFirst();
//                    LatLngBounds.Builder builder = LatLngBounds.builder();
//
//                    for (int i = 0; i < dbCursor.getCount(); i++) {
//                        LatLng park_pos = new LatLng(dbCursor.getDouble(4), dbCursor.getDouble(5));
//                        Marker marker = mMap.addMarker(new MarkerOptions()
//                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_dot_solid))
//                                        .position(park_pos)
//                                        //position marker at latlng of park_pos
//                                        .title(dbCursor.getString(0))
//                                //set title at name that has been stored in intent_extra
////                            .snippet(dbCursor.getString(1))
//                        );
//                        marker.setTag(0);
//                        //include bounds of the data record
//                        builder.include(park_pos);
//                        //need to move cursor to next line until the end
//                        dbCursor.moveToNext();
//                        //zoom into bounds of all park_pos points
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(park_pos, 12));
//                    }
//
//                    // Move the camera to show all points
//                    if (TextUtils.isEmpty(keyword)) {
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
//                    }
//
//                    return true;
//                }
//            }
//        });
//    }

}