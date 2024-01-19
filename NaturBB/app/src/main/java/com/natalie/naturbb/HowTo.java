package com.natalie.naturbb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class HowTo extends BottomSheetDialogFragment {

    public HowTo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.howto, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set the BottomSheetDialogFragment state to expanded
        View view = getView();
        if (view != null) {
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    ViewTreeObserver currentVTO = view.getViewTreeObserver();
                    currentVTO.removeOnPreDrawListener(this);
                    BottomSheetBehavior.from((View) view.getParent()).setState(BottomSheetBehavior.STATE_EXPANDED);
                    return true;
                }
            });
        }
    }
}
