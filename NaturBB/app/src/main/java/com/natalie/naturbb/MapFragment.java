package com.natalie.naturbb;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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


public class MapFragment extends Fragment implements OnMapReadyCallback, ListFragmentListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private SearchView searchView;
    private Park clickedClusterItem;
    private boolean isMarkerAdded = false;
    private boolean mapReady = false;
    private String pendingSearchQuery;


    private SearchViewModel searchViewModel;
    private ListFragmentListener listFragmentListener;

    @Override
    public void setListFragmentListener(ListFragmentListener listener) {
        this.listFragmentListener = listener;

    }
    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleRadioGroupOff();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        ListFragment listFragment = (ListFragment)  getParentFragmentManager().findFragmentById(R.id.list);
        if (listFragment != null) {
            listFragment.setListFragmentListener(this); // 'this' refers to the MapFragment itself
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        searchView = getActivity().findViewById(R.id.searchbar);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe search query changes
        searchViewModel.getSearchQuery().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String newQuery) {
                // Handle search query changes, e.g., update list with new data
                handleSearch(newQuery);

            }
        });
    }

    @Override
    public void handleSearch(String query) {
        // Check if the map is ready
        if (mapReady) {
            updateMarkers(query);
        } else {
            // If the map is not ready, store the query and handle it later in onMapReady
            pendingSearchQuery = query;
        }    }


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

        mapReady = true;
        isMarkerAdded = false; // Initialize the marker added flag

        searchView.clearFocus();


        Location userLocation = ((MainActivity) requireActivity()).getUserLocation();

        if (userLocation != null) {
            LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

            mMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
                public void onMyLocationClick(@NonNull Location location) {
                    if (!isMarkerAdded) { // Check if the marker has not been added
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        // Add a marker for the user's current location
                        Marker userLocationMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).alpha(0.7f).position(userLatLng).title("Your Location"));

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

        // Handle pending search query if exists
        if (pendingSearchQuery != null) {
            updateMarkers(pendingSearchQuery);
            pendingSearchQuery = null; // Clear the pending query
        } else {
            loadAllParks();
        }

    }

    private void loadAllParks() {
        //Load all markers and boundaries for parks

        SQLiteDatabase database = ListFragment.dbHelper.getDataBase();

        //query everything from table
        Cursor dbCursor = database.rawQuery("SELECT * FROM natur_table_park;", null);

        //after the query the cursor is at the bottom
        // bring the cursor back to first record because you need to iterate through again
        dbCursor.moveToFirst();

        LatLngBounds.Builder builder = LatLngBounds.builder();
        // so far empty but then can feed it in the for loop

        GeoJsonLayer layer = null;
        try {
            layer = new GeoJsonLayer(mMap, R.raw.naturbb_parkboundary, getActivity());
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
                    polygonStyle.setFillColor(Color.argb(60, 88, 129, 89));
                    polygonStyle.setStrokeColor(Color.argb(80, 54, 100, 14));
                    polygonStyle.setStrokeWidth(9);
                    feature.setPolygonStyle(polygonStyle);
                }
            }
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        GeoJsonLayer finalLayer = layer;

        ClusterManager clusterManager = new ClusterManager<Park>(getContext(), mMap);
        for (int i = 0; i < dbCursor.getCount(); i++) {

            LatLng park_pos = new LatLng(dbCursor.getDouble(4), dbCursor.getDouble(5));
            clusterManager.addItem(new Park(park_pos, dbCursor.getString(0), dbCursor.getString(1)));

            //include bounds of the data record
            builder.include(park_pos);

            //need to move cursor to next line until the end
            dbCursor.moveToNext();

        }
        clusterManager.cluster();
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                clusterManager.onCameraIdle();
                float zoomLevel = mMap.getCameraPosition().zoom;
                Log.d("zoom level", String.valueOf(zoomLevel));
                if (zoomLevel > 8 && finalLayer != null) {
                    finalLayer.addLayerToMap();
                } else {
                    finalLayer.removeLayerFromMap();
                }
            }
        });
        DefaultClusterRenderer mapRenderer = new MapMarkersRenderer(getContext(), mMap, clusterManager);
        clusterManager.setRenderer(mapRenderer);
        clusterManager.setOnClusterClickListener((ClusterManager.OnClusterClickListener<Park>) cluster -> {
//                        Toast.makeText(getActivity(), "Cluster click", Toast.LENGTH_SHORT).show();
            Log.e("cluster", "clicked");
            return false;
        });
        //            mapRenderer.getMarker(item).showInfoWindow(new CustomInfoWindowAdapter(getContext(),item.()));
//            return false;
        clusterManager.setOnClusterItemClickListener((ClusterManager.OnClusterItemClickListener<Park>) item -> {
            clickedClusterItem = item;
            Log.e("cluster item", item.getSnippet());
            Log.e("cluster item stored", clickedClusterItem.getSnippet());
            Log.e("cluster item", "clicked");
            return false;
        });


        clusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoWindowAdapter2());
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        clusterManager.setOnClusterItemInfoWindowClickListener((ClusterManager.OnClusterItemInfoWindowClickListener<Park>) clusterItem -> {
//                Toast.makeText(getContext(), "Clicked info window: " + stringClusterItem.name,
//                        Toast.LENGTH_SHORT).show();

            Log.e("cluster item stored HEREEEE", clickedClusterItem.getSnippet());
            showListBottomSheetFragment(clusterItem.name);
        });
        mMap.setOnInfoWindowClickListener(clusterManager);

    }
    private void showListBottomSheetFragment(String parkName) {

        ListBottomSheetFragment bottomSheetFragment = new ListBottomSheetFragment();
        // Retrieve other details for the park (description, info, etc.)
        GetBottomSheetData getBottomSheetData = new GetBottomSheetData(parkName);
        String description = getBottomSheetData.description;
        String info = getBottomSheetData.info;
        String parkImage = getBottomSheetData.park_image;

        // Pass data to the fragment using a bundle
        Bundle bundle = new Bundle();
        bundle.putString("park_name", parkName);
        bundle.putString("park_image", parkImage);
        bundle.putString("description", description);
        bundle.putString("info", info);
        bottomSheetFragment.setArguments(bundle);

        // Show the bottom sheet fragment
        bottomSheetFragment.show(getParentFragmentManager(), bottomSheetFragment.getTag());

        // Clear focus from the search view (if needed)
        searchView.clearFocus();
    }


    //with clustering
    public void updateMarkers(String keyword) {

        // Check if the map is not empty before clearing
        if (mMap != null) {
            mMap.clear(); // Clear existing markers on the map
        }

        // Get the database instance from the MainActivity
        SQLiteDatabase database = ListFragment.dbHelper.getDataBase();

        // Query everything from the table
        Cursor dbCursor;

        if (TextUtils.isEmpty(keyword)) {
            // If the keyword is empty, clear the ClusterManager and reload the map
            loadAllParks(); //implement this method to load all markers
            searchView.clearFocus();
        } else {
            // If there is a keyword, filter the results
            dbCursor = database.rawQuery(
                    "SELECT * FROM natur_table_park WHERE region LIKE ? ORDER BY region asc",
                    new String[]{"%" + keyword + "%"}
            );

            dbCursor.moveToFirst();

            LatLngBounds.Builder builder = LatLngBounds.builder();

            GeoJsonLayer layer = null;
            try {
                layer = new GeoJsonLayer(mMap, R.raw.naturbb_parkboundary, getActivity());
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
                        polygonStyle.setFillColor(Color.argb(60, 88, 129, 89));
                        polygonStyle.setStrokeColor(Color.argb(80, 54, 100, 14));
                        polygonStyle.setStrokeWidth(9);
                        feature.setPolygonStyle(polygonStyle);
                    }
                }
                LatLngBounds bounds = builder.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            GeoJsonLayer finalLayer = layer;


            ClusterManager clusterManager = new ClusterManager<Park>(getContext(), mMap);
            for (int i = 0; i < dbCursor.getCount(); i++) {

                LatLng park_pos = new LatLng(dbCursor.getDouble(4), dbCursor.getDouble(5));
                clusterManager.addItem(new Park(park_pos, dbCursor.getString(0), dbCursor.getString(1)));

                //include bounds of the data record
                builder.include(park_pos);

                //need to move cursor to next line until the end
                dbCursor.moveToNext();

            }
            clusterManager.cluster();
            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    clusterManager.onCameraIdle();
                    float zoomLevel = mMap.getCameraPosition().zoom;
                    Log.d("zoom level", String.valueOf(zoomLevel));
                    if (zoomLevel > 8 && finalLayer != null) {
                        finalLayer.addLayerToMap();
                    } else {
                        finalLayer.removeLayerFromMap();
                    }
                }
            });
            DefaultClusterRenderer mapRenderer = new MapMarkersRenderer(getContext(), mMap, clusterManager);
            clusterManager.setRenderer(mapRenderer);
            clusterManager.setOnClusterClickListener((ClusterManager.OnClusterClickListener<Park>) cluster -> {
//                        Toast.makeText(getActivity(), "Cluster click", Toast.LENGTH_SHORT).show();
                Log.e("cluster", "clicked");
                return false;
            });
            //            mapRenderer.getMarker(item).showInfoWindow(new CustomInfoWindowAdapter(getContext(),item.()));
//            return false;
            clusterManager.setOnClusterItemClickListener((ClusterManager.OnClusterItemClickListener<Park>) item -> {
                clickedClusterItem = item;
                Log.e("cluster item", item.getSnippet());
                Log.e("cluster item stored", clickedClusterItem.getSnippet());
                Log.e("cluster item", "clicked");
                return false;
            });


            clusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoWindowAdapter2());
            mMap.setOnMarkerClickListener(clusterManager);
            mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
            clusterManager.setOnClusterItemInfoWindowClickListener((ClusterManager.OnClusterItemInfoWindowClickListener<Park>) clusterItem -> {
//                Toast.makeText(getContext(), "Clicked info window: " + stringClusterItem.name,
//                        Toast.LENGTH_SHORT).show();

                Log.e("cluster item stored HEREEEE", clickedClusterItem.getSnippet());
                showListBottomSheetFragment(clusterItem.name);
            });
            mMap.setOnInfoWindowClickListener(clusterManager);

        }
    }

    @Override
    public ArrayAdapter<CharSequence> createAdapterHtml(Cursor cursor) {
        return null;
    }


    public class CustomInfoWindowAdapter2 implements GoogleMap.InfoWindowAdapter {
        //        private LayoutInflater inflater;
        //        private ViewGroup container;
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            Log.e("call", "get info contents");
            // Getting view from the layout file info_window_layout
            View v = LayoutInflater.from(getContext()).inflate(R.layout.window_layout, null, false);
            TextView tvName = v.findViewById(R.id.tv_name);
            TextView tvSnippet = v.findViewById(R.id.tv_snippet);
            String parkName = "Not Found";
            String snippet = "";
            if (clickedClusterItem != null) {
                Log.e("call", "not null!");
                parkName = clickedClusterItem.getTitle();
                snippet = clickedClusterItem.getSnippet();
            }
            // Setting the latitude
            tvName.setText(parkName);
            // Setting the longitude
            tvSnippet.setText(snippet);
            return v;
        }

        @Override
        public View getInfoContents(@NonNull Marker marker) {
            return null;
        }
    }

    private void toggleRadioGroupOff() {
        Log.d("Toggle", "Toggling radio group off");

        RadioGroup radioGroup = getActivity().findViewById(R.id.radioGroup);
        SearchView searchView1 = getActivity().findViewById(R.id.searchbar);
        TextView sortBy = getActivity().findViewById(R.id.sortBy);

        // Iterate through each child in the RadioGroup
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View child = radioGroup.getChildAt(i);

            // Check if the child is a Switch
            if (child instanceof Switch) {
                Switch switchView = (Switch) child;
                // Set the checked state of the Switch to false
                switchView.setChecked(false);
                // Disable switches
                switchView.setEnabled(false);
            }
        }

        // Disable the entire RadioGroup to prevent user interaction
        radioGroup.setEnabled(false);
        radioGroup.setVisibility(View.GONE);
        sortBy.setVisibility(View.GONE);
//        searchView1.setQueryHint("Sorry, not working for map :(");
    }
}

