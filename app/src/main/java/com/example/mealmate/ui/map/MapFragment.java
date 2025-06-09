package com.example.mealmate.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mealmate.R;
import com.example.mealmate.databinding.FragmentMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

/**
 * Fragment for displaying nearby grocery stores on a map
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted, get user location
                    getUserLocationAndFindStores();
                } else {
                    // Permission denied
                    hideLoading();
                    Toast.makeText(getContext(), "Location permission is required to find nearby stores",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "YOUR_API_KEY");
        }
        placesClient = Places.createClient(requireContext());

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();

        // Get the SupportMapFragment and request notification when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupClickListeners() {
        binding.buttonBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        binding.buttonRefresh.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.clear(); // Clear existing markers
                showLoading("Refreshing stores...");
                checkLocationPermissionAndProceed();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // We'll handle location manually

        // Check for location permission and proceed
        checkLocationPermissionAndProceed();
    }

    private void checkLocationPermissionAndProceed() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocationAndFindStores();
        } else {
            // Request permission
            showLoading("Requesting location permission...");
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getUserLocationAndFindStores() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        showLoading("Finding your location...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // Move camera to user location
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f));

                        // Enable my location dot
                        try {
                            mMap.setMyLocationEnabled(true);
                        } catch (SecurityException e) {
                            // Handle exception
                        }

                        // Find nearby stores
                        findNearbyStores();
                    } else {
                        hideLoading();
                        Toast.makeText(getContext(),
                                "Unable to get your location. Please ensure location services are enabled.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(getContext(), "Failed to get location: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void findNearbyStores() {
        showLoading("Finding nearby stores...");

        // Define the fields to retrieve
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.TYPES,
                Place.Field.ADDRESS);

        // Create the request
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        placesClient.findCurrentPlace(request)
                .addOnSuccessListener(response -> {
                    int storeCount = 0;

                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Place place = placeLikelihood.getPlace();

                        // Check if this place is a grocery store or supermarket
                        if (place.getTypes() != null &&
                                (place.getTypes().contains(Place.Type.GROCERY_OR_SUPERMARKET) ||
                                        place.getTypes().contains(Place.Type.SUPERMARKET))) {

                            // Add marker to map
                            if (place.getLatLng() != null && place.getName() != null) {
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(place.getLatLng())
                                        .title(place.getName());

                                if (place.getAddress() != null) {
                                    markerOptions.snippet(place.getAddress());
                                }

                                mMap.addMarker(markerOptions);
                                storeCount++;
                            }
                        }
                    }

                    hideLoading();

                    if (storeCount == 0) {
                        Toast.makeText(getContext(), "No grocery stores found nearby",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Found " + storeCount + " stores nearby",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(exception -> {
                    hideLoading();
                    Toast.makeText(getContext(), "Error finding stores: " + exception.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(String message) {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.textLoadingMessage.setText(message);
    }

    private void hideLoading() {
        binding.layoutLoading.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}