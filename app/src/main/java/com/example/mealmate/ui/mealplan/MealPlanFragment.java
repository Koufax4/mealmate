package com.example.mealmate.ui.mealplan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mealmate.R;
import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.MealPlan;
import com.example.mealmate.data.model.Recipe;
import com.example.mealmate.databinding.FragmentMealPlanBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MealPlanFragment displays the weekly meal planning interface.
 * Users can assign recipes to different days of the week and save their meal
 * plans.
 */
public class MealPlanFragment extends Fragment {

    private FragmentMealPlanBinding binding;
    private MealPlanViewModel mealPlanViewModel;

    // Map to store day layouts for easy access
    private Map<String, LinearLayout> dayLayouts;
    private Map<String, MaterialCardView> addButtons;

    // Current selected day for recipe assignment
    private String selectedDay;

    // Available recipes for selection
    private List<Recipe> availableRecipes;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentMealPlanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mealPlanViewModel = new ViewModelProvider(this).get(MealPlanViewModel.class);

        initializeDayLayouts();
        setupClickListeners();
        observeViewModel();

        // Load initial data
        mealPlanViewModel.loadCurrentWeekMealPlan();
        mealPlanViewModel.loadUserRecipes();
    }

    /**
     * Initializes the day layouts map for easy access.
     */
    private void initializeDayLayouts() {
        dayLayouts = new HashMap<>();
        addButtons = new HashMap<>();

        dayLayouts.put("Monday", binding.layoutMondayMeals);
        dayLayouts.put("Tuesday", binding.layoutTuesdayMeals);
        dayLayouts.put("Wednesday", binding.layoutWednesdayMeals);
        dayLayouts.put("Thursday", binding.layoutThursdayMeals);
        dayLayouts.put("Friday", binding.layoutFridayMeals);
        dayLayouts.put("Saturday", binding.layoutSaturdayMeals);
        dayLayouts.put("Sunday", binding.layoutSundayMeals);

        addButtons.put("Monday", binding.buttonAddMondayMeal);
        addButtons.put("Tuesday", binding.buttonAddTuesdayMeal);
        addButtons.put("Wednesday", binding.buttonAddWednesdayMeal);
        addButtons.put("Thursday", binding.buttonAddThursdayMeal);
        addButtons.put("Friday", binding.buttonAddFridayMeal);
        addButtons.put("Saturday", binding.buttonAddSaturdayMeal);
        addButtons.put("Sunday", binding.buttonAddSundayMeal);
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private void setupClickListeners() {
        // Toolbar navigation
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Week navigation
        binding.buttonPreviousWeek.setOnClickListener(v -> mealPlanViewModel.goToPreviousWeek());
        binding.buttonNextWeek.setOnClickListener(v -> mealPlanViewModel.goToNextWeek());

        // Save meal plan
        binding.fabSaveMealPlan.setOnClickListener(v -> mealPlanViewModel.saveMealPlan());

        // Add recipe buttons for each day
        for (Map.Entry<String, MaterialCardView> entry : addButtons.entrySet()) {
            String day = entry.getKey();
            MaterialCardView button = entry.getValue();
            button.setOnClickListener(v -> showRecipeSelectionDialog(day));
        }
    }

    /**
     * Observes ViewModel LiveData for UI updates.
     */
    private void observeViewModel() {
        // Observe current meal plan
        mealPlanViewModel.getCurrentMealPlanLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        if (resource.data != null) {
                            // We have a plan (either from Firestore or a newly created one)
                            mealPlanViewModel.setCurrentMealPlan(resource.data);
                            updateMealPlanUI(resource.data);
                        } else {
                            // This case means no plan was found in Firestore.
                            // Tell the ViewModel to create one. This will trigger this observer again.
                            mealPlanViewModel.createNewMealPlan();
                        }
                        break;
                    case ERROR:
                        showError("Failed to load meal plan: " + resource.message);
                        break;
                    case LOADING:
                        // Show loading indicator if needed by clearing the UI
                        updateMealPlanUI(null);
                        break;
                }
            }
        });

        // Observe available recipes
        mealPlanViewModel.getRecipesLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        availableRecipes = resource.data;
                        break;
                    case ERROR:
                        showError("Failed to load recipes: " + resource.message);
                        break;
                }
            }
        });

        // Observe save result
        mealPlanViewModel.getSaveMealPlanResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        showSuccess("Meal plan saved successfully!");
                        mealPlanViewModel.clearSaveMealPlanResult();
                        break;
                    case ERROR:
                        showError("Failed to save meal plan: " + resource.message);
                        mealPlanViewModel.clearSaveMealPlanResult();
                        break;
                    case LOADING:
                        // Show loading indicator
                        break;
                }
            }
        });
    }

    /**
     * Updates the UI with the current meal plan data.
     */
    private void updateMealPlanUI(MealPlan mealPlan) {
        // Update week range text regardless of meal plan state
        binding.textWeekRange.setText(mealPlanViewModel.getFormattedWeekStart());

        // Clear all days first
        for (LinearLayout dayLayout : dayLayouts.values()) {
            clearDayRecipes(dayLayout);
        }

        if (mealPlan == null) {
            // This happens during loading or error state
            return;
        }

        // Re-populate each day based on the meal plan
        for (Map.Entry<String, LinearLayout> entry : dayLayouts.entrySet()) {
            String day = entry.getKey();
            LinearLayout dayLayout = entry.getValue();

            // Add recipes for this day if the plan has days defined
            if (mealPlan.getDays() != null) {
                List<String> dayRecipes = mealPlan.getDays().get(day);
                if (dayRecipes != null && !dayRecipes.isEmpty()) {
                    for (String recipeId : dayRecipes) {
                        addRecipeToDay(dayLayout, recipeId, day);
                    }
                }
            }
        }
    }

    /**
     * Clears recipe views from a day layout, keeping only the add button.
     */
    private void clearDayRecipes(LinearLayout dayLayout) {
        // More robustly remove only recipe cards by checking their tag
        for (int i = dayLayout.getChildCount() - 1; i >= 0; i--) {
            View child = dayLayout.getChildAt(i);
            if ("recipe_card".equals(child.getTag())) {
                dayLayout.removeViewAt(i);
            }
        }
    }

    /**
     * Adds a recipe view to a day layout.
     */
    private void addRecipeToDay(LinearLayout dayLayout, String recipeId, String day) {
        // Find the recipe name using the efficient viewmodel lookup
        Recipe recipe = mealPlanViewModel.getRecipeById(recipeId);
        String recipeName = (recipe != null) ? recipe.getName() : "Unknown Recipe";

        // Create recipe card
        MaterialCardView recipeCard = new MaterialCardView(requireContext());
        // Add a tag to identify this view as a recipe card for easy removal
        recipeCard.setTag("recipe_card");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (60 * getResources().getDisplayMetrics().density));
        params.setMargins(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));
        recipeCard.setLayoutParams(params);
        recipeCard.setCardBackgroundColor(getResources().getColor(R.color.teal_100, null));
        recipeCard.setRadius(12 * getResources().getDisplayMetrics().density);
        recipeCard.setCardElevation(2 * getResources().getDisplayMetrics().density);
        recipeCard.setClickable(true);
        recipeCard.setFocusable(true);
        // Navigate to recipe detail when clicked
        recipeCard.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("recipeId", recipeId);
            args.putString("recipeName", recipeName);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mealPlanFragment_to_recipeDetailFragment, args);
        });
        // The following line caused the crash and is not needed for the ripple effect.
        // recipeCard.setForeground(getResources().getDrawable(android.R.attr.selectableItemBackground,
        // null));

        // Create content layout
        LinearLayout contentLayout = new LinearLayout(requireContext());
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        contentLayout.setPadding(
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (16 * getResources().getDisplayMetrics().density),
                (int) (16 * getResources().getDisplayMetrics().density));

        // Recipe name text
        TextView recipeText = new TextView(requireContext());
        recipeText.setText(recipeName);
        recipeText.setTextSize(14);
        recipeText.setTextColor(getResources().getColor(R.color.teal_900, null));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        recipeText.setLayoutParams(textParams);

        // Remove button
        MaterialCardView removeButton = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(
                (int) (32 * getResources().getDisplayMetrics().density),
                (int) (32 * getResources().getDisplayMetrics().density));
        removeButton.setLayoutParams(removeParams);
        removeButton.setRadius(16 * getResources().getDisplayMetrics().density);
        removeButton.setCardBackgroundColor(getResources().getColor(R.color.red_600, null));
        removeButton.setCardElevation(2 * getResources().getDisplayMetrics().density);
        removeButton.setClickable(true);
        removeButton.setFocusable(true);
        removeButton.setOnClickListener(v -> {
            mealPlanViewModel.removeRecipeFromDay(day, recipeId);
        });

        // Remove icon (using "×" text for simplicity)
        TextView removeIcon = new TextView(requireContext());
        removeIcon.setText("×");
        removeIcon.setTextSize(16);
        removeIcon.setTextColor(getResources().getColor(R.color.white, null));
        removeIcon.setGravity(android.view.Gravity.CENTER);
        removeButton.addView(removeIcon);

        contentLayout.addView(recipeText);
        contentLayout.addView(removeButton);
        recipeCard.addView(contentLayout);

        // Add to day layout (before the add button)
        int addButtonIndex = dayLayout.getChildCount() - 1;
        dayLayout.addView(recipeCard, addButtonIndex);
    }

    /**
     * Shows a recipe selection dialog for the specified day.
     */
    private void showRecipeSelectionDialog(String day) {
        if (availableRecipes == null || availableRecipes.isEmpty()) {
            showError("No recipes available. Please create some recipes first.");
            return;
        }

        selectedDay = day;

        // Create and show recipe selection dialog
        RecipeSelectionDialogFragment dialog = RecipeSelectionDialogFragment.newInstance(availableRecipes);
        dialog.setOnRecipeSelectedListener(recipe -> {
            mealPlanViewModel.assignRecipeToDay(selectedDay, recipe.getRecipeId());
        });
        dialog.show(getParentFragmentManager(), "RecipeSelectionDialog");
    }

    /**
     * Shows an error message.
     */
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a success message.
     */
    private void showSuccess(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}