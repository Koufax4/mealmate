package com.example.mealmate.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.MealPlan;
import com.example.mealmate.data.model.Recipe;
import com.example.mealmate.data.repository.GroceryRepository;
import com.example.mealmate.data.repository.MealPlanRepository;
import com.example.mealmate.data.repository.RecipeRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final RecipeRepository recipeRepository;
    private final MealPlanRepository mealPlanRepository;
    private final GroceryRepository groceryRepository;

    private final MutableLiveData<AuthResource<List<Recipe>>> recipesLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<List<MealPlan>>> mealPlansLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Integer>> unpurchasedItemsCount = new MutableLiveData<>();

    public HomeViewModel() {
        recipeRepository = new RecipeRepository();
        mealPlanRepository = new MealPlanRepository();
        groceryRepository = new GroceryRepository();
    }

    public LiveData<AuthResource<List<Recipe>>> getRecipesLiveData() {
        return recipesLiveData;
    }

    public LiveData<AuthResource<List<MealPlan>>> getMealPlansLiveData() {
        return mealPlansLiveData;
    }

    public LiveData<AuthResource<Integer>> getUnpurchasedItemsCount() {
        return unpurchasedItemsCount;
    }

    public void loadDashboardData() {
        recipeRepository.getUserRecipes(recipesLiveData);
        mealPlanRepository.getUserMealPlans(mealPlansLiveData);
        groceryRepository.getUnpurchasedItemsCount(unpurchasedItemsCount);
    }
}