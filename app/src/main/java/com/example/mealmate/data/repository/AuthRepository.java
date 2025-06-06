package com.example.mealmate.data.repository;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
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
                        userAuthLiveData.setValue(AuthResource.error(task.getException().getMessage(), null));
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
                        userAuthLiveData.setValue(AuthResource.error(task.getException().getMessage(), null));
                    }
                });
    }

    private void createUserProfileDocument(FirebaseUser firebaseUser, MutableLiveData<AuthResource<FirebaseUser>> liveData) {
        String userId = firebaseUser.getUid();
        // Use the User data model for consistency
        User newUser = new User(
                userId,
                firebaseUser.getEmail(),
                null, // displayName can be added later
                null, // photoUrl can be added later
                Timestamp.now() // Use client-side timestamp for simplicity here
        );

        firestore.collection("users").document(userId)
                .set(newUser) // Use the User object directly
                .addOnCompleteListener(profileTask -> {
                    if (profileTask.isSuccessful()) {
                        Log.d(TAG, "User profile created for: " + userId);
                        liveData.setValue(AuthResource.success(firebaseUser));
                    } else {
                        Log.e(TAG, "Failed to create user profile: ", profileTask.getException());
                        // Rollback auth creation if profile creation fails
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


    public void sendPasswordResetEmail(String email, MutableLiveData<AuthResource<Void>> resetResultLiveData) { // Modified signature
        resetResultLiveData.setValue(AuthResource.loading(null));

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        resetResultLiveData.setValue(AuthResource.success(null));
                    } else {
                        Log.e(TAG, "Password reset email failed: ", task.getException());
                        resetResultLiveData.setValue(AuthResource.error(task.getException().getMessage(), null));
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
}