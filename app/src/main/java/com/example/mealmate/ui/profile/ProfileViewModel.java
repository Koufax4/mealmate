package com.example.mealmate.ui.profile;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.User;
import com.example.mealmate.data.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class ProfileViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<AuthResource<User>> userProfileData = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Void>> updateProfileState = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Void>> passwordResetState = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
    }

    public LiveData<AuthResource<User>> getUserProfileData() {
        return userProfileData;
    }

    public LiveData<AuthResource<Void>> getUpdateProfileState() {
        return updateProfileState;
    }

    public LiveData<AuthResource<Void>> getPasswordResetState() {
        return passwordResetState;
    }

    public void loadUserProfile() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            authRepository.getUserProfile(currentUser.getUid(), userProfileData);
        } else {
            userProfileData.setValue(AuthResource.error("Not logged in", null));
        }
    }

    public void updateProfile(String displayName, Uri photoUri) {
        authRepository.updateUserProfile(displayName, photoUri, updateProfileState);
    }

    public void sendPasswordReset() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            authRepository.sendPasswordResetEmail(currentUser.getEmail(), passwordResetState);
        } else {
            passwordResetState.setValue(AuthResource.error("User not found or has no email.", null));
        }
    }

    public void logout() {
        authRepository.logout();
    }

    public void clearUpdateState() {
        updateProfileState.setValue(null);
    }

    public void clearPasswordResetState() {
        passwordResetState.setValue(null);
    }
}