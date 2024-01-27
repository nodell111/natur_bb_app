package com.natalie.naturbb;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListFragment extends Fragment implements ListFragmentListener {

    public static DatabaseHelper dbHelper;
    // Declare variables
    private SQLiteDatabase database;
    private Cursor dbCursor;
    private ListView list_view;
    private SearchView searchView;
    private Switch switchSortSize;
    private Switch switchSortName;
    private Switch switchSortDistance;
    private SearchViewModel searchViewModel;
    private ListFragmentListener listFragmentListener;

    @Override
    public void onResume() {
        super.onResume();
        // Ensure that radio buttons are enabled when the fragment resumes
        toggleRadioGroupOn();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);

        ListFragment listFragment = (ListFragment) getParentFragmentManager().findFragmentById(R.id.list);
        if (listFragment != null) {
            listFragment.setListFragmentListener(this); // 'this' refers to the MapFragment itself
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);
        list_view = view.findViewById(R.id.parkList);
        //parkList is an adapter for list_view

        dbHelper = new DatabaseHelper(view.getContext());

        // Initialize DatabaseHelper and SQLiteDatabase in onCreateView
        try {
            dbHelper.createDataBase();
            //creating the database and prevent crash with try&catch
        } catch (IOException ioe) {
        }
        database = dbHelper.getDataBase();
        // Query the database for park information and populate the ListView
        dbCursor = database.rawQuery("SELECT * FROM natur_table_park ORDER BY region asc;", null);

        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        //create HTML list items with adapter
        list_view.setAdapter(adapter);
        //adapter needs the layout view file and data (the dbCursor points to the data records)

        // Set up item click listener for the ListView
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get park details from the clicked item and display a bottom sheet
                dbCursor.moveToPosition(position);
                String park_name = dbCursor.getString(0);
                String park_image = dbCursor.getString(3);
                //after getting park_name and park_image from db
                //we can run this function to show bottom sheet dialog
                //scroll down until you see this function to edit
                showListBottomSheetFragment(park_name, park_image);

            }
        });

        // Use getActivity() to find views in the activity's layout
        searchView = getActivity().findViewById(R.id.searchbar);
        switchSortSize = getActivity().findViewById(R.id.sortSize);
        switchSortName = getActivity().findViewById(R.id.sortName);
        switchSortDistance = getActivity().findViewById(R.id.sortDistance);

        setupSwitchGroup("");
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
                MapFragment mapFragment = (MapFragment) getParentFragmentManager().findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.handleSearch(newQuery);
                }
            }
        });
    }


    @Override
    public void setListFragmentListener(ListFragmentListener listener) {
        this.listFragmentListener = listener;
    }

    @Override
    public void handleSearch(String query) {
        // Perform search using the query
        // Update the list with the new data
        if (dbCursor != null) {
            dbCursor.close();
        }
        dbCursor = database.rawQuery(
                "SELECT * FROM natur_table_park WHERE region LIKE ? ORDER BY region ASC",
                new String[]{"%" + query + "%"}
        );
        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        if (list_view != null) {
            list_view.setAdapter(adapter);
        }
        setupSwitchGroup(query);
    }

    public ArrayAdapter<CharSequence> createAdapterHtml(Cursor cursor) {
        int length = cursor.getCount();
        cursor.moveToFirst();
        Spanned[] html_array = new Spanned[length];
        String[] image_names = new String[length]; // New array for image names
        int index_name = cursor.getColumnIndex("region");
        int image_name = cursor.getColumnIndex("image_name");


        for (int i = 0; i < length; i++) {
            html_array[i] = Html.fromHtml(cursor.getString(index_name));
            image_names[i] = cursor.getString(image_name);
            cursor.moveToNext();
        }

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getActivity(),
                R.layout.list_item,
                R.id.textViewItem,
                html_array
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // Set background drawable dynamically based on image_name
                String imageName = image_names[position];
                int resId = getResources().getIdentifier(imageName, "drawable", getActivity().getPackageName());
                // Get reference to the ImageView
                ImageView imageView = view.findViewById(R.id.imageViewItem);

                // Set the image resource dynamically
                imageView.setImageResource(resId);

                return view;
            }
        };

        return adapter;
    }

    private void showListBottomSheetFragment(String parkName, String parkImage) {
        ListBottomSheetFragment bottomSheetFragment = new ListBottomSheetFragment();

        GetBottomSheetData getBottomSheetData = new GetBottomSheetData(parkName);
        String description = getBottomSheetData.description;
        String info = getBottomSheetData.info;

        // Pass data to the fragment using a bundle
        Bundle bundle = new Bundle();
        bundle.putString("park_name", parkName);
        bundle.putString("park_image", parkImage);
        bundle.putString("description", description);
        bundle.putString("info", info);
        bottomSheetFragment.setArguments(bundle);

        bottomSheetFragment.show(getParentFragmentManager(), bottomSheetFragment.getTag());

        searchView.clearFocus();

    }


//    private void setupSearchView() {
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//
//                searchView.clearFocus();
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                String keyword = searchView.getQuery().toString();
//                if (dbCursor != null) {
//                    dbCursor.close();
//                }
//                dbCursor = database.rawQuery(
//                        "SELECT * FROM natur_table_park WHERE region LIKE ? ORDER BY region asc",
//                        new String[]{"%" + keyword + "%"}
//                );
//                ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
//                if (list_view != null) {
//                    list_view.setAdapter(adapter);
//                }
//
//                return true;
//            }
//        });
//
//
//    }


    // In the ListFragment or wherever you are using setupSearchView()
//    private void setupSearchView() {
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                searchView.clearFocus();
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                if (getActivity() instanceof MainActivity) {
//                    ((MainActivity) getActivity()).handleSearch(newText);
//                }
//                return true;
//            }
//        });
//    }

    //when switch for size is checked then sort list by size column
    private void setupSwitchGroup(String query) {
        switchSortSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSortName.setChecked(false);
                    switchSortDistance.setChecked(false);
                    sortListBySize(query);
                    switchSortSize.setEnabled(false);
                    switchSortName.setEnabled(true);
                    switchSortDistance.setEnabled(true);
                }
            }
        });

        switchSortName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSortSize.setChecked(false);
                    switchSortDistance.setChecked(false);
                    sortListByName(query);
                    switchSortName.setEnabled(false);
                    switchSortSize.setEnabled(true);
                    switchSortDistance.setEnabled(true);
                }
            }
        });

        switchSortDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSortSize.setChecked(false);
                    switchSortName.setChecked(false);
                    sortListByDistance(query);
                    switchSortDistance.setEnabled(false);
                    switchSortSize.setEnabled(true);
                    switchSortName.setEnabled(true);
                }
            }
        });
    }

    private void sortListBySize(String query) {
        if (dbCursor != null) {
            dbCursor.close();
        }

        dbCursor = database.rawQuery(
                "SELECT * FROM natur_table_park WHERE region LIKE ? ORDER BY area_km2 ASC",
                new String[]{"%" + query + "%"});

        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        if (list_view != null) {
            list_view.setAdapter(adapter);
        }
    }

    private void sortListByName(String query) {
        if (dbCursor != null) {
            dbCursor.close();
        }

        dbCursor = database.rawQuery(
                "SELECT * FROM natur_table_park WHERE region LIKE ? ORDER BY region ASC",
                new String[]{"%" + query + "%"});

        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        if (list_view != null) {
            list_view.setAdapter(adapter);
        }
    }

    private void sortListByDistance(String query) {
        if (dbCursor != null) {
            dbCursor.close();
        }

        dbCursor = database.rawQuery(
                "SELECT * FROM natur_table_park WHERE region LIKE ? ORDER BY region ASC",
                new String[]{"%" + query + "%"});

        // Retrieve user location from MainActivity
        Location userLocation = ((MainActivity) requireActivity()).getUserLocation();

        // Check if userLocation is not null
        if (userLocation != null) {

            // List to store ParkDistance objects
            List<ParkDistance> parkDistances = new ArrayList<>();

            // AsyncTask to handle API request in the background
            AsyncTask.execute(() -> {
                try {
                    while (dbCursor.moveToNext()) {
                        double parkLat = dbCursor.getDouble(4);
                        double parkLng = dbCursor.getDouble(5);

                        Log.d("ParkCoordinates", "Park Lat: " + parkLat + ", Park Lng: " + parkLng);

                        // Make API request to get distance matrix
                        String apiKey = BuildConfig.MAPS_API_KEY;
                        String apiUrl = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                                "?origins=" + userLocation.getLatitude() + "," + userLocation.getLongitude() +
                                "&destinations=" + parkLat + "," + parkLng +
                                "&key=" + apiKey;

                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(apiUrl).build();
                        Response response = client.newCall(request).execute();

                        if (response.isSuccessful()) {
                            String jsonResponse = response.body().string();
                            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                            // Parse the distance from the API response
                            JsonArray rows = jsonObject.getAsJsonArray("rows");

                            if (rows.size() > 0) {
                                JsonArray elements = rows.get(0).getAsJsonObject().getAsJsonArray("elements");
                                if (elements.size() > 0) {
                                    float apiDistance = elements.get(0).getAsJsonObject().getAsJsonObject("distance").get("value").getAsFloat();

                                    // Create a ParkDistance object to store park information and distance
                                    ParkDistance parkDistance = new ParkDistance(
                                            dbCursor.getString(0),  // Park name or other identifier
                                            apiDistance);

                                    parkDistances.add(parkDistance);
                                } else {
                                    // Handle the case where elements array is empty
                                    Log.e("sortListByDistance", "Elements array is empty");
                                }
                            } else {
                                // Handle the case where rows array is empty
                                Log.e("sortListByDistance", "Rows array is empty");
                            }

                        }
                    }

                    // Inside the AsyncTask, after the while loop
                    Log.d("AsyncTask", "Number of parkDistances: " + parkDistances.size());

                    // Before sorting the list
                    Log.d("AsyncTask", "Before sorting: " + parkDistances);

                    // Sort the list based on distance
                    Collections.sort(parkDistances, Comparator.comparing(ParkDistance::getDistance));
                    Log.d("AsyncTask", "After sorting: " + parkDistances);

                    // Create a list of park names in the sorted order
                    List<String> sortedParkNames = new ArrayList<>();
                    for (ParkDistance parkDistance : parkDistances) {
                        sortedParkNames.add(parkDistance.getParkName());
                    }

                    // Query the database with the sorted list of park names
                    String whereClause = "region IN (" + TextUtils.join(",", Collections.nCopies(sortedParkNames.size(), "?")) + ")";
                    String[] whereArgs = sortedParkNames.toArray(new String[sortedParkNames.size()]);
                    String orderByClause = "CASE region " + buildOrderByClause(sortedParkNames) + " END";


                    // Query the database with the sorted list of park names and apply ordering
                    dbCursor = database.query(
                            "natur_table_park",
                            null,
                            whereClause,
                            whereArgs,
                            null,
                            null,
                            orderByClause
                    );

                    // Create an adapter with the new dbCursor
                    ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);

                    // Update UI on the main thread
                    requireActivity().runOnUiThread(() -> {
                        if (list_view != null) {
                            list_view.setAdapter(adapter);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            Log.e("sortListByDistance", "User location is null.");
            Toast.makeText(requireContext(), "User location not available", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to build the CASE statement for ordering
    private String buildOrderByClause(List<String> sortedParkNames) {
        StringBuilder orderByClause = new StringBuilder();
        for (int i = 0; i < sortedParkNames.size(); i++) {
            orderByClause.append(" WHEN '").append(sortedParkNames.get(i)).append("' THEN ").append(i);
        }
        return orderByClause.toString();
    }

    // Inner class to represent a park and its distance
    public class ParkDistance {
        private final String parkName;
        private final float distance;

        public ParkDistance(String parkName, float distance) {
            this.parkName = parkName;
            this.distance = distance;
        }

        public String getParkName() {
            return parkName;
        }

        public float getDistance() {
            return distance;
        }
    }

    // Enable radio group switches when the fragment resumes
    private void toggleRadioGroupOn() {
        Log.d("Toggle", "Toggling radio group on");

        RadioGroup radioGroup = getActivity().findViewById(R.id.radioGroup);
        SearchView searchView1 = getActivity().findViewById(R.id.searchbar);
        TextView sortBy = getActivity().findViewById(R.id.sortBy);

        // Iterate through each child in the RadioGroup
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View child = radioGroup.getChildAt(i);

            // Check if the child is a Switch
            if (child instanceof Switch) {
                Switch switchView = (Switch) child;

                // Enable switches
                switchView.setEnabled(true);
            }
        }

        // Enable the entire RadioGroup to prevent user interaction
        radioGroup.setEnabled(true);
        radioGroup.setVisibility(View.VISIBLE);
        sortBy.setVisibility(View.VISIBLE);
        searchView1.setQueryHint("Search for a park");

    }

    @Override
    public void onDestroy() {
        if (dbCursor != null && !dbCursor.isClosed()) {
            dbCursor.close();
        }
        super.onDestroy();
    }

}