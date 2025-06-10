package com.example.mealmate.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.mealmate.R;
import com.example.mealmate.databinding.FragmentProfileBinding;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();

                        // **FIX: Clear tint before loading user-selected image**
                        binding.imageViewProfile.clearColorFilter();
                        binding.imageViewProfile.setImageTintList(null);

                        Glide.with(this)
                                .load(selectedImageUri)
                                .circleCrop()
                                .into(binding.imageViewProfile);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        observeViewModel();

        profileViewModel.loadUserProfile();
    }

    private void setupClickListeners() {
        binding.buttonBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.textViewChangePhoto.setOnClickListener(v -> openImagePicker());
        binding.imageViewProfile.setOnClickListener(v -> openImagePicker());
        binding.buttonUpdateProfile.setOnClickListener(v -> updateProfile());
        binding.buttonChangePassword.setOnClickListener(v -> changePassword());
        binding.buttonLogout.setOnClickListener(v -> logout());
    }

    private void observeViewModel() {
        profileViewModel.getUserProfileData().observe(getViewLifecycleOwner(), resource -> {
            if(resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    if (resource.data != null) {
                        binding.editTextDisplayName.setText(resource.data.getDisplayName());
                        binding.editTextEmail.setText(resource.data.getEmail());

                        // **FIX: Clear tint before loading profile data**
                        binding.imageViewProfile.clearColorFilter();
                        binding.imageViewProfile.setImageTintList(null);

                        if (resource.data.getPhotoUrl() != null && !resource.data.getPhotoUrl().isEmpty()) {
                            Glide.with(this)
                                    .load(resource.data.getPhotoUrl())
                                    .placeholder(R.drawable.ic_profile)
                                    .circleCrop()
                                    .into(binding.imageViewProfile);
                        } else {
                            Glide.with(this)
                                    .load(R.drawable.ic_profile)
                                    .circleCrop()
                                    .into(binding.imageViewProfile);
                        }
                    }
                    break;
                case ERROR:
                    setLoading(false);
                    showMessage("Error: " + resource.message);
                    break;
            }
        });

        profileViewModel.getUpdateProfileState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    showMessage("Profile updated successfully!");
                    profileViewModel.clearUpdateState();
                    selectedImageUri = null; // Reset URI after successful upload
                    break;
                case ERROR:
                    setLoading(false);
                    showMessage("Update failed: " + resource.message);
                    profileViewModel.clearUpdateState();
                    break;
            }
        });

        profileViewModel.getPasswordResetState().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    showMessage("Password reset email sent.");
                    profileViewModel.clearPasswordResetState();
                    break;
                case ERROR:
                    setLoading(false);
                    showMessage("Failed to send email: " + resource.message);
                    profileViewModel.clearPasswordResetState();
                    break;
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.buttonUpdateProfile.setEnabled(!isLoading);
        binding.buttonLogout.setEnabled(!isLoading);
        binding.buttonChangePassword.setEnabled(!isLoading);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateProfile() {
        String displayName = binding.editTextDisplayName.getText().toString().trim();
        if (displayName.isEmpty()) {
            binding.editTextDisplayName.setError("Display name cannot be empty.");
            return;
        }
        profileViewModel.updateProfile(displayName, selectedImageUri);
    }

    private void changePassword() {
        profileViewModel.sendPasswordReset();
    }

    private void logout() {
        profileViewModel.logout();
        // Navigate to login screen and clear back stack
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_profileFragment_to_loginFragment);
    }

    private void showMessage(String message) {
        if(getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}