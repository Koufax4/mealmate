package com.example.mealmate.data.repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.Nullable;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    public void loginUser(String email, String password, MutableLiveData<AuthResource<FirebaseUser>> userAuthLiveData) {
        userAuthLiveData.setValue(AuthResource.loading(null));

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        userAuthLiveData.setValue(AuthResource.success(user));
                    } else {
                        Log.e(TAG, "Login failed: ", task.getException());
                        userAuthLiveData.setValue(AuthResource.error(Objects.requireNonNull(task.getException()).getMessage(), null));
                    }
                });
    }

    public void registerUser(String email, String password, MutableLiveData<AuthResource<FirebaseUser>> userAuthLiveData) {
        userAuthLiveData.setValue(AuthResource.loading(null));

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            createUserProfileDocument(firebaseUser, userAuthLiveData);
                        } else {
                            Log.e(TAG, "Registration successful but user is null");
                            userAuthLiveData.setValue(AuthResource.error("Registration failed: User data not found.", null));
                        }
                    } else {
                        Log.e(TAG, "Registration failed: ", task.getException());
                        userAuthLiveData.setValue(AuthResource.error(Objects.requireNonNull(task.getException()).getMessage(), null));
                    }
                });
    }

    private void createUserProfileDocument(FirebaseUser firebaseUser, MutableLiveData<AuthResource<FirebaseUser>> liveData) {
        String userId = firebaseUser.getUid();
        User newUser = new User(
                userId,
                firebaseUser.getEmail(),
                firebaseUser.getEmail().split("@")[0], // Default display name
                null,
                Timestamp.now()
        );

        firestore.collection("users").document(userId)
                .set(newUser)
                .addOnCompleteListener(profileTask -> {
                    if (profileTask.isSuccessful()) {
                        Log.d(TAG, "User profile created for: " + userId);
                        liveData.setValue(AuthResource.success(firebaseUser));
                    } else {
                        Log.e(TAG, "Failed to create user profile: ", profileTask.getException());
                        firebaseUser.delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                Log.d(TAG, "Firebase Auth user deleted due to profile creation failure.");
                            } else {
                                Log.e(TAG, "Failed to delete Firebase Auth user after profile creation failure.", deleteTask.getException());
                            }
                        });
                        liveData.setValue(AuthResource.error("User registration failed during profile creation.", null));
                    }
                });
    }

    public void getUserProfile(String userId, MutableLiveData<AuthResource<User>> userLiveData) {
        userLiveData.setValue(AuthResource.loading(null));
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        userLiveData.setValue(AuthResource.success(user));
                    } else {
                        userLiveData.setValue(AuthResource.error("User profile not found.", null));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user profile: ", e);
                    userLiveData.setValue(AuthResource.error(e.getMessage(), null));
                });
    }

    public void updateUserProfile(String displayName, @Nullable Uri photoUri, MutableLiveData<AuthResource<Void>> updateResult) {
        FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) {
            updateResult.setValue(AuthResource.error("User not authenticated.", null));
            return;
        }
        updateResult.setValue(AuthResource.loading(null));

        if (photoUri != null) {
            uploadProfilePhoto(firebaseUser.getUid(), photoUri, (downloadUrl) ->
                    updateFirebaseAndFirestoreProfiles(firebaseUser, displayName, downloadUrl, updateResult)
            );
        } else {
            updateFirebaseAndFirestoreProfiles(firebaseUser, displayName, null, updateResult);
        }
    }

    private void uploadProfilePhoto(String userId, Uri photoUri, OnPhotoUploadListener listener) {
        StorageReference photoRef = storage.getReference().child("profile_photos/" + userId);
        photoRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> listener.onSuccess(uri.toString()))
                        .addOnFailureListener(e -> listener.onSuccess(null))) // Non-fatal if URL fetch fails
                .addOnFailureListener(e -> listener.onSuccess(null)); // Non-fatal if upload fails
    }

    private void updateFirebaseAndFirestoreProfiles(FirebaseUser firebaseUser, String displayName, @Nullable String photoUrl, MutableLiveData<AuthResource<Void>> updateResult) {
        UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName);

        String finalPhotoUrl = (photoUrl != null) ? photoUrl : (firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
        if(finalPhotoUrl != null) {
            profileUpdatesBuilder.setPhotoUri(Uri.parse(finalPhotoUrl));
        }

        firebaseUser.updateProfile(profileUpdatesBuilder.build()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> firestoreUpdates = new HashMap<>();
                firestoreUpdates.put("displayName", displayName);
                if (photoUrl != null) {
                    firestoreUpdates.put("photoUrl", photoUrl);
                }

                firestore.collection("users").document(firebaseUser.getUid())
                        .update(firestoreUpdates)
                        .addOnSuccessListener(aVoid -> updateResult.setValue(AuthResource.success(null)))
                        .addOnFailureListener(e -> updateResult.setValue(AuthResource.error(e.getMessage(), null)));
            } else {
                updateResult.setValue(AuthResource.error(Objects.requireNonNull(task.getException()).getMessage(), null));
            }
        });
    }

    public void sendPasswordResetEmail(String email, MutableLiveData<AuthResource<Void>> resetResultLiveData) {
        resetResultLiveData.setValue(AuthResource.loading(null));
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        resetResultLiveData.setValue(AuthResource.success(null));
                    } else {
                        Log.e(TAG, "Password reset email failed: ", task.getException());
                        resetResultLiveData.setValue(AuthResource.error(Objects.requireNonNull(task.getException()).getMessage(), null));
                    }
                });
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    interface OnPhotoUploadListener {
        void onSuccess(@Nullable String downloadUrl);
    }
}