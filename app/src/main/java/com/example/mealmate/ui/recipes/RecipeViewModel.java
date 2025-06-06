package com.example.mealmate.ui.recipes;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.Recipe;
import com.example.mealmate.data.repository.RecipeRepository;

import java.util.List;

/**
 * ViewModel for managing recipe-related UI state and coordinating with
 * RecipeRepository.
 */
public class RecipeViewModel extends ViewModel {

    private final RecipeRepository recipeRepository;

    // LiveData for recipe operations
    private final MutableLiveData<AuthResource<Recipe>> saveRecipeResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<List<Recipe>>> recipesLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Recipe>> recipeDetailLiveData = new MutableLiveData<>();
    private final MutableLiveData<AuthResource<Void>> deleteRecipeResult = new MutableLiveData<>();

    // Image URI for add recipe
    private Uri selectedImageUri;

    public RecipeViewModel() {
        this.recipeRepository = new RecipeRepository();
    }

    /**
     * Saves a recipe with optional image.
     *
     * @param recipe The recipe to save
     */
    public void saveRecipe(Recipe recipe) {
        recipeRepository.saveRecipe(recipe, selectedImageUri, saveRecipeResult);
    }

    /**
     * Fetches all recipes for the current user.
     */
    public void loadUserRecipes() {
        recipeRepository.getUserRecipes(recipesLiveData);
    }

    /**
     * Fetches a specific recipe by ID.
     *
     * @param recipeId The ID of the recipe to fetch
     */
    public void loadRecipeById(String recipeId) {
        recipeRepository.getRecipeById(recipeId, recipeDetailLiveData);
    }

    /**
     * Deletes a recipe.
     *
     * @param recipe The recipe to delete
     */
    public void deleteRecipe(Recipe recipe) {
        recipeRepository.deleteRecipe(recipe, deleteRecipeResult);
    }

    /**
     * Sets the selected image URI for recipe creation.
     *
     * @param imageUri The selected image URI
     */
    public void setSelectedImageUri(Uri imageUri) {
        this.selectedImageUri = imageUri;
    }

    /**
     * Gets the currently selected image URI.
     *
     * @return The selected image URI
     */
    public Uri getSelectedImageUri() {
        return selectedImageUri;
    }

    /**
     * Clears the selected image URI.
     */
    public void clearSelectedImageUri() {
        this.selectedImageUri = null;
    }

    // Getters for LiveData
    public LiveData<AuthResource<Recipe>> getSaveRecipeResult() {
        return saveRecipeResult;
    }

    public LiveData<AuthResource<List<Recipe>>> getRecipesLiveData() {
        return recipesLiveData;
    }

    public LiveData<AuthResource<Recipe>> getRecipeDetailLiveData() {
        return recipeDetailLiveData;
    }

    public LiveData<AuthResource<Void>> getDeleteRecipeResult() {
        return deleteRecipeResult;
    }

    /**
     * Clears the save recipe result.
     */
    public void clearSaveRecipeResult() {
        saveRecipeResult.setValue(null);
    }

    /**
     * Clears the delete recipe result.
     */
    public void clearDeleteRecipeResult() {
        deleteRecipeResult.setValue(null);
    }

    /**
     * Clears the recipe detail data.
     */
    public void clearRecipeDetail() {
        recipeDetailLiveData.setValue(null);
    }
}