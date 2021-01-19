package com.dianaszczepankowska.compass;

import android.annotation.SuppressLint;
import android.hardware.GeomagneticField;
import android.location.Location;

import java.util.GregorianCalendar;

public class CalculateDirection {
    private static final float ALPHA = 0.05f;
    private float magneticDeclination;
    private CoordinatesModel myLocation;
    private final CoordinatesModel destination;
    private float magneticNorth;
    private float realNorthAngle;

    public CalculateDirection(CoordinatesModel destination, SensorClass sensorClass, LocationClass locationClass) {
        this.destination = destination;
        SensorClass.CompassListener compassListener = getCompassListener();
        LocationClass.LocationListener locationListener = getLocationListener();
        locationClass.setListener(locationListener);
        sensorClass.setListener(compassListener);
    }

    public interface CalculateDirectionListener {
        void findNorthAngle(Float northAngle);

        void findDirectionAngle(Float directionAngle);

        void findDistance(String distance);
    }

    private CalculateDirection.CalculateDirectionListener listener;

    public void setListener(CalculateDirection.CalculateDirectionListener listener) {
        this.listener = listener;
    }


    private SensorClass.CompassListener getCompassListener() {
        return north -> {
            magneticNorth = north;
            getRealNorth();
        };
    }

    private LocationClass.LocationListener getLocationListener() {
        return (myLocation) -> {
            this.myLocation = myLocation;
            magneticDeclination = getMagneticDeclination();
        };
    }


    //Isolates the gravity
    static float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    private float getMagneticDeclination() {
        GeomagneticField geomagnetic = new GeomagneticField(myLocation.getLatitude(), myLocation.getLongitude(), myLocation.getAltitude(), GregorianCalendar.getInstance().getTimeInMillis());
        return geomagnetic.getDeclination();
    }

    private void getDirectionAngle() {
        float direction;
        if (destination != null && myLocation != null) {

            Location location1 = new Location("myLocation");
            location1.setLatitude(myLocation.getLatitude());
            location1.setLongitude(myLocation.getLongitude());

            Location location2 = new Location("destination");
            location2.setLatitude(destination.getLatitude());
            location2.setLongitude(destination.getLongitude());

            direction = location1.bearingTo(location2) + realNorthAngle;

        } else {
            direction = 0;
        }
        calculateDistance();
        if (listener != null) {
            listener.findDirectionAngle(direction);
        }
    }

    private void getRealNorth() {
        magneticNorth = (float) Math.toDegrees(magneticNorth);
        realNorthAngle = (magneticNorth + magneticDeclination + 360) % 360;
        if (listener != null) {
            listener.findNorthAngle(realNorthAngle);
        }
        getDirectionAngle();
    }

    @SuppressLint("DefaultLocale")
    private void calculateDistance() {
        String distanceStr;
        if (myLocation != null && destination != null) {
            float[] results = new float[1];
            Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(),
                    destination.getLatitude(), destination.getLongitude(), results);
            double distance = results[0];
            distanceStr = String.format("%.1f", distance);
        } else {
            distanceStr = "0";
        }
        if (listener != null) {
            listener.findDistance(distanceStr);
        }
    }
}
