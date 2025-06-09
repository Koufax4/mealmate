package com.example.mealmate.ui.mealplan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.MealPlan;
import com.example.mealmate.data.model.Recipe;
import com.example.mealmate.data.repository.MealPlanRepository;
import com.example.mealmate.data.repository.RecipeRepository;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ViewModel for managing meal plan-related UI state and coordinating with
 * MealPlanRepository and RecipeRepository.
 */
public class MealPlanViewModel extends ViewModel {

    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;

    private final MutableLiveData<AuthResource<MealPlan>> saveMealPlanResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<List<MealPlan>>> mealPlansLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<MealPlan>> currentMealPlanLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Void>> deleteMealPlanResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<List<Recipe>>> recipesLiveData = new MutableLiveData<>();

    private MealPlan currentMealPlan;
    private Calendar currentWeekStart;
    private final Map<String, Recipe> recipeMap = new HashMap<>();

    public MealPlanViewModel() {
        this.mealPlanRepository = new MealPlanRepository();
        this.recipeRepository = new RecipeRepository();
        initializeCurrentWeek();
    }

    private void initializeCurrentWeek() {
        currentWeekStart = Calendar.getInstance();
        setCalendarToMonday(currentWeekStart);
    }

    private void setCalendarToMonday(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public void createNewMealPlan() {
        String planName = "Week of " + getFormattedWeekStart();
        MealPlan newPlan = new MealPlan(null, planName, new Timestamp(currentWeekStart.getTime()));
        Map<String, List<String>> days = new HashMap<>();
        String[] dayNames = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        for (String day : dayNames) {
            days.put(day, new ArrayList<>());
        }
        newPlan.setDays(days);
        this.currentMealPlan = newPlan;
        currentMealPlanLiveData.setValue(AuthResource.success(this.currentMealPlan));
    }

    public void setCurrentMealPlan(MealPlan mealPlan) {
        this.currentMealPlan = mealPlan;
    }

    public void loadCurrentWeekMealPlan() {
        Timestamp weekStartTimestamp = new Timestamp(currentWeekStart.getTime());
        mealPlanRepository.getMealPlanForWeek(weekStartTimestamp, currentMealPlanLiveData);
    }

    public void assignRecipeToDay(String day, String recipeId) {
        if (currentMealPlan == null) return;
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
        if (!dayRecipes.contains(recipeId)) {
            dayRecipes.add(recipeId);
        }
        currentMealPlanLiveData.setValue(AuthResource.success(currentMealPlan));
    }

    public void removeRecipeFromDay(String day, String recipeId) {
        if (currentMealPlan == null || currentMealPlan.getDays() == null) return;
        List<String> dayRecipes = currentMealPlan.getDays().get(day);
        if (dayRecipes != null) {
            dayRecipes.remove(recipeId);
            currentMealPlanLiveData.setValue(AuthResource.success(currentMealPlan));
        }
    }

    public void saveMealPlan() {
        if (currentMealPlan != null) {
            mealPlanRepository.saveMealPlan(currentMealPlan, saveMealPlanResult);
        }
    }

    public void loadUserRecipes() {
        recipeRepository.getUserRecipes(recipesLiveData);
    }

    public void populateRecipeMap(List<Recipe> recipes) {
        recipeMap.clear();
        if (recipes != null) {
            for (Recipe recipe : recipes) {
                recipeMap.put(recipe.getRecipeId(), recipe);
            }
        }
    }

    public Recipe getRecipeById(String recipeId) {
        return recipeMap.get(recipeId);
    }

    public void goToPreviousWeek() {
        currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
        loadCurrentWeekMealPlan();
    }

    public void goToNextWeek() {
        currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
        loadCurrentWeekMealPlan();
    }

    public String getFormattedWeekStart() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
        Calendar endOfWeek = (Calendar) currentWeekStart.clone();
        endOfWeek.add(Calendar.DAY_OF_MONTH, 6);
        return sdf.format(currentWeekStart.getTime()) + " - " + sdf.format(endOfWeek.getTime());
    }

    public MealPlan getCurrentMealPlan() {
        return currentMealPlan;
    }

    public long getCurrentWeekStartMillis() {
        return currentWeekStart.getTimeInMillis();
    }

    public List<Date> getCurrentWeekDates() {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = (Calendar) currentWeekStart.clone();
        for (int i = 0; i < 7; i++) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }

    public LiveData<AuthResource<MealPlan>> getSaveMealPlanResult() {
        return saveMealPlanResult;
    }

    public LiveData<AuthResource<MealPlan>> getCurrentMealPlanLiveData() {
        return currentMealPlanLiveData;
    }

    public LiveData<AuthResource<List<Recipe>>> getRecipesLiveData() {
        return recipesLiveData;
    }

    public void clearSaveMealPlanResult() {
        saveMealPlanResult.setValue(null);
    }
}