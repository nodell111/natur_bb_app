package com.natalie.naturbb;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class AppFeaturesPage extends Fragment {


    public AppFeaturesPage() {
        // Required empty public constructor
    }

    // Called when the fragment is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    // Called to create the fragment's view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_features_page, container, false);

        // Hide existing content when the fragment is created
        ((AboutActivity) requireActivity()).hideExistingContent();

        // Set up the click listener for the email link
        TextView emailLink = view.findViewById(R.id.contactDeveloperLink);
        emailLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(); // Call the sendEmail method when the email link is clicked
            }
        });

        // Set up the click listener for the map link
        TextView mapLink = view.findViewById(R.id.adresstext);
        mapLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap(); // Call the openMap method when the map link is clicked
            }
        });

        return view;
    }

    // Method to send an email
    private void sendEmail() {
        String email = "dilara.bozkurt@tum.de";
        String subject = "Feedback";
        String body = ""; // Customize the email body

        composeEmail(email, subject, body); // Call the composeEmail method
    }

    // Method to create an email intent
    private void composeEmail(String address, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + address));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        // Check if there's an app that can handle the email intent
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent); // Start the email intent
        }
    }

    // Method to open a map
    private void openMap() {
        // Coordinates for TU Dresden
        String mapUrl = "geo:51.0290301,13.7212011?q=H%C3%BClsse-Bau,+Helmholtzstra%C3%9Fe+10,+01069+Dresden";
        showMap(mapUrl); // Call the showMap method with the map URL
    }

    // Method to create a map intent
    private void showMap(String mapUrl) {
        Uri geoUri = Uri.parse(mapUrl);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);

        // Check if there's an app that can handle the map intent
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent); // Start the map intent
        }
    }

    // Called when the fragment is destroyed (user navigates back)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show existing content when the fragment is destroyed
        ((AboutActivity) requireActivity()).showExistingContent();
    }
}

