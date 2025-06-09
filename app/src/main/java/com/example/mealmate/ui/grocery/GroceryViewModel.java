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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
     * This method consolidates duplicate ingredients and saves the updated list.
     */
    public void addIngredientsToGroceryList(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            addIngredientsResult.setValue(AuthResource.success(null));
            return;
        }

        addIngredientsResult.setValue(AuthResource.loading(null));

        // First, get the current grocery list
        MutableLiveData<AuthResource<List<GroceryItem>>> currentListLiveData = new MutableLiveData<>();
        groceryRepository.getGroceryList(MAIN_GROCERY_LIST_ID, currentListLiveData);

        currentListLiveData.observeForever(resource -> {
            if (resource.status == AuthResource.Status.SUCCESS) {
                List<GroceryItem> currentItems = resource.data != null ? resource.data : new ArrayList<>();

                // Create a map for easy consolidation
                Map<String, GroceryItem> consolidatedItems = new HashMap<>();

                // Add existing items to the map
                for (GroceryItem item : currentItems) {
                    consolidatedItems.put(item.getName().toLowerCase().trim(), item);
                }

                // Add new ingredients, consolidating duplicates
                for (Ingredient ingredient : ingredients) {
                    addIngredientToConsolidatedList(ingredient, consolidatedItems);
                }

                // Save the updated list
                List<GroceryItem> finalList = new ArrayList<>(consolidatedItems.values());
                groceryRepository.saveGroceryList(MAIN_GROCERY_LIST_ID, finalList, addIngredientsResult);

                // Update the main grocery list LiveData
                groceryListLiveData.setValue(AuthResource.success(finalList));

            } else if (resource.status == AuthResource.Status.ERROR) {
                addIngredientsResult.setValue(AuthResource.error(resource.message, null));
            }
        });
    }

    /**
     * Adds an ingredient to the consolidated grocery list, combining quantities if
     * the same item exists.
     */
    private void addIngredientToConsolidatedList(Ingredient ingredient, Map<String, GroceryItem> consolidatedItems) {
        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
            return;
        }

        String normalizedName = ingredient.getName().toLowerCase().trim();

        if (consolidatedItems.containsKey(normalizedName)) {
            // Item already exists, update quantity and combine units if different
            GroceryItem existingItem = consolidatedItems.get(normalizedName);

            try {
                double existingQuantity = existingItem.getQuantity();
                double newQuantity = Double.parseDouble(ingredient.getQuantity());

                // If units are the same or one is empty, just add quantities
                if (existingItem.getUnit().equals(ingredient.getUnit()) ||
                        existingItem.getUnit().isEmpty() || ingredient.getUnit().isEmpty()) {

                    existingItem.setQuantity(existingQuantity + newQuantity);
                    if (existingItem.getUnit().isEmpty() && !ingredient.getUnit().isEmpty()) {
                        existingItem.setUnit(ingredient.getUnit());
                    }
                } else {
                    // Different units, add to notes field
                    String currentNotes = existingItem.getNotes() != null ? existingItem.getNotes() : "";
                    String additionalNote = " + " + ingredient.getQuantity() + " " + ingredient.getUnit();
                    existingItem.setNotes(currentNotes + additionalNote);
                }
            } catch (NumberFormatException e) {
                // If new quantity isn't parseable as number, add to notes
                String currentNotes = existingItem.getNotes() != null ? existingItem.getNotes() : "";
                String additionalNote = " + " + ingredient.getQuantity() + " " + ingredient.getUnit();
                existingItem.setNotes(currentNotes + additionalNote);
            }
        } else {
            // New item, create GroceryItem
            GroceryItem newItem = new GroceryItem();
            newItem.setItemId(UUID.randomUUID().toString());
            newItem.setName(ingredient.getName());
            try {
                newItem.setQuantity(Double.parseDouble(ingredient.getQuantity()));
            } catch (NumberFormatException e) {
                newItem.setQuantity(1.0); // Default to 1 if quantity is not a number
            }
            newItem.setUnit(ingredient.getUnit());
            newItem.setPurchased(false);
            newItem.setCategory(categorizeIngredient(ingredient.getName()));

            consolidatedItems.put(normalizedName, newItem);
        }
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