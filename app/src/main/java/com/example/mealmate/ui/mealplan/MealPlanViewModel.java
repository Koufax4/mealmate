// app/src/main/java/com/example/mealmate/ui/mealplan/MealPlanViewModel.java
package com.example.mealmate.ui.mealplan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.MealPlan;
import com.example.mealmate.data.model.Recipe;
import com.example.mealmate.data.repository.MealPlanRepository;
import com.example.mealmate.data.repository.RecipeRepository;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for managing meal plan-related UI state and coordinating with
 * MealPlanRepository and RecipeRepository.
 */
public class MealPlanViewModel extends ViewModel {

    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;

    // LiveData for meal plan operations
    private final MutableLiveData<AuthResource<MealPlan>> saveMealPlanResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<List<MealPlan>>> mealPlansLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<MealPlan>> currentMealPlanLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Void>> deleteMealPlanResult = new MutableLiveData<>();

    // Private LiveData for internal updates
    private final MutableLiveData<AuthResource<List<Recipe>>> _recipesLiveData = new MutableLiveData<>();
    // Public LiveData that fragment observes. The transformation updates the recipeMap.
    public final LiveData<AuthResource<List<Recipe>>> recipesLiveData;

    // Current meal plan being edited
    private MealPlan currentMealPlan;
    private Calendar currentWeekStart;
    private Map<String, Recipe> recipeMap = new HashMap<>();

    public MealPlanViewModel() {
        this.mealPlanRepository = new MealPlanRepository();
        this.recipeRepository = new RecipeRepository();
        initializeCurrentWeek();

        // Use Transformations.map to update the recipeMap when recipes are loaded
        recipesLiveData = Transformations.map(_recipesLiveData, resource -> {
            if (resource != null && resource.status == AuthResource.Status.SUCCESS && resource.data != null) {
                recipeMap.clear();
                for (Recipe recipe : resource.data) {
                    recipeMap.put(recipe.getRecipeId(), recipe);
                }
            }
            return resource;
        });
    }

    /**
     * Initializes the current week to start on Monday.
     */
    private void initializeCurrentWeek() {
        currentWeekStart = Calendar.getInstance();
        // Set to Monday of current week
        int dayOfWeek = currentWeekStart.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        currentWeekStart.add(Calendar.DAY_OF_MONTH, -daysToSubtract);

        // Set to start of day
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0);
        currentWeekStart.set(Calendar.MINUTE, 0);
        currentWeekStart.set(Calendar.SECOND, 0);
        currentWeekStart.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Creates a new meal plan for the current week and posts it to the LiveData.
     * This will be observed by the fragment.
     */
    public void createNewMealPlan() {
        String planName = "Week of " + getFormattedWeekStart();
        MealPlan newPlan = new MealPlan(null, planName, new Timestamp(currentWeekStart.getTime()));

        // Initialize empty days map
        Map<String, List<String>> days = new HashMap<>();
        String[] dayNames = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        for (String day : dayNames) {
            days.put(day, new ArrayList<>());
        }
        newPlan.setDays(days);

        // Set the internal state and notify observers
        this.currentMealPlan = newPlan;
        currentMealPlanLiveData.setValue(AuthResource.success(this.currentMealPlan));
    }

    /**
     * Sets the ViewModel's internal state for the current meal plan.
     * @param mealPlan The meal plan to set as the current one.
     */
    public void setCurrentMealPlan(MealPlan mealPlan) {
        this.currentMealPlan = mealPlan;
    }

    /**
     * Loads the meal plan for the current week from Firestore.
     * The repository will post the result to the LiveData that the fragment observes.
     */
    public void loadCurrentWeekMealPlan() {
        Timestamp weekStartTimestamp = new Timestamp(currentWeekStart.getTime());
        mealPlanRepository.getMealPlanForWeek(weekStartTimestamp, currentMealPlanLiveData);
    }

