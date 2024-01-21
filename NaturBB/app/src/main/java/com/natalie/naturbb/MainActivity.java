package com.natalie.naturbb;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements ListFragmentListener {
    private Location userLocation;
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private MyViewPageAdapter myViewPageAdapter;

    // Method to create the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Handle action bar item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        // Navigate to the main activity
        if (itemId == R.id.home) {
            Intent homeIntent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(homeIntent);
            return true;
        } else if (itemId == R.id.about) {
            // Navigate to the about activity
            Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Find views in the layout
        tabLayout = findViewById(R.id.tabListMap);
        viewPager2 = findViewById(R.id.view_pager);
        myViewPageAdapter = new MyViewPageAdapter(this);
        // Set up ViewPager and TabLayout
        viewPager2.setAdapter(myViewPageAdapter);
        viewPager2.setUserInputEnabled(false);

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        SearchViewModel searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        SearchView searchView = findViewById(R.id.searchbar);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Update the search query in the ViewModel
                searchViewModel.setSearchQuery(newText);
                return true;
            }
        });

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            // Set the MainActivity as the ListFragmentListener for the MapFragment
            mapFragment.setListFragmentListener(this);
        }

        // Check and request location permissions
        checkLocationPermissions();

        // Set up location updates
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(@NonNull Location location) {
                userLocation = location;
                // Handle location changes here
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(@NonNull String provider) {
            }

            public void onProviderDisabled(@NonNull String provider) {
            }
        };
        // Request location updates if permissions are granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
        }
    }

    // Check and request location permissions if not granted
    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 888);
        }
    }

    // Handle the result of the location permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 888) {
            // Check if the user granted location permissions
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, you can now perform location-related tasks
            } else {
                // Permissions denied, handle accordingly (show a message, disable functionality, etc.)
                Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Getter method to retrieve the user's location
    public Location getUserLocation() {
        return userLocation;
    }

    public SQLiteDatabase getDataBase() {

        // Initialize DatabaseHelper and SQLiteDatabase in onCreateView
        try {
            dbHelper.createDataBase();
            //creating the database and prevent crash with try&catch
        } catch (IOException ioe) {
        }

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
            database = dbHelper.getDataBase();
        }
        return database;
    }

    @Override
    public void setListFragmentListener(ListFragmentListener listener) {

    }

    @Override
    public void handleSearch(String query) {
        // Implement the search handling in MainActivity
        SearchView searchView = findViewById(R.id.searchbar);
        query = searchView.getQuery().toString();

        // Call handleSearch in ListFragment
        ListFragment listFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        if (listFragment != null) {
            listFragment.handleSearch(query);
        }

        // Call handleSearch in MapFragment
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.handleSearch(query);
        }
    }


    @Override
    public ArrayAdapter<CharSequence> createAdapterHtml(Cursor cursor) {
        return null;
    }
}

