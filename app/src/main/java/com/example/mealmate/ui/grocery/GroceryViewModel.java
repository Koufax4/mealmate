package com.example.mealmate.ui.grocery;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.GroceryItem;
import com.example.mealmate.data.model.Ingredient;
import com.example.mealmate.data.model.MealPlan;
import com.example.mealmate.data.model.Recipe;
import com.example.mealmate.data.repository.GroceryRepository;
import com.example.mealmate.data.repository.MealPlanRepository;
import com.example.mealmate.data.repository.RecipeRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ViewModel for managing grocery list data and operations.
 */
public class GroceryViewModel extends ViewModel {

    private static final String TAG = "GroceryViewModel";

    private final GroceryRepository groceryRepository;
    private final MealPlanRepository mealPlanRepository; // Added for loading the meal plan
    private final RecipeRepository recipeRepository;

    // LiveData for grocery list
    private final MutableLiveData<AuthResource<List<GroceryItem>>> groceryListLiveData = new MutableLiveData<>();

    // LiveData for operation results
    private final MutableLiveData<AuthResource<Void>> updateItemResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Void>> deleteItemResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Void>> generateListResult = new MutableLiveData<>();

    // Current grocery list ID
    private String currentGroceryListId;

    public GroceryViewModel() {
        this.groceryRepository = new GroceryRepository();
        this.mealPlanRepository = new MealPlanRepository(); // Added
        this.recipeRepository = new RecipeRepository();
    }

    /**
     * Gets the LiveData for the grocery list.
     */
    public LiveData<AuthResource<List<GroceryItem>>> getGroceryListLiveData() {
        return groceryListLiveData;
    }

    /**
     * Gets the LiveData for update item result.
     */
    public LiveData<AuthResource<Void>> getUpdateItemResult() {
        return updateItemResult;
    }

    /**
     * Gets the LiveData for delete item result.
     */
    public LiveData<AuthResource<Void>> getDeleteItemResult() {
        return deleteItemResult;
    }

    /**
     * Gets the LiveData for generate list result.
     */
    public LiveData<AuthResource<Void>> getGenerateListResult() {
        return generateListResult;
    }

    /**
     * Loads an existing grocery list by ID.
     */
    public void loadGroceryList() {
        if (currentGroceryListId != null) {
            groceryRepository.getGroceryList(currentGroceryListId, groceryListLiveData);
        } else {
            groceryListLiveData.setValue(AuthResource.error("Grocery list ID is not set.", null));
        }
    }

    /**
     * Kicks off the process of generating a grocery list by first loading the
     * meal plan.
     */
    public void generateGroceryListFromPlanId(String mealPlanId) {
        // A grocery list is uniquely tied to a meal plan
        this.currentGroceryListId = "grocery_" + mealPlanId;

        // First, check if a list already exists. If so, load it.
        groceryRepository.getGroceryList(currentGroceryListId, new MutableLiveData<AuthResource<List<GroceryItem>>>() {
            @Override
            public void setValue(AuthResource<List<GroceryItem>> value) {
                super.setValue(value);
                if (value.status == AuthResource.Status.SUCCESS && value.data != null && !value.data.isEmpty()) {
                    // List already exists, just display it.
                    groceryListLiveData.setValue(value);
                } else {
                    // List doesn't exist, so we generate it.
                    loadMealPlanAndGenerateList(mealPlanId);
                }
            }
        });
    }

    /**
     * Loads the MealPlan object from Firestore before generating the list.
     */
    private void loadMealPlanAndGenerateList(String mealPlanId) {
        generateListResult.setValue(AuthResource.loading(null));
        mealPlanRepository.getMealPlanById(mealPlanId, new MutableLiveData<AuthResource<MealPlan>>() {
            @Override
            public void setValue(AuthResource<MealPlan> resource) {
                if (resource.status == AuthResource.Status.SUCCESS && resource.data != null) {
                    generateGroceryListFromPlan(resource.data);
                } else if (resource.status == AuthResource.Status.ERROR) {
                    generateListResult.setValue(AuthResource.error(resource.message, null));
                }
            }
        });
    }

    /**
     * Generates a consolidated grocery list from a meal plan object.
     */
    private void generateGroceryListFromPlan(@NonNull MealPlan mealPlan) {
        Log.d(TAG, "Starting grocery list generation for meal plan: " + mealPlan.getName());

        Set<String> uniqueRecipeIds = new HashSet<>();
        if (mealPlan.getDays() != null) {
            for (List<String> dayRecipes : mealPlan.getDays().values()) {
                if (dayRecipes != null) {
                    uniqueRecipeIds.addAll(dayRecipes);
                }
            }
        }

        if (uniqueRecipeIds.isEmpty()) {
            Log.w(TAG, "No recipes found in meal plan");
            groceryListLiveData.setValue(AuthResource.success(new ArrayList<>()));
            generateListResult.setValue(AuthResource.success(null));
            return;
        }

        fetchAllRecipes(new ArrayList<>(uniqueRecipeIds));
    }

    private void fetchAllRecipes(List<String> recipeIds) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("recipes")
                .whereIn("recipeId", recipeIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, GroceryItem> consolidatedItems = new HashMap<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        if (recipe.getIngredients() != null) {
                            for (Ingredient ingredient : recipe.getIngredients()) {
                                addIngredientToConsolidatedList(ingredient, recipe.getRecipeId(), consolidatedItems);
                            }
                        }
                    }
                    List<GroceryItem> finalList = new ArrayList<>(consolidatedItems.values());
                    groceryRepository.saveGroceryList(currentGroceryListId, finalList, generateListResult);
                    groceryListLiveData.setValue(AuthResource.success(finalList));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch recipes for grocery list", e);
                    generateListResult.setValue(AuthResource.error(e.getMessage(), null));
                });
    }


    /**
     * Adds an ingredient to the consolidated grocery list, combining quantities if
     * the same item exists.
     */
    private void addIngredientToConsolidatedList(Ingredient ingredient, String recipeId,
                                                 Map<String, GroceryItem> consolidatedItems) {

        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
            return;
        }

        String consolidationKey = ingredient.getName().toLowerCase().trim() + "|" +
                (ingredient.getUnit() != null ? ingredient.getUnit().toLowerCase().trim() : "");

        if (consolidatedItems.containsKey(consolidationKey)) {
            GroceryItem existingItem = consolidatedItems.get(consolidationKey);
            if(existingItem != null) {
                double newQuantity = existingItem.getQuantity() + ingredient.getQuantity();
                existingItem.setQuantity(newQuantity);
            }
        } else {
            GroceryItem newItem = new GroceryItem();
            newItem.setItemId(UUID.randomUUID().toString());
            newItem.setName(ingredient.getName());
            newItem.setQuantity(ingredient.getQuantity());
            newItem.setUnit(ingredient.getUnit());
            newItem.setCategory(ingredient.getCategory());
            newItem.setPurchased(false);
            newItem.setRecipeId(recipeId);
            consolidatedItems.put(consolidationKey, newItem);
        }
    }

    /**
     * Updates a grocery item (e.g., purchased status).
     */
    public void updateItem(GroceryItem item) {
        if (currentGroceryListId == null) {
            updateItemResult.setValue(AuthResource.error("No grocery list loaded", null));
            return;
        }
        groceryRepository.updateGroceryItem(currentGroceryListId, item, updateItemResult);
    }

    /**
     * Deletes a grocery item from the list.
     */
    public void deleteItem(GroceryItem item) {
        if (currentGroceryListId == null) {
            deleteItemResult.setValue(AuthResource.error("No grocery list loaded", null));
            return;
        }
        groceryRepository.deleteGroceryItem(currentGroceryListId, item, deleteItemResult);
    }

    /**
     * Clears the update item result.
     */
    public void clearUpdateItemResult() {
        updateItemResult.setValue(null);
    }

    /**
     * Clears the delete item result.
     */
    public void clearDeleteItemResult() {
        deleteItemResult.setValue(null);
    }

    /**
     * Clears the generate list result.
     */
    public void clearGenerateListResult() {
        generateListResult.setValue(null);
    }
}