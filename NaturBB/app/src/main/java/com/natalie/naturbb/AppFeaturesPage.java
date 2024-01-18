package com.natalie.naturbb;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class AppFeaturesPage extends Fragment {


    public AppFeaturesPage() {
        // Required empty public constructor
    }

    // Set up the click listener for the email link

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_features_page, container, false);

        // Hide existing content when the fragment is created
        ((AboutActivity) requireActivity()).hideExistingContent();

        // Set up other views and listeners

        return view;
    }

    // Other methods

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show existing content when the fragment is destroyed (user navigates back)
        ((AboutActivity) requireActivity()).showExistingContent();
    }



}

