package com.natalie.naturbb;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.home) {
            Intent homeIntent = new Intent(AboutActivity.this, MainActivity.class);
            startActivity(homeIntent);
            return true;
        } else if (itemId == R.id.about) {
            Intent aboutIntent = new Intent(AboutActivity.this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        // Setting up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayShowTitleEnabled(false);



        // Add this code to connect fragments
        TextView sourcesLink = findViewById(R.id.sourcesAndCreditsLink);
        TextView appFeaturesLink = findViewById(R.id.appFeaturesLink);
        TextView howtoLink = findViewById(R.id.howtoLink);


        TextView aboutTitle = findViewById(R.id.aboutTitle);
        aboutTitle.setPaintFlags(aboutTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        sourcesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new sourcesandcredits());
            }
        });

        appFeaturesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new AppFeaturesPage());
            }
        });

        howtoLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HowTo howtoDialogFragment = new HowTo();
                howtoDialogFragment.show(getSupportFragmentManager(), howtoDialogFragment.getTag());
            }
        });
    }

    // Method to show existing content
    public void showExistingContent() {
        findViewById(R.id.sourcesAndCreditsLink).setVisibility(View.VISIBLE);
        findViewById(R.id.appFeaturesLink).setVisibility(View.VISIBLE);
        findViewById(R.id.howtoLink).setVisibility(View.VISIBLE);
        findViewById(R.id.imageView3).setVisibility(View.VISIBLE);
        findViewById(R.id.aboutTitle).setVisibility(View.VISIBLE);
        // Add more views as needed
    }
    // Method to hide existing content
    public void hideExistingContent() {
        findViewById(R.id.sourcesAndCreditsLink).setVisibility(View.GONE);
        findViewById(R.id.appFeaturesLink).setVisibility(View.GONE);
        findViewById(R.id.howtoLink).setVisibility(View.GONE);
        findViewById(R.id.imageView3).setVisibility(View.GONE);
        findViewById(R.id.aboutTitle).setVisibility(View.GONE);
        // Add more views as needed
    }

    private void loadFragment(Fragment fragment) {
        // Hide existing content
        hideExistingContent();

        // Load the fragment into the fragmentContainer
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}








