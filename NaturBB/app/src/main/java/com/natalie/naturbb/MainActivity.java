package com.natalie.naturbb;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    ImageButton info_Button;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_TIME_KEY = "isFirstTime";

    private Location userLocation;  // Declare userLocation variable

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    MyViewPageAdapter myViewPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Find the ImageButton by its ID
        info_Button = findViewById(R.id.infoButton);

        info_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);

            }


        });

        // Check if the app is opened after quitting
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean isAfterQuit = settings.getBoolean(FIRST_TIME_KEY, true);

        if (isAfterQuit) {
            // If it's opened after quitting, open the LandingActivity
            Intent intent = new Intent(this, LandingActivity.class);
            startActivity(intent);
            finish();

            // Update SharedPreferences to indicate that it's not the first time after quitting
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(FIRST_TIME_KEY, false);
            editor.apply();
        } else {
            // Continue with your regular flow
            setContentView(R.layout.activity_main);

            tabLayout = findViewById(R.id.tabListMap);
            viewPager2 = findViewById(R.id.view_pager);
            myViewPageAdapter = new MyViewPageAdapter(this);
            viewPager2.setAdapter(myViewPageAdapter);

            viewPager2.setUserInputEnabled(false);

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

            //gives access to system services
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            //listener will be informed by manager when each new position is located
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(@NonNull Location location) {
                    // Update userLocation when location changes and store as variable
                    userLocation = location;
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(@NonNull String provider) {
                }

                public void onProviderDisabled(@NonNull String provider) {
                }
            };

            //code for location permission request
            ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                } else {
                    Toast.makeText(this, "Location cannot be obtained due to missing permission.", Toast.LENGTH_LONG).show();
                }
            });

            String[] PERMISSIONS = {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            locationPermissionRequest.launch(PERMISSIONS);
        }



        // Find the ImageButton by its ID
        info_Button = findViewById(R.id.infoButton);

        info_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);

            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_home) {
                    Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);

                    return true;
                } else if (itemId == R.id.action_favorites) {
                    Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);

                    return true;
                }
                return false;
            }
        });

    }


    // Getter method to access userLocation from other components (like fragments)
    public Location getUserLocation() {
        return userLocation;
    }
}

