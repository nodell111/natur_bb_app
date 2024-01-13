package com.natalie.naturbb;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.view.MenuItem;
import android.widget.TextView;

import android.net.Uri;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayoutMediator;



public class MainActivity extends AppCompatActivity
{
    private TextView textFavorites;
    private TextView textSchedules;
    private TextView textMusic;
    ImageButton info_Button;


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

}