    /**
     * Assigns a recipe to a specific day and meal slot.
     *
     * @param day      The day of the week
     * @param recipeId The recipe ID to assign
     */
    public void assignRecipeToDay(String day, String recipeId) {
        if (currentMealPlan == null) {
            // This shouldn't happen with the new logic, but as a safeguard:
            return;
        }

        Map<String, List<String>> days = currentMealPlan.getDays();
        if (days == null) {
            days = new HashMap<>();
            currentMealPlan.setDays(days);
        }

        List<String> dayRecipes = days.get(day);
        if (dayRecipes == null) {
            dayRecipes = new ArrayList<>();
            days.put(day, dayRecipes);
        }

        // Add recipe if not already present
        if (!dayRecipes.contains(recipeId)) {
            dayRecipes.add(recipeId);
        }

        // Post the updated plan to trigger UI refresh
        currentMealPlanLiveData.setValue(AuthResource.success(currentMealPlan));
    }

    /**
     * Removes a recipe from a specific day.
     *
     * @param day      The day of the week
     * @param recipeId The recipe ID to remove
     */
    public void removeRecipeFromDay(String day, String recipeId) {
        if (currentMealPlan == null || currentMealPlan.getDays() == null) {
            return;
        }

        List<String> dayRecipes = currentMealPlan.getDays().get(day);
        if (dayRecipes != null) {
            dayRecipes.remove(recipeId);
            // Post the updated plan to trigger UI refresh
            currentMealPlanLiveData.setValue(AuthResource.success(currentMealPlan));
        }
    }

    /**
     * Saves the current meal plan.
     */
    public void saveMealPlan() {
        if (currentMealPlan != null) {
            mealPlanRepository.saveMealPlan(currentMealPlan, saveMealPlanResult);
        }
    }

    /**
     * Loads all meal plans for the current user.
     */
    public void loadUserMealPlans() {
        mealPlanRepository.getUserMealPlans(mealPlansLiveData);
    }

    /**
     * Loads all recipes for recipe selection.
     */
    public void loadUserRecipes() {
        // The repository now updates the private _recipesLiveData
        recipeRepository.getUserRecipes(_recipesLiveData);
    }

    /**
     * Gets a recipe by its ID using the efficient lookup map.
     * @param recipeId The ID of the recipe.
     * @return The Recipe object, or null if not found.
     */
    public Recipe getRecipeById(String recipeId) {
        return recipeMap.get(recipeId);
    }

    /**
     * Deletes a meal plan.
     *
     * @param mealPlan The meal plan to delete
     */
    public void deleteMealPlan(MealPlan mealPlan) {
        mealPlanRepository.deleteMealPlan(mealPlan, deleteMealPlanResult);
    }

    /**
     * Navigates to the previous week.
     */
    public void goToPreviousWeek() {
        currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
        loadCurrentWeekMealPlan();
    }

    /**
     * Navigates to the next week.
     */
    public void goToNextWeek() {
        currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
        loadCurrentWeekMealPlan();
    }

    /**
     * Gets the formatted week start date.
     *
     * @return Formatted week start date string
     */
    public String getFormattedWeekStart() {
        Calendar endOfWeek = (Calendar) currentWeekStart.clone();
        endOfWeek.add(Calendar.DAY_OF_MONTH, 6);

        return String.format("%02d/%02d - %02d/%02d",
                currentWeekStart.get(Calendar.MONTH) + 1,
                currentWeekStart.get(Calendar.DAY_OF_MONTH),
                endOfWeek.get(Calendar.MONTH) + 1,
                endOfWeek.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Gets the current meal plan.
     *
     * @return The current meal plan
     */
    public MealPlan getCurrentMealPlan() {
        return currentMealPlan;
    }

    // Getters for LiveData
    public LiveData<AuthResource<MealPlan>> getSaveMealPlanResult() {
        return saveMealPlanResult;
    }

    public LiveData<AuthResource<List<MealPlan>>> getMealPlansLiveData() {
        return mealPlansLiveData;
    }

    public LiveData<AuthResource<MealPlan>> getCurrentMealPlanLiveData() {
        return currentMealPlanLiveData;
    }

    public LiveData<AuthResource<Void>> getDeleteMealPlanResult() {
        return deleteMealPlanResult;
    }

    public LiveData<AuthResource<List<Recipe>>> getRecipesLiveData() {
        // The fragment will observe the public, transformed LiveData
        return recipesLiveData;
    }

    /**
     * Clears the save meal plan result.
     */
    public void clearSaveMealPlanResult() {
        saveMealPlanResult.setValue(null);
    }

    /**
     * Clears the delete meal plan result.
     */
    public void clearDeleteMealPlanResult() {
        deleteMealPlanResult.setValue(null);
    }
}