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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListFragment extends Fragment {

    public static DatabaseHelper dbHelper;
    // Declare variables
    private SQLiteDatabase database;
    private Cursor dbCursor;
    private ListView list_view;
    private SearchView searchView;
    private Switch switchSortSize;
    private Switch switchSortName;
    private Switch switchSortDistance;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_listfragment, container, false);
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

        dbCursor = database.rawQuery("SELECT * FROM natur_table_park ORDER BY region asc;", null);


        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        //create HTML list items with adapter
        list_view.setAdapter(adapter);
        //adapter needs the layout view file and data (the dbCursor points to the data records)

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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


        setupSearchView();
        setupSwitchGroup();
        return view;
    }


    private ArrayAdapter<CharSequence> createAdapterHtml(Cursor cursor) {
        int length = cursor.getCount();
        cursor.moveToFirst();
        Spanned[] html_array = new Spanned[length];
        String[] image_names = new String[length]; // New array for image names
        int index_name = cursor.getColumnIndex("region");
        int image_name = cursor.getColumnIndex("image_name");

//        int index_name_en = cursor.getColumnIndex("region_en");
//        for (int i = 0; i < length; i++) {
//            html_array[i] = Html.fromHtml(cursor.getString(index_name) + "<br><i>" + cursor.getString(index_name_en) + "</i>");
//            cursor.moveToNext();
//        }

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
                view.setBackgroundResource(resId);

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


    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String keyword = searchView.getQuery().toString();
                if (dbCursor != null) {
                    dbCursor.close();
                }
                dbCursor = database.rawQuery(
                        "SELECT * FROM natur_table_park WHERE region LIKE ? ORDER BY region asc",
                        new String[]{"%" + keyword + "%"}
                );
                ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
                if (list_view != null) {
                    list_view.setAdapter(adapter);
                }
                return true;
            }
        });


    }

    //when switch for size is checked then sort list by size column
    private void setupSwitchGroup() {
        switchSortSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSortName.setChecked(false);
                    switchSortDistance.setChecked(false);
                    sortListBySize();
                }
            }
        });

        switchSortName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSortSize.setChecked(false);
                    switchSortDistance.setChecked(false);
                    sortListByName();
                }
            }
        });

        switchSortDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSortSize.setChecked(false);
                    switchSortName.setChecked(false);
                    sortListByDistance();
                }
            }
        });
    }

    private void sortListBySize() {
        if (dbCursor != null) {
            dbCursor.close();
        }
        dbCursor = database.rawQuery("SELECT * FROM natur_table_park ORDER BY area_km2 ASC;", null);

        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        if (list_view != null) {
            list_view.setAdapter(adapter);
        }
    }

    private void sortListByName() {
        if (dbCursor != null) {
            dbCursor.close();
        }
        dbCursor = database.rawQuery("SELECT * FROM natur_table_park ORDER BY region ASC;", null);

        ArrayAdapter<CharSequence> adapter = createAdapterHtml(dbCursor);
        if (list_view != null) {
            list_view.setAdapter(adapter);
        }
    }


    private void sortListByDistance() {
        if (dbCursor != null) {
            dbCursor.close();
        }
        dbCursor = database.rawQuery("SELECT * FROM natur_table_park;", null);

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
                        String apiKey = "AIzaSyCFbT2GLzWBitk4BRiIglO-2SHU93cziUw";
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


    @Override
    public void onDestroy() {
        if (dbCursor != null && !dbCursor.isClosed()) {
            dbCursor.close();
        }
        super.onDestroy();
    }


}