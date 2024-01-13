package com.natalie.naturbb;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Park implements ClusterItem {
    public final String name;
    private final LatLng mPosition;
    private final String mSnippet;
    public Park(LatLng position, String name, String snippet) {
        this.name = name;
        mPosition = position;
        this.mSnippet = snippet;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Nullable
    @Override
    public String getTitle() {
        return this.name;
    }

    @Nullable
    @Override
    public String getSnippet() {
        Log.d("snippet", this.mSnippet);
        return null;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return null;
    }
}


