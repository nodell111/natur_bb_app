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

import com.google.android.gms.maps.CameraUpdate;
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


    // Set the ListFragmentListener for communication between fragments
    @Override
    public void setListFragmentListener(ListFragmentListener listener) {
        this.listFragmentListener = listener;

    }

    // Default constructor for MapFragment
    public MapFragment() {
        // Required empty public constructor
    }


    // Called when the fragment is resumed; toggle radio group off
    @Override
    public void onResume() {
        super.onResume();
        toggleRadioGroupOff();
    }

    // Called when the fragment is created; initialize ViewModel and set ListFragmentListener
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        // Set ListFragmentListener to communicate with the ListFragment
        ListFragment listFragment = (ListFragment) getParentFragmentManager().findFragmentById(R.id.list);
        if (listFragment != null) {
            listFragment.setListFragmentListener(this); // 'this' refers to the MapFragment itself
        }

    }


    // Called when the fragment view is created; inflate layout and initialize map
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        searchView = getActivity().findViewById(R.id.searchbar);

        // Initialize or retrieve the SupportMapFragment
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        return view;
    }

    // Called after the view is created; observe search query changes
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

    // Handle search query changes
    @Override
    public void handleSearch(String query) {
        // Check if the map is ready
        if (mapReady) {
            updateMarkers(query);
        } else {
            // If the map is not ready, store the query and handle it later in onMapReady
            pendingSearchQuery = query;
        }
    }


    // Called when the map is ready; set up map features and handle pending search query
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

        // Get user location from main activity
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


            //On my location button click, move camera to current location
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
        //Bounds for map, so far empty but then can feed it in the for loop

        GeoJsonLayer layer = null;
        try {
            layer = new GeoJsonLayer(mMap, R.raw.naturbb_parkboundary, getActivity());
            for (GeoJsonFeature feature : layer.getFeatures()) {
                if (feature.hasGeometry() && feature.getGeometry().getGeometryType().equals("MultiPolygon")) {
                    for (GeoJsonPolygon polygon : ((GeoJsonMultiPolygon) feature.getGeometry()).getPolygons()) {
                        List<? extends List<LatLng>> polygonList = polygon.getCoordinates();
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
        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150);
        mMap.animateCamera(cu);
        clusterManager.cluster();
        mMap.setOnCameraIdleListener(() -> {
            clusterManager.onCameraIdle();
            float zoomLevel = mMap.getCameraPosition().zoom;
            if (zoomLevel > 8 && finalLayer != null) {
                finalLayer.addLayerToMap();
            } else {
                finalLayer.removeLayerFromMap();
            }
        });
        // to customise the markers
        DefaultClusterRenderer mapRenderer = new MapMarkersRenderer(getContext(), mMap, clusterManager);
        clusterManager.setRenderer(mapRenderer);
        clusterManager.setOnClusterClickListener((ClusterManager.OnClusterClickListener<Park>) cluster -> false);
        clusterManager.setOnClusterItemClickListener((ClusterManager.OnClusterItemClickListener<Park>) item -> {
            clickedClusterItem = item;
            return false;
        });

        // to customise marker pop up window
        clusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        clusterManager.setOnClusterItemInfoWindowClickListener((ClusterManager.OnClusterItemInfoWindowClickListener<Park>) clusterItem -> {
            showListBottomSheetFragment(clusterItem.name);
        });
        mMap.setOnInfoWindowClickListener(clusterManager);

    }

    // Show the bottom sheet fragment with park details
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

    // Update markers based on the search keyword
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
            // add corresponding polygons
            try {
                layer = new GeoJsonLayer(mMap, R.raw.naturbb_parkboundary, getActivity());
                GeoJsonPolygonStyle polyStyle = layer.getDefaultPolygonStyle();
                polyStyle.setVisible(false);
                for (GeoJsonFeature feature : layer.getFeatures()) {
                    for (int i = 0; i < dbCursor.getCount(); i++) {
                        String parkName = dbCursor.getString(0);
                        if (feature.getProperty("name").matches(parkName) && feature.hasGeometry() && feature.getGeometry().getGeometryType().equals("MultiPolygon")) {
                            for (GeoJsonPolygon polygon : ((GeoJsonMultiPolygon) feature.getGeometry()).getPolygons()) {
                                List<? extends List<LatLng>> polygonList = polygon.getCoordinates();
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
                        dbCursor.moveToNext();
                    }
                    dbCursor.moveToFirst();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            ClusterManager clusterManager = new ClusterManager<Park>(getContext(), mMap);
            dbCursor.moveToFirst();
            int resultNumber = dbCursor.getCount();
            for (int i = 0; i < dbCursor.getCount(); i++) {

                LatLng park_pos = new LatLng(dbCursor.getDouble(4), dbCursor.getDouble(5));
                clusterManager.addItem(new Park(park_pos, dbCursor.getString(0), dbCursor.getString(1)));

                //include bounds of the data record
                builder.include(park_pos);

                //need to move cursor to next line until the end
                dbCursor.moveToNext();

            }
            if (resultNumber > 0) {
                final CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(), 8);
                mMap.animateCamera(cu);
            }
            clusterManager.cluster();

            GeoJsonLayer finalLayer = layer;
            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    clusterManager.onCameraIdle();
                    float zoomLevel = mMap.getCameraPosition().zoom;
                    if (zoomLevel > 8 && finalLayer != null) {
                        finalLayer.addLayerToMap();
                    } else {
                        finalLayer.removeLayerFromMap();
                    }
                }
            });
            // to customise the markers
            DefaultClusterRenderer mapRenderer = new MapMarkersRenderer(getContext(), mMap, clusterManager);
            clusterManager.setRenderer(mapRenderer);
            clusterManager.setOnClusterClickListener((ClusterManager.OnClusterClickListener<Park>) cluster -> false);
            clusterManager.setOnClusterItemClickListener((ClusterManager.OnClusterItemClickListener<Park>) item -> {
                clickedClusterItem = item;
                return false;
            });

            // to customise marker pop up window
            clusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoWindowAdapter());
            mMap.setOnMarkerClickListener(clusterManager);
            mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
            clusterManager.setOnClusterItemInfoWindowClickListener((ClusterManager.OnClusterItemInfoWindowClickListener<Park>) clusterItem -> {
                showListBottomSheetFragment(clusterItem.name);
            });
            mMap.setOnInfoWindowClickListener(clusterManager);
        }
    }

    @Override
    public ArrayAdapter<CharSequence> createAdapterHtml(Cursor cursor) {
        return null;
    }

    // Custom info window adapter for marker pop-up
    public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
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

    // Toggle off the radio group and disable switches
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
    }

}