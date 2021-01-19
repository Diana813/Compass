package com.dianaszczepankowska.compass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.dianaszczepankowska.compass.databinding.CompassFragmentBinding;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import static com.dianaszczepankowska.compass.LocationClass.REQ_CODE;


public class CompassFragment extends Fragment {

    private CompassFragmentBinding binding;
    private SensorClass sensorClass;
    private LocationClass locationClass;
    private CoordinatesModel destination;
    private float currentPosition = 0;
    private float currentDirection = 0;
    private float realNorthAngle;
    private float destinationAngle;
    private String distance;
    private Context context;


    public void onResume() {
        super.onResume();
        if (REQ_CODE == 1) {
            locationClass.checkLocationSettingsAndStartLocationUpdates(context);
        }
        sensorClass.registerSensorListeners();
    }

    public void onPause() {
        super.onPause();
        locationClass.stopLocationUpdates();
        sensorClass.unregisterSensorListeners();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        sensorClass = new SensorClass(context);
        locationClass = new LocationClass(context);
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.compass_fragment, container, false);
        View view = binding.getRoot();
        LocationClass.requestPermissions(context);
        getDestinationData();
        CalculateDirection calculateDirection = new CalculateDirection(destination, sensorClass, locationClass);
        CalculateDirection.CalculateDirectionListener directionListener = getCalculationListener();
        calculateDirection.setListener(directionListener);
        setOnDestinationButtonClickListener();
        return view;
    }

    private CalculateDirection.CalculateDirectionListener getCalculationListener() {
        return new CalculateDirection.CalculateDirectionListener() {
            @Override
            public void findNorthAngle(Float northAngle1) {

                realNorthAngle = northAngle1;
                showNorth();
            }

            @Override
            public void findDirectionAngle(Float directionAngle) {
                destinationAngle = directionAngle;

            }

            @Override
            public void findDistance(String distanceStr) {
                distance = distanceStr;
                displayDistance();
            }
        };
    }


    private void setOnDestinationButtonClickListener() {
        binding.setDestinationButton.setDestination.setOnClickListener(view -> {
            FragmentTransaction fragmentTransaction =
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            DestinationFragment destinationFragment = new DestinationFragment();
            fragmentTransaction.replace(R.id.fragmentContainer, destinationFragment).addToBackStack("tag");
            fragmentTransaction.commit();
        });
    }

    private void showNorth() {
        animateRotation(currentPosition, realNorthAngle, binding.compassImage.compass);
        currentPosition = realNorthAngle;
        if (getDestinationData()) {
            showDirection();
        }
    }

    private boolean getDestinationData() {
        Bundle args = getArguments();
        if (args != null) {
            float lon = (float) args.getDouble("longitude");
            float lat = (float) args.getDouble("latitude");
            destination = new CoordinatesModel(lat, lon);
            return true;
        }
        return false;
    }

    private void showDirection() {
        animateRotation(currentDirection, destinationAngle, binding.compassImage.guide);
        currentDirection = destinationAngle;
    }

    @SuppressLint("SetTextI18n")
    private void displayDistance() {
        binding.distanceInfo.distanceTextView.setText(getString(R.string.distance_from_the_destination) + " " + distance + "m");
    }

    private void animateRotation(float currentDirection, float direction, View view) {
        Animation animation = new RotateAnimation(currentDirection, direction,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setDuration(500);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }
}