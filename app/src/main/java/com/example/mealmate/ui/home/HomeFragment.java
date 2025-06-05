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
import androidx.navigation.fragment.NavHostFragment;

import com.example.mealmate.R;
import com.example.mealmate.databinding.FragmentHomeBinding;
import com.example.mealmate.ui.auth.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

/**
 * HomeFragment displays the main landing screen after user login.
 * Features a modern design with glassmorphism hero card, navigation grid,
 * dynamic greeting, and quick stats dashboard.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AuthViewModel authViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize the modern UI components
        setupDynamicGreeting();
        setupNavigationCards();
        setupQuickStats();
        setupLogoutButton();

        return root;
    }

    /**
     * Sets up dynamic greeting based on time of day
     */
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

        binding.textWelcome.setText(greeting + "! " + emoji);

        // Set dynamic subtitle based on time
        String subtitle;
        if (hourOfDay >= 5 && hourOfDay < 11) {
            subtitle = "Ready to plan some delicious meals?";
        } else if (hourOfDay >= 11 && hourOfDay < 14) {
            subtitle = "Time to prep lunch or plan dinner!";
        } else if (hourOfDay >= 14 && hourOfDay < 18) {
            subtitle = "Perfect time for meal planning!";
        } else {
            subtitle = "Plan tomorrow's delicious meals!";
        }

        binding.textSubtitle.setText(subtitle);
    }

    /**
     * Sets up navigation cards with click listeners
     */
    private void setupNavigationCards() {
        // My Recipes Card
        binding.cardMyRecipes.setOnClickListener(v -> {
            // TODO: Navigate to recipes list fragment
            // NavHostFragment.findNavController(HomeFragment.this)
            // .navigate(R.id.navigation_recipes);
        });

        // Meal Planner Card
        binding.cardMealPlanner.setOnClickListener(v -> {
            // TODO: Navigate to meal planner fragment
            // NavHostFragment.findNavController(HomeFragment.this)
            // .navigate(R.id.navigation_meal_planner);
        });

        // Grocery Lists Card
        binding.cardGroceryLists.setOnClickListener(v -> {
            // TODO: Navigate to grocery lists fragment
            // NavHostFragment.findNavController(HomeFragment.this)
            // .navigate(R.id.navigation_grocery_lists);
        });

        // Settings Card
        binding.cardSettings.setOnClickListener(v -> {
            // TODO: Navigate to settings fragment
            // NavHostFragment.findNavController(HomeFragment.this)
            // .navigate(R.id.navigation_settings);
        });

        // Floating Action Button for Quick Add Recipe
        binding.fabQuickAdd.setOnClickListener(v -> {
            // TODO: Navigate to add recipe fragment
            // NavHostFragment.findNavController(HomeFragment.this)
            // .navigate(R.id.navigation_add_recipe);
        });
    }

    /**
     * Sets up quick stats dashboard (placeholder values for now)
     */
    private void setupQuickStats() {
        // TODO: These will be populated from actual data in future phases
        binding.textRecipeCount.setText("0");
        binding.textMealPlansCount.setText("0");
        binding.textGroceryListsCount.setText("0");
    }

    /**
     * Sets up logout button functionality
     */
    private void setupLogoutButton() {
        binding.buttonLogout.setOnClickListener(v -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Navigate back to login screen
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.navigation_login);

            // Clear the back stack to prevent returning to home screen
            NavHostFragment.findNavController(HomeFragment.this)
                    .popBackStack(R.id.mobile_navigation, true);

            // Navigate to login again to ensure we're on the login screen
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.navigation_login);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}