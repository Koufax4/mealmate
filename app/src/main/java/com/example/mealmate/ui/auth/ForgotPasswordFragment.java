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
import com.example.mealmate.databinding.FragmentForgotPasswordBinding;

public class ForgotPasswordFragment extends Fragment {

    private static final String TAG = "ForgotPasswordFragment";
    private FragmentForgotPasswordBinding binding;
    private AuthViewModel authViewModel;

    public ForgotPasswordFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSendResetLink.setOnClickListener(v -> attemptSendPasswordResetEmail());

        authViewModel.getPasswordResetOperationState().observe(getViewLifecycleOwner(), authResource -> {
            if (authResource == null) return;

            if (NavHostFragment.findNavController(this).getCurrentDestination() == null ||
                    NavHostFragment.findNavController(this).getCurrentDestination().getId() != R.id.navigation_forgot_password) {
                return;
            }

            switch (authResource.status) {
                case LOADING:
                    binding.progressBarForgotPassword.setVisibility(View.VISIBLE);
                    binding.buttonSendResetLink.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBarForgotPassword.setVisibility(View.GONE);
                    binding.buttonSendResetLink.setEnabled(true);
                    Toast.makeText(getContext(), "Password reset email sent successfully.", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(this).popBackStack();
                    authViewModel.clearAuthOperationState();
                    break;
                case ERROR:
                    binding.progressBarForgotPassword.setVisibility(View.GONE);
                    binding.buttonSendResetLink.setEnabled(true);
                    Toast.makeText(getContext(), "Failed: " + authResource.message, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Password Reset Error: " + authResource.message);
                    authViewModel.clearAuthOperationState();
                    break;
            }
        });
    }

    private void attemptSendPasswordResetEmail() {
        String email = binding.editTextEmailForgot.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            binding.textFieldEmailForgotLayout.setError("Email is required");
            return;
        } else {
            binding.textFieldEmailForgotLayout.setError(null);
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textFieldEmailForgotLayout.setError("Enter a valid email address");
            return;
        } else {
            binding.textFieldEmailForgotLayout.setError(null);
        }

        authViewModel.performPasswordReset(email); // Call the updated ViewModel method
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}