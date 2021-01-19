package com.dianaszczepankowska.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

public class SensorClass implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor magneticField;
    private float[] accelerometerData = new float[3];
    private float[] magnetometerData = new float[3];
    float[] rotationMatrix = new float[9];
    float[] orientationValues = new float[3];

    public interface CompassListener {
        void findNorth(float north);
    }

    private CompassListener listener;

    public void setListener(CompassListener listener) {
        this.listener = listener;
    }

    public SensorClass(Context context) {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerData = CalculateDirection.lowPassFilter(sensorEvent.values.clone(), accelerometerData);
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerData = CalculateDirection.lowPassFilter(sensorEvent.values.clone(), magnetometerData);
        }

        SensorManager.getRotationMatrix(rotationMatrix,
                null, accelerometerData, magnetometerData);
        float[] orientation = SensorManager.getOrientation(rotationMatrix, orientationValues);

        float north = -orientation[0];
        if (listener != null) {
            listener.findNorth(north);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void registerSensorListeners() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensorListeners() {
        sensorManager.unregisterListener(this);
    }
}
