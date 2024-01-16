package com.natalie.naturbb;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    FragmentActivity activity;
    ClusterItem clusterItem;
    public CustomInfoWindowAdapter(FragmentActivity activity, ClusterItem clickedClusterItem) {
        this.activity = activity;
        this.clusterItem = clickedClusterItem;
//        Log.d("call", clickedClusterItem.getSnippet());
    }
    private LayoutInflater inflater;
//    private View inflater;
    private ViewGroup container;
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        Log.e("call", "get info contents");
        // Getting view from the layout file info_window_layout
        View v = LayoutInflater.from(activity).inflate(R.layout.window_layout, null, false);
        // Getting reference to the TextView to set latitude
        TextView tvName = v.findViewById(R.id.tv_name);
        // Getting reference to the TextView to set longitude
        TextView tvSnippet = v.findViewById(R.id.tv_snippet);
        String parkName = "Not Found";
        String snippet = "";
        if(clusterItem != null) {
            parkName = clusterItem.getTitle();
            snippet = clusterItem.getSnippet();
        }
        // Setting the latitude
        tvName.setText(parkName);
        // Setting the longitude
        tvSnippet.setText(snippet);
        return v;
    }

    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }
}
