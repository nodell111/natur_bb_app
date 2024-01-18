package com.natalie.naturbb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.Objects;
import com.natalie.naturbb.ListFragment;


/**
 * Demonstrates heavy customisation of the look of rendered clusters.
 */
public class MapMarkersRenderer extends DefaultClusterRenderer<Park> {
    private ClusterManager clusterManager;
    private final IconGenerator mClusterIconGenerator;
    private final Context context;
    public MapMarkersRenderer(Context context, GoogleMap map, ClusterManager<Park> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        this.clusterManager = clusterManager;
        mClusterIconGenerator = new IconGenerator(context.getApplicationContext());
    }
//        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
//        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
//        private final ImageView mImageView;
//        private final ImageView mClusterImageView;
//        private final int mDimension;
//
//        public ParkRendererrer() {
//            super(getApplicationContext(), getMap(), mClusterManager);
//
//            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
//            mClusterIconGenerator.setContentView(multiProfile);
//            mClusterImageView = multiProfile.findViewById(R.id.image);
//
//            mImageView = new ImageView(getApplicationContext());
//            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
//            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
//            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
//            mImageView.setPadding(padding, padding, padding, padding);
//            mIconGenerator.setContentView(mImageView);
//        }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull Park Park, @NonNull MarkerOptions markerOptions) {
        // Draw a single Park - show their profile photo and set the info window to show their name
        Log.e("call", "on before cluster item rendered");
        markerOptions
                .icon(getItemIcon(Park))
                .title(Park.name)
                .snippet(Park.getSnippet());
    }

    @Override
    protected void onClusterItemUpdated(@NonNull Park Park, @NonNull Marker marker) {
        // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
        Log.e("call", "onclusteritem updated");
        marker.setIcon(getItemIcon(Park));
        marker.setTitle(Park.name);
        marker.setSnippet(Park.getSnippet());
    }

    /**
     * Get a descriptor for a single Park (i.e., a marker outside a cluster) from their
     * profile photo to be used for a marker icon
     *
     * @param Park Park to return an BitmapDescriptor for
     * @return the Park's profile photo as a BitmapDescriptor
     */
    private BitmapDescriptor getItemIcon(Park Park) {
//            mImageView.setImageResource(Park.profilePhoto);
//            Bitmap icon = mIconGenerator.makeIcon();
        return BitmapDescriptorFactory.fromResource(R.drawable.location_dot_solid);
    }

//        @Override
//        protected void onBeforeClusterRendered(@NonNull Cluster<Park> cluster, @NonNull MarkerOptions markerOptions) {
//            // Draw multiple people.
//            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
////            markerOptions.icon(getClusterIcon(cluster));
//            mClusterIconGenerator.setBackground(
//                    ContextCompat.getDrawable(context, R.drawable.cluster_icon));
//            mClusterIconGenerator.setTextAppearance(R.style.WhiteTextAppearance);
//            final Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
//            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
//        }
//
//        @Override
//        protected void onClusterUpdated(@NonNull Cluster<Park> cluster, Marker marker) {
//            // Same implementation as onBeforeClusterRendered() (to update cached markers)
//            marker.setIcon(getClusterIcon(cluster));
//        }

    /**
     * Get a descriptor for multiple people (a cluster) to be used for a marker icon. Note: this
     * method runs on the UI thread. Don't spend too much time in here (like in this example).
     *
     * @param cluster cluster to draw a BitmapDescriptor for
     * @return a BitmapDescriptor representing a cluster
     */
//        private BitmapDescriptor getClusterIcon(Cluster<Park> cluster) {
//            List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));
//            int width = mDimension;
//            int height = mDimension;
//
//            for (Park p : cluster.getItems()) {
//                // Draw 4 at most.
//                if (profilePhotos.size() == 4) break;
//                Drawable drawable = getResources().getDrawable(p.profilePhoto);
//                drawable.setBounds(0, 0, width, height);
//                profilePhotos.add(drawable);
//            }
//            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
//            multiDrawable.setBounds(0, 0, width, height);
//
//            mClusterImageView.setImageDrawable(multiDrawable);
//            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
//            return BitmapDescriptorFactory.fromBitmap(icon);
//        }

//        @Override
//        protected boolean shouldRenderAsCluster(@NonNull Cluster cluster) {
//            // Always render clusters.
//            return cluster.getSize() > 1;
//        }
}


