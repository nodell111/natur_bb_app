package com.natalie.naturbb;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AppFeaturesPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppFeaturesPage extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AppFeaturesPage() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AppFeaturesPage.
     */
    // TODO: Rename and change types and number of parameters
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

