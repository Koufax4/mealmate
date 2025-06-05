package com.example.mealmate.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<AuthResource<FirebaseUser>> userAuthOperationState;
    private final MutableLiveData<AuthResource<Void>> passwordResetOperationState;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
        userAuthOperationState = new MutableLiveData<>();
        passwordResetOperationState = new MutableLiveData<>();
    }

    public void login(String email, String password) {
        authRepository.loginUser(email, password, userAuthOperationState);
    }

    public void register(String email, String password) {
        authRepository.registerUser(email, password, userAuthOperationState);
    }

    public void performPasswordReset(String email) { // Renamed from sendPasswordResetEmail for clarity
        authRepository.sendPasswordResetEmail(email, passwordResetOperationState);
    }

    public LiveData<AuthResource<FirebaseUser>> getUserAuthOperationState() {
        return userAuthOperationState;
    }

    public LiveData<AuthResource<Void>> getPasswordResetOperationState() {
        return passwordResetOperationState;
    }


    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public void clearAuthOperationState() {
        userAuthOperationState.setValue(null);
        passwordResetOperationState.setValue(null);
    }
}