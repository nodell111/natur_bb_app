package com.natalie.naturbb;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MapBottomSheetFragment extends BottomSheetDialogFragment {

    private String poiName;
    private String description;
    private String category;
    private String city;

    public MapBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve data from the arguments bundle
        Bundle args = getArguments();
        if (args != null) {
            poiName = args.getString("poi_name", "");
            description = args.getString("description", "");
            category = args.getString("category", "");
            city = args.getString("city", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_bottom_sheet, container, false);

        // Set the title and description based on poiName
        TextView titleTextView = view.findViewById(R.id.mapBottomSheet_title);
        TextView descriptionTextView = view.findViewById(R.id.mapBottomSheet_description);
        TextView categoryTextView = view.findViewById(R.id.mapBottomSheet_category);

        titleTextView.setText(poiName);
        descriptionTextView.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT));
        categoryTextView.setText(Html.fromHtml(category, Html.FROM_HTML_MODE_COMPACT));

        return view;
    }
}