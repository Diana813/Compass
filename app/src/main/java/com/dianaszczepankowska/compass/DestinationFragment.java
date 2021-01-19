package com.dianaszczepankowska.compass;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dianaszczepankowska.compass.databinding.DestinationFragmentBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import static android.app.Activity.RESULT_OK;
import static java.lang.Double.parseDouble;

public class DestinationFragment extends Fragment {

    private DestinationFragmentBinding binding;
    private final int AUTOCOMPLETE_REQUEST_CODE = 3;
    private double latitude;
    private double longitude;
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void onPause() {
        super.onPause();
        latitude = 0;
        longitude = 0;
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.destination_fragment, container, false);
        View view = binding.getRoot();
        binding.setDestinationButton.setDestination.setText(getString(R.string.search_in_google));
        binding.confirmationButton.setDestination.setText(getString(R.string.set));
        binding.latitude.editText.setHint(getString(R.string.latitude));
        binding.longitude.editText.setHint(getString(R.string.longitude));
        setOnSearchButtonClickListener();
        setConfirmButtonClickListener();
        return view;
    }

    private void setOnSearchButtonClickListener() {
        binding.setDestinationButton.setDestination.setOnClickListener(view -> {
            if (isNetworkConnected()) {
                List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(context);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            } else {
                Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    LatLng coordinates = place.getLatLng();
                    String name = place.getName();
                    if (coordinates != null) {
                        latitude = coordinates.latitude;
                        longitude = coordinates.longitude;
                        binding.latitude.coordinateInput.setText(String.valueOf(latitude));
                        binding.longitude.coordinateInput.setText(String.valueOf(longitude));
                    }
                    binding.destinationName.setVisibility(View.VISIBLE);
                    binding.destinationName.setText(name);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Toast.makeText(context, getString(R.string.no_data_availabe), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setConfirmButtonClickListener() {
        binding.confirmationButton.setDestination.setOnClickListener(view -> {
            String editTextLatitude = Objects.requireNonNull(binding.latitude.coordinateInput.getText()).toString();
            if (!editTextLatitude.equals("")) {
                latitude = parseDouble(editTextLatitude);
            } else {
                latitude = 0;
            }
            String editTextLongitude = Objects.requireNonNull(binding.longitude.coordinateInput.getText()).toString();
            if (!editTextLatitude.equals("")) {
                longitude = parseDouble(editTextLongitude);
            } else {
                longitude = 0;
            }
            FragmentTransaction fragmentTransaction =
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            CompassFragment compassFragment = new CompassFragment();
            Bundle args = new Bundle();
            args.putDouble("latitude", latitude);
            args.putDouble("longitude", longitude);
            compassFragment.setArguments(args);
            fragmentTransaction.replace(R.id.fragmentContainer, compassFragment);
            fragmentTransaction.commitNow();
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) return false;
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }
}
