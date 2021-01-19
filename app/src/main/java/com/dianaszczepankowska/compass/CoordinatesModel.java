package com.dianaszczepankowska.compass;

public class CoordinatesModel {
    private final float longitude;
    private final float latitude;
    private float altitude;

    public CoordinatesModel(float latitude, float longitude, float altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public CoordinatesModel(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public float getLongitude() {
        return longitude;
    }


    public float getLatitude() {
        return latitude;
    }


    public float getAltitude() {
        return altitude;
    }
}
