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
import com.example.mealmate.databinding.FragmentLoginBinding;
import com.example.mealmate.viewmodels.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Scoped to this fragment
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonLogin.setOnClickListener(v -> attemptLogin());

        binding.textViewRegisterNow.setOnClickListener(v ->
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_loginFragment_to_navigation_register)
        );

        binding.textViewForgotPassword.setOnClickListener(v ->
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        );

        authViewModel.getUserAuthOperationState().observe(getViewLifecycleOwner(), authResource -> {
            if (authResource == null) return;
            if (NavHostFragment.findNavController(this).getCurrentDestination() == null ||
                    NavHostFragment.findNavController(this).getCurrentDestination().getId() != R.id.navigation_login) {

                return;
            }


            switch (authResource.status) {
                case LOADING:
                    binding.progressBarLogin.setVisibility(View.VISIBLE);
                    binding.buttonLogin.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBarLogin.setVisibility(View.GONE);
                    binding.buttonLogin.setEnabled(true);
                    FirebaseUser user = authResource.data;
                    Toast.makeText(getContext(), "Login Successful: " + (user != null ? user.getEmail() : "Unknown user"), Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(LoginFragment.this)
                            .navigate(R.id.action_loginFragment_to_navigation_home);
                    authViewModel.clearAuthOperationState();
                    break;
                case ERROR:
                    binding.progressBarLogin.setVisibility(View.GONE);
                    binding.buttonLogin.setEnabled(true);
                    Toast.makeText(getContext(), "Login Failed: " + authResource.message, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Login Error: " + authResource.message);
                    authViewModel.clearAuthOperationState();
                    break;
            }
        });
    }

    private void attemptLogin() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.textFieldEmailLayout.setError("Email is required");
            return;
        } else {
            binding.textFieldEmailLayout.setError(null);
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textFieldEmailLayout.setError("Enter a valid email address");
            return;
        } else {
            binding.textFieldEmailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.textFieldPasswordLayout.setError("Password is required");
            return;
        } else {
            binding.textFieldPasswordLayout.setError(null);
        }


        authViewModel.login(email, password);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}