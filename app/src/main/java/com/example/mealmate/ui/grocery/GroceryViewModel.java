// app/src/main/java/com/example/mealmate/ui/grocery/GroceryViewModel.java
package com.example.mealmate.ui.grocery;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.GroceryItem;
import com.example.mealmate.data.model.Ingredient;
import com.example.mealmate.data.repository.GroceryRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ViewModel for managing grocery list data and operations.
 */
public class GroceryViewModel extends ViewModel {

    private static final String TAG = "GroceryViewModel";
    private static final String MAIN_GROCERY_LIST_ID = "main_list";

    private final GroceryRepository groceryRepository;

    // LiveData for grocery list
    private final MutableLiveData<AuthResource<List<GroceryItem>>> groceryListLiveData = new MutableLiveData<>();

    // LiveData for operation results
    private final MutableLiveData<AuthResource<Void>> updateItemResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Void>> deleteItemResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Void>> addIngredientsResult = new MutableLiveData<>();

    public GroceryViewModel() {
        this.groceryRepository = new GroceryRepository();
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
     * Gets the LiveData for add ingredients result.
     */
    public LiveData<AuthResource<Void>> getAddIngredientsResult() {
        return addIngredientsResult;
    }

    /**
     * Loads the main grocery list.
     */
    public void loadGroceryList() {
        groceryRepository.getGroceryList(MAIN_GROCERY_LIST_ID, groceryListLiveData);
    }

    /**
     * Adds ingredients from a recipe to the main grocery list.
     * This method fetches the current list, consolidates new ingredients, and saves
     * the updated list.
     */
    public void addIngredientsToGroceryList(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            addIngredientsResult.setValue(AuthResource.error("No ingredients to add", null));
            return;
        }

        addIngredientsResult.setValue(AuthResource.loading(null));

        // Create a temporary LiveData to get the current list just once.
        MutableLiveData<AuthResource<List<GroceryItem>>> currentListLiveData = new MutableLiveData<>();
        groceryRepository.getGroceryList(MAIN_GROCERY_LIST_ID, currentListLiveData);

        currentListLiveData.observeForever(resource -> {
            if (resource != null) {
                // We only want to process this once, so remove the observer immediately.
                currentListLiveData.removeObserver(resource1 -> {
                });

                if (resource.status == AuthResource.Status.SUCCESS) {
                    List<GroceryItem> currentItems = resource.data != null ? resource.data : new ArrayList<>();
                    processAndSaveGroceryList(currentItems, ingredients);
                } else if (resource.status == AuthResource.Status.ERROR) {
                    // Handle case where the initial list fetch fails
                    addIngredientsResult.setValue(AuthResource.error(resource.message, null));
                }
            }
        });
    }

    private void processAndSaveGroceryList(List<GroceryItem> currentItems, List<Ingredient> newIngredients) {
        // Use a map for efficient consolidation based on a normalized key.
        Map<String, GroceryItem> consolidatedItems = new HashMap<>();
        for (GroceryItem item : currentItems) {
            String key = (item.getName().toLowerCase().trim() + "|"
                    + (item.getUnit() != null ? item.getUnit().toLowerCase().trim() : ""));
            consolidatedItems.put(key, item);
        }

        for (Ingredient ingredient : newIngredients) {
            if (ingredient.getName() == null || ingredient.getName().trim().isEmpty())
                continue;

            String key = (ingredient.getName().toLowerCase().trim() + "|"
                    + (ingredient.getUnit() != null ? ingredient.getUnit().toLowerCase().trim() : ""));

            if (consolidatedItems.containsKey(key)) {
                // Item exists, just add the quantity.
                GroceryItem existingItem = consolidatedItems.get(key);
                if (existingItem != null) {
                    existingItem.setQuantity(existingItem.getQuantity() + ingredient.getQuantity());
                }
            } else {
                // New item, add to map.
                GroceryItem newItem = new GroceryItem();
                newItem.setItemId(UUID.randomUUID().toString());
                newItem.setName(ingredient.getName());
                newItem.setQuantity(ingredient.getQuantity());
                newItem.setUnit(ingredient.getUnit());
                newItem.setPurchased(false);
                newItem.setCategory(categorizeIngredient(ingredient.getName()));
                consolidatedItems.put(key, newItem);
            }
        }

        // Save the updated list back to Firestore.
        groceryRepository.saveGroceryList(MAIN_GROCERY_LIST_ID, new ArrayList<>(consolidatedItems.values()),
                addIngredientsResult);
    }

    /**
     * Simple categorization of ingredients (can be enhanced later).
     */
    private String categorizeIngredient(String ingredientName) {
        String name = ingredientName.toLowerCase();

        if (name.contains("milk") || name.contains("cheese") || name.contains("yogurt") ||
                name.contains("butter") || name.contains("cream")) {
            return "Dairy";
        } else if (name.contains("apple") || name.contains("banana") || name.contains("orange") ||
                name.contains("berry") || name.contains("fruit")) {
            return "Fruits";
        } else if (name.contains("potato") || name.contains("carrot") || name.contains("onion") ||
                name.contains("tomato") || name.contains("lettuce") || name.contains("vegetable")) {
            return "Vegetables";
        } else if (name.contains("chicken") || name.contains("beef") || name.contains("pork") ||
                name.contains("fish") || name.contains("meat")) {
            return "Meat & Seafood";
        } else if (name.contains("bread") || name.contains("pasta") || name.contains("rice") ||
                name.contains("flour") || name.contains("cereal")) {
            return "Grains & Bakery";
        } else {
            return "Other";
        }
    }

    /**
     * Updates a grocery item's state.
     */
    public void updateItem(GroceryItem item) {
        if (item != null) {
            groceryRepository.updateGroceryItem(MAIN_GROCERY_LIST_ID, item, updateItemResult);
        }
    }

    /**
     * Deletes a grocery item.
     */
    public void deleteItem(GroceryItem item) {
        if (item != null) {
            groceryRepository.deleteGroceryItem(MAIN_GROCERY_LIST_ID, item, deleteItemResult);
        }
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
     * Clears the add ingredients result.
     */
    public void clearAddIngredientsResult() {
        addIngredientsResult.setValue(null);
    }
}