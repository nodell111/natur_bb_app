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

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public AppFeaturesPage() {
        // Required empty public constructor
    }

    public static AppFeaturesPage newInstance(String param1, String param2) {
        AppFeaturesPage fragment = new AppFeaturesPage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

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
                sendEmail();
            }
        });

        // Set up the click listener for the map link
        TextView mapLink = view.findViewById(R.id.adresstext);
        mapLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        // Set up other views and listeners

        return view;
    }

    private void sendEmail() {
        String email = "dilara.bozkurt@tum.de";
        String subject = "Feedback";
        String body = ""; // Customize the email body

        composeEmail(email, subject, body);
    }

    private void composeEmail(String address, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + address));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void openMap() {
        // Coordinates for TU Dresden
        String mapUrl = "geo:51.0290301,13.7212011?q=H%C3%BClsse-Bau,+Helmholtzstra%C3%9Fe+10,+01069+Dresden";
        showMap(mapUrl);
    }

    private void showMap(String mapUrl) {
        Uri geoUri = Uri.parse(mapUrl);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show existing content when the fragment is destroyed (user navigates back)
        ((AboutActivity) requireActivity()).showExistingContent();
    }
}



