package com.example.sharecaring.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    private LatLng postion;
    private String title;
    private String snippet;
    private int iconPicture;
    private User user;

    public ClusterMarker(LatLng postion, String title, String snippet, int iconPicture) {
        this.postion = postion;
        this.title = title;
        this.snippet = snippet;
        this.iconPicture = iconPicture;
    }

    public ClusterMarker() {}

    @NonNull
    @Override
    public LatLng getPosition() {
        return postion;
    }

    public void setPostion(LatLng postion) {
        this.postion = postion;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    public int getIconPicture() {
        return iconPicture;
    }
}
