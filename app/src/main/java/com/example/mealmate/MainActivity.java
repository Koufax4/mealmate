package com.example.mealmate;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.example.mealmate.databinding.ActivityMainBinding;
import com.example.mealmate.ui.auth.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;

    // Navigation items
    private LinearLayout navHome, navRecipe, navMealPlan, navGrocery, navMap;
    private MaterialCardView navContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Initialize navigation controller
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Setup custom navigation
        setupCustomNavigation();

        // Manage navigation visibility
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_login ||
                    destination.getId() == R.id.navigation_register ||
                    destination.getId() == R.id.navigation_forgot_password) {
                navContainer.setVisibility(View.GONE);
            } else {
                navContainer.setVisibility(View.VISIBLE);
                updateNavigationSelection(destination.getId());
            }
        });
    }

    private void setupCustomNavigation() {
        navContainer = findViewById(R.id.nav_container);
        navHome = findViewById(R.id.nav_home);
        navRecipe = findViewById(R.id.nav_recipe);
        navMealPlan = findViewById(R.id.nav_meal_plan);
        navGrocery = findViewById(R.id.nav_grocery);
        navMap = findViewById(R.id.nav_map);

        // Set click listeners
        navHome.setOnClickListener(v -> {
            navController.navigate(R.id.navigation_home);
        });

        navRecipe.setOnClickListener(v -> {
            navController.navigate(R.id.recipeListFragment);
        });

        navMealPlan.setOnClickListener(v -> {
            navController.navigate(R.id.mealPlanFragment);
        });

        navGrocery.setOnClickListener(v -> {
            navController.navigate(R.id.groceryListFragment);
        });

        navMap.setOnClickListener(v -> {
            // For now, show coming soon message since map is disabled
            // navController.navigate(R.id.mapFragment);
            Toast.makeText(this, "Store Locations feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateNavigationSelection(int destinationId) {
        // Reset all items to unselected state
        resetAllNavItems();

        // Highlight the selected item
        if (destinationId == R.id.navigation_home) {
            setNavItemSelected(navHome, R.drawable.ic_home_black_24dp, "Home", true);
        } else if (destinationId == R.id.recipeListFragment ||
                destinationId == R.id.addRecipeFragment ||
                destinationId == R.id.recipeDetailFragment) {
            setNavItemSelected(navRecipe, R.drawable.ic_restaurant_24, null, false);
        } else if (destinationId == R.id.mealPlanFragment) {
            setNavItemSelected(navMealPlan, R.drawable.ic_calendar_today_24, null, false);
        } else if (destinationId == R.id.groceryListFragment) {
            setNavItemSelected(navGrocery, R.drawable.ic_shopping_cart_24, null, false);
        }
        // Note: Map case will be added when map feature is re-enabled
    }

    private void resetAllNavItems() {
        setNavItemSelected(navHome, R.drawable.ic_home_black_24dp, null, false);
        setNavItemSelected(navRecipe, R.drawable.ic_restaurant_24, null, false);
        setNavItemSelected(navMealPlan, R.drawable.ic_calendar_today_24, null, false);
        setNavItemSelected(navGrocery, R.drawable.ic_shopping_cart_24, null, false);
        setNavItemSelected(navMap, R.drawable.ic_location_on_24, null, false);
    }

    private void setNavItemSelected(LinearLayout navItem, int iconRes, String text, boolean isSelected) {
        ImageView icon = navItem.getChildAt(0) instanceof ImageView ? (ImageView) navItem.getChildAt(0) : null;
        TextView textView = null;

        // Find text view if it exists
        for (int i = 0; i < navItem.getChildCount(); i++) {
            View child = navItem.getChildAt(i);
            if (child instanceof TextView) {
                textView = (TextView) child;
                break;
            }
        }

        if (icon != null) {
            icon.setImageResource(iconRes);
            if (isSelected) {
                icon.setColorFilter(ContextCompat.getColor(this, R.color.purple_700));
                navItem.setBackgroundResource(R.drawable.nav_item_selected_background);
            } else {
                icon.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                navItem.setBackgroundResource(R.drawable.nav_item_background);
            }
        }

        if (textView != null) {
            if (isSelected && text != null) {
                textView.setText(text);
                textView.setVisibility(View.VISIBLE);
                textView.setTextColor(ContextCompat.getColor(this, R.color.purple_700));
            } else {
                textView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        NavDestination currentDestination = navController.getCurrentDestination();

        if (currentUser != null) {
            if (currentDestination != null && currentDestination.getId() == R.id.navigation_login) {
                navController.popBackStack(R.id.navigation_login, true);
                navController.navigate(R.id.navigation_home);
            } else if (currentDestination != null &&
                    (currentDestination.getId() == R.id.navigation_register ||
                            currentDestination.getId() == R.id.navigation_forgot_password)) {
                navController.popBackStack(currentDestination.getId(), true);
                navController.navigate(R.id.navigation_home);
            }
        } else {
            if (currentDestination != null &&
                    (currentDestination.getId() == R.id.navigation_home ||
                            currentDestination.getId() == R.id.navigation_dashboard ||
                            currentDestination.getId() == R.id.navigation_notifications)) {

                navController.popBackStack(R.id.mobile_navigation, true);
                navController.navigate(R.id.navigation_login);
            }
        }
    }
}