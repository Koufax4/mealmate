package com.example.mealmate;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;

    private MaterialCardView navContainer;
    private final List<LinearLayout> navItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        setupCustomNavigation();

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isAuthDestination = isAuthDestination(destination);

            navContainer.setVisibility(isAuthDestination ? View.GONE : View.VISIBLE);

            if (!isAuthDestination) {
                updateNavigationSelection(destination.getId());
            }
        });
    }

    private boolean isAuthDestination(NavDestination destination) {
        return destination != null && (destination.getId() == R.id.navigation_login ||
                destination.getId() == R.id.navigation_register ||
                destination.getId() == R.id.navigation_forgot_password);
    }

    private void setupCustomNavigation() {
        navContainer = findViewById(R.id.nav_container);
        LinearLayout navView = findViewById(R.id.nav_view);

        navItems.add(findViewById(R.id.nav_home));
        navItems.add(findViewById(R.id.nav_recipe));
        navItems.add(findViewById(R.id.nav_meal_plan));
        navItems.add(findViewById(R.id.nav_grocery));
        navItems.add(findViewById(R.id.nav_map));

        // Enable animations on the container
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        navView.setLayoutTransition(layoutTransition);

        // Set click listeners
        findViewById(R.id.nav_home).setOnClickListener(v -> navigateTo(R.id.navigation_home));
        findViewById(R.id.nav_recipe).setOnClickListener(v -> navigateTo(R.id.recipeListFragment));
        findViewById(R.id.nav_meal_plan).setOnClickListener(v -> navigateTo(R.id.mealPlanFragment));
        findViewById(R.id.nav_grocery).setOnClickListener(v -> navigateTo(R.id.groceryListFragment));
//        findViewById(R.id.nav_map).setOnClickListener(v -> navigateTo(R.id.mapFragment));
    }

    private void navigateTo(int destinationId) {
        if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != destinationId) {
            navController.navigate(destinationId);
        }
    }

    private void updateNavigationSelection(int destinationId) {
        for (LinearLayout item : navItems) {
            boolean isSelected = false;
            int itemId = item.getId();

            if (itemId == R.id.nav_home && destinationId == R.id.navigation_home) {
                isSelected = true;
            } else if (itemId == R.id.nav_recipe && (destinationId == R.id.recipeListFragment || destinationId == R.id.addRecipeFragment || destinationId == R.id.recipeDetailFragment)) {
                isSelected = true;
            } else if (itemId == R.id.nav_meal_plan && destinationId == R.id.mealPlanFragment) {
                isSelected = true;
            } else if (itemId == R.id.nav_grocery && destinationId == R.id.groceryListFragment) {
                isSelected = true;
            }
//            else if (itemId == R.id.nav_map && destinationId == R.id.mapFragment) {
//                isSelected = true;
//            }

            setNavItemSelected(item, isSelected);
        }
    }

    private void setNavItemSelected(LinearLayout navItem, boolean isSelected) {
        ImageView icon = (ImageView) navItem.getChildAt(0);
        TextView text = (TextView) navItem.getChildAt(1);

        if (isSelected) {
            navItem.setBackgroundResource(R.drawable.nav_item_selected_background);
            icon.setColorFilter(ContextCompat.getColor(this, R.color.purple_700));
            text.setVisibility(View.VISIBLE);
        } else {
            navItem.setBackgroundResource(R.drawable.nav_item_background);
            icon.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
            text.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        NavDestination currentDestination = navController.getCurrentDestination();

        boolean isAuthDestination = currentDestination != null &&
                (currentDestination.getId() == R.id.navigation_login ||
                        currentDestination.getId() == R.id.navigation_register ||
                        currentDestination.getId() == R.id.navigation_forgot_password);

        if (currentUser != null) {
            if (isAuthDestination) {
                navController.popBackStack(currentDestination.getId(), true);
                navController.navigate(R.id.navigation_home);
            }
        } else {
            if (currentDestination != null && !isAuthDestination) {
                navController.popBackStack(R.id.mobile_navigation, true);
                navController.navigate(R.id.navigation_login);
            }
        }
    }
}