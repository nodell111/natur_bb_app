package com.natalie.naturbb.fragments;

import android.os.Bundle;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.natalie.naturbb.R;

public class ListBottomSheetFragment extends BottomSheetDialogFragment {

    private String parkName;
    private String parkImage;

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_bottom_sheet, container, false);

        // Set the title and description based on parkName (you can customize this part)
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
}
