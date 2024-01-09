package com.natalie.naturbb.fragments;

import android.content.Intent;
import android.os.Bundle;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.natalie.naturbb.MainActivity;
import com.natalie.naturbb.MapsDetailFragment;
import com.natalie.naturbb.R;

public class ListBottomSheetFragment extends BottomSheetDialogFragment {

    private String parkName;
    private String parkImage;

    private SearchView searchView;

    //add more variables as needed for additional arguments

    public ListBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve data from the arguments bundle
        Bundle args = getArguments();
        if (args != null) {
            parkName = args.getString("park_name", "");
            parkImage = args.getString("park_image", "");
            //add more arguments here from listfragment_dist as needed
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_bottom_sheet, container, false);

        searchView = getActivity().findViewById(R.id.searchbar);

        // Set the title and description based on parkName
        TextView titleTextView = view.findViewById(R.id.listBottomSheet_title);
        TextView descriptionTextView = view.findViewById(R.id.listBottomSheet_description);

        titleTextView.setText(parkName);
        descriptionTextView.setText(Html.fromHtml(
                "Description<br>"
                + "Highlights: <br>" +
                "Opening Hours: <br>" +
                "Info:"
                , Html.FROM_HTML_MODE_COMPACT));


        // Update the ImageView based on parkImage
        updateImageViewBasedOnParkImage(view);

        // Set up a button click listener
        Button startMapButton = view.findViewById(R.id.button_start_map);
        startMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the onClickStartMap method when the button is clicked
                onClickStartMap();
            }
        });

        searchView.clearFocus();

        return view;
    }

    private void updateImageViewBasedOnParkImage(View view) {
        ImageView imageView = view.findViewById(R.id.listBottomSheet_image);

        // Set the ImageView source dynamically based on the parkImage value
        int resourceId = getResources().getIdentifier(parkImage, "drawable", requireActivity().getPackageName());
        if (resourceId != 0) {
            // Set the image if the resource ID is valid
            imageView.setImageResource(resourceId);
        } else {
            // Handle the case where the resource ID is not found
            // You might want to set a default image or show an error message
            imageView.setImageResource(R.drawable.natur_barnim);
        }
    }

    public void onClickStartMap() {

        // Set the visibility of relevant views to GONE
        TextView titleTextView = requireView().findViewById(R.id.listBottomSheet_title);
        TextView descriptionTextView = requireView().findViewById(R.id.listBottomSheet_description);
        ImageView imageView = requireView().findViewById(R.id.listBottomSheet_image);
        AppCompatButton startMapButton = requireView().findViewById(R.id.button_start_map);

        descriptionTextView.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        startMapButton.setVisibility(View.GONE);

        // Create an instance of the fragment you want to show
        MapsDetailFragment mapsDetailFragment = new MapsDetailFragment();

        // Use a FragmentTransaction to replace the current fragment with the new one
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mapsdetail_view, mapsDetailFragment); // R.id.fragment_container is the ID of the container where you want to place the fragment
        transaction.addToBackStack(null); // Optional: This allows users to navigate back to the previous fragment
        transaction.commit();

    }

}
