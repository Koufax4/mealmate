// app/src/main/java/com/example/mealmate/ui/home/HomeFragment.java
package com.example.mealmate.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment; // Import NavHostFragment

import com.example.mealmate.R; // Import R
import com.example.mealmate.databinding.FragmentHomeBinding;
import com.example.mealmate.ui.auth.AuthViewModel; // Import AuthViewModel
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AuthViewModel authViewModel; // ViewModel for auth operations

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        binding.buttonLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.navigation_login);
            NavHostFragment.findNavController(HomeFragment.this).popBackStack(R.id.mobile_navigation, true);
            NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.navigation_login);

        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}