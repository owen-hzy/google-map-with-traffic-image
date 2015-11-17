package com.example.hzy.traffic;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hzy on 16/11/15.
 */
public class MarkerInfo {
    private static final String url_prefix = "http://tdcctv.data.one.gov.hk/%s.JPG";

    private String key;
    private LatLng latLng;
    private Bitmap image;

    public MarkerInfo(String key, LatLng latLng) {
        this.key = key;
        this.latLng = latLng;
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return String.format(url_prefix, key);
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
