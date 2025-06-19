package com.example.mealmate.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.mealmate.R;
import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupModernHeader();
        setupDynamicGreeting();
        setupNavigationCards();
        setupObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserInfo();
        homeViewModel.loadDashboardData();
    }

    private void updateUserInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && binding != null) {
            // **FIX: Clear any existing tint before loading a new image**
            binding.imageProfile.clearColorFilter();
            binding.imageProfile.setImageTintList(null);

            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(binding.imageProfile);
            } else {
                Glide.with(this)
                        .load(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.imageProfile);
            }

            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                binding.textUserName.setText(currentUser.getDisplayName() + " 👨‍🍳");
            } else if (currentUser.getEmail() != null) {
                binding.textUserName.setText(currentUser.getEmail().split("@")[0] + " 👨‍🍳");
            } else {
                binding.textUserName.setText("Chef! 👨‍🍳");
            }
        }
    }

    private void setupModernHeader() {
        updateUserInfo();

        binding.buttonSettings.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_profileFragment));

        binding.imageProfile.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_profileFragment));
    }

    private void setupDynamicGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        String emoji;

        if (hourOfDay >= 5 && hourOfDay < 12) {
            greeting = "Good Morning";
            emoji = "🌅";
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            greeting = "Good Afternoon";
            emoji = "☀️";
        } else if (hourOfDay >= 17 && hourOfDay < 21) {
            greeting = "Good Evening";
            emoji = "🌆";
        } else {
            greeting = "Good Night";
            emoji = "🌙";
        }

        binding.textHeroTitle.setText(greeting + "! " + emoji);
    }

    private void setupNavigationCards() {
        binding.cardMyRecipes.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_navigation_home_to_recipeListFragment));

        binding.cardMealPlanner.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_navigation_home_to_mealPlanFragment));

        binding.cardGroceryLists.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_navigation_home_to_groceryListFragment));

        binding.cardStoreLocations.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_navigation_home_to_mapFragment));
    }

    private void setupObservers() {
        homeViewModel.getRecipesLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == AuthResource.Status.SUCCESS && resource.data != null) {
                binding.textRecipeCountStats.setText(String.valueOf(resource.data.size()));
            } else {
                binding.textRecipeCountStats.setText("0");
            }
        });

        homeViewModel.getMealPlansLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == AuthResource.Status.SUCCESS && resource.data != null) {
                binding.textMealPlanCountStats.setText(String.valueOf(resource.data.size()));
            } else {
                binding.textMealPlanCountStats.setText("0");
            }
        });

        homeViewModel.getUnpurchasedItemsCount().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == AuthResource.Status.SUCCESS && resource.data != null) {
                binding.textGroceryListCountStats.setText(String.valueOf(resource.data));
            } else {
                binding.textGroceryListCountStats.setText("0");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}