package com.example.mealmate;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mealmate.databinding.ActivityMainBinding;
import com.example.mealmate.ui.auth.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_login ||
                    destination.getId() == R.id.navigation_register ||
                    destination.getId() == R.id.navigation_forgot_password) {
                navView.setVisibility(View.GONE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            } else if (destination.getId() == R.id.navigation_home) {
                // Hide ActionBar for home screen since we have custom header
                navView.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            } else {
                navView.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }
            }
        });
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