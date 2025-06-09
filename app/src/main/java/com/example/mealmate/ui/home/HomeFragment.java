// app/src/main/java/com/example/mealmate/ui/home/HomeFragment.java
package com.example.mealmate.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mealmate.R;
import com.example.mealmate.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

/**
 * HomeFragment displays the main landing screen after user login.
 * Features a modern design with glassmorphism hero card, navigation grid,
 * dynamic greeting, and quick stats dashboard with fixed header.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize the modern UI components
        setupModernHeader();
        setupDynamicGreeting();
        setupNavigationCards();

        return root;
    }

    /**
     * Sets up the modern innovative header with profile and actions
     */
    private void setupModernHeader() {
        // Search button click handler
        binding.buttonSearch.setOnClickListener(v -> {
            // TODO: Implement search functionality
            // Could open a search dialog or navigate to search screen
        });

        // Notifications button click handler
        binding.buttonNotifications.setOnClickListener(v -> {
            // TODO: Implement notifications functionality
            // Could show notification panel or navigate to notifications screen
        });

        // Profile avatar click handler
        binding.imageProfile.setOnClickListener(v -> {
            // TODO: Navigate to profile screen
            // NavHostFragment.findNavController(HomeFragment.this)
            // .navigate(R.id.navigation_profile);
        });

        // Optional: Show notification badge if there are notifications
        // binding.notificationBadge.setVisibility(View.VISIBLE);
    }

    /**
     * Sets up dynamic greeting based on time of day in hero section
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

        binding.textHeroTitle.setText(greeting + "! " + emoji);
    }

    /**
     * Sets up navigation cards with click listeners
     */
    private void setupNavigationCards() {
        // My Recipes Card
        binding.cardMyRecipes.setOnClickListener(v -> {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_navigation_home_to_recipeListFragment);
        });

        // Meal Planner Card
        binding.cardMealPlanner.setOnClickListener(v -> {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_navigation_home_to_mealPlanFragment);
        });

        // Grocery Lists Card
        binding.cardGroceryLists.setOnClickListener(v -> {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_navigation_home_to_groceryListFragment);
        });

        // Store Locations Card
        binding.cardStoreLocations.setOnClickListener(v -> {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_navigation_home_to_mapFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}