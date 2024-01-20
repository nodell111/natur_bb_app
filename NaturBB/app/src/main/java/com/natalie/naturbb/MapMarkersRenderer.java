package com.natalie.naturbb;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class MapMarkersRenderer extends DefaultClusterRenderer<Park> {
    public MapMarkersRenderer(Context context, GoogleMap map, ClusterManager<Park> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull Park Park, @NonNull MarkerOptions markerOptions) {
        markerOptions
                .icon(getItemIcon())
                .title(Park.name)
                .snippet(Park.getSnippet());
    }

    @Override
    protected void onClusterItemUpdated(@NonNull Park Park, @NonNull Marker marker) {
        // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
        marker.setIcon(getItemIcon());
        marker.setTitle(Park.name);
        marker.setSnippet(Park.getSnippet());
    }

    private BitmapDescriptor getItemIcon() {
        return BitmapDescriptorFactory.fromResource(R.drawable.location_dot_solid);
    }

}