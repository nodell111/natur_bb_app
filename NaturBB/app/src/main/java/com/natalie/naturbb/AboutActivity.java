package com.natalie.naturbb;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        // Add this code to connect fragments
        TextView sourcesLink = findViewById(R.id.sourcesAndCreditsLink);
        TextView appFeaturesLink = findViewById(R.id.appFeaturesLink);

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
    }
    private void hideExistingContent() {
        findViewById(R.id.sourcesAndCreditsLink).setVisibility(View.GONE);
        findViewById(R.id.appFeaturesLink).setVisibility(View.GONE);
        // Add more views as needed
    }

    private void showExistingContent() {
        findViewById(R.id.sourcesAndCreditsLink).setVisibility(View.VISIBLE);
        findViewById(R.id.appFeaturesLink).setVisibility(View.VISIBLE);
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

// Call showExistingContent when you want to display the existing content again


}



