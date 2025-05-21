package com.example.mealmate.ui.auth;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mealmate.R;
import com.example.mealmate.databinding.FragmentRegisterBinding;
import com.example.mealmate.viewmodels.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";
    private FragmentRegisterBinding binding;
    private AuthViewModel authViewModel;

    public RegisterFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonRegister.setOnClickListener(v -> attemptRegistration());

        binding.textViewLoginNow.setOnClickListener(v ->
                NavHostFragment.findNavController(RegisterFragment.this)
                        .navigate(R.id.action_registerFragment_to_loginFragment)
        );

        authViewModel.getUserAuthOperationState().observe(getViewLifecycleOwner(), authResource -> {
            if (authResource == null) return;
            if (NavHostFragment.findNavController(this).getCurrentDestination() == null ||
                    NavHostFragment.findNavController(this).getCurrentDestination().getId() != R.id.navigation_register) {
                return;
            }


            switch (authResource.status) {
                case LOADING:
                    binding.progressBarRegister.setVisibility(View.VISIBLE);
                    binding.buttonRegister.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBarRegister.setVisibility(View.GONE);
                    binding.buttonRegister.setEnabled(true);
                    FirebaseUser user = authResource.data;
                    Toast.makeText(getContext(), "Registration Successful: " + (user != null ? user.getEmail() : ""), Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(RegisterFragment.this)
                            .navigate(R.id.action_registerFragment_to_navigation_home);
                    authViewModel.clearAuthOperationState();
                    break;
                case ERROR:
                    binding.progressBarRegister.setVisibility(View.GONE);
                    binding.buttonRegister.setEnabled(true);
                    Toast.makeText(getContext(), "Registration Failed: " + authResource.message, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Registration Error: " + authResource.message);
                    authViewModel.clearAuthOperationState();
                    break;
            }
        });
    }

    private void attemptRegistration() {
        String email = binding.editTextEmailRegister.getText().toString().trim();
        String password = binding.editTextPasswordRegister.getText().toString().trim();
        String confirmPassword = binding.editTextConfirmPasswordRegister.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.textFieldEmailRegisterLayout.setError("Email is required");
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textFieldEmailRegisterLayout.setError("Enter a valid email address");
            return;
        } else {
            binding.textFieldEmailRegisterLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.textFieldPasswordRegisterLayout.setError("Password is required");
            return;
        } else if (password.length() < 6) {
            binding.textFieldPasswordRegisterLayout.setError("Password must be at least 6 characters");
            return;
        } else {
            binding.textFieldPasswordRegisterLayout.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.textFieldConfirmPasswordRegisterLayout.setError("Confirm password is required");
            return;
        } else {
            binding.textFieldConfirmPasswordRegisterLayout.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            binding.textFieldConfirmPasswordRegisterLayout.setError("Passwords do not match");
            return;
        } else {
            binding.textFieldConfirmPasswordRegisterLayout.setError(null);
        }

        authViewModel.register(email, password);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}