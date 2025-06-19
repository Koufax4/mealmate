package com.example.mealmate.data.repository;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Repository class for handling recipe-related operations with Firebase
 * Firestore and Storage.
 */
public class RecipeRepository {

    private static final String TAG = "RecipeRepository";
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseAuth firebaseAuth;

    public RecipeRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Saves a recipe to Firestore. If the recipe has an image URI, uploads it
     * first.
     *
     * @param recipe         The recipe to save
     * @param imageUri       The local image URI (optional)
     * @param resultLiveData LiveData to notify about the operation result
     */
    public void saveRecipe(Recipe recipe, Uri imageUri, MutableLiveData<AuthResource<Recipe>> resultLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            resultLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        resultLiveData.setValue(AuthResource.loading(null));

        // Set user ID and generate recipe ID if not set
        recipe.setUserId(currentUser.getUid());
        if (recipe.getRecipeId() == null) {
            recipe.setRecipeId(UUID.randomUUID().toString());
        }

        // Set creation timestamp
        recipe.setCreatedAt(com.google.firebase.Timestamp.now());

        if (imageUri != null) {
            // Upload image first, then save recipe
            uploadImage(imageUri, recipe, resultLiveData);
        } else {
            // Save recipe directly
            saveRecipeToFirestore(recipe, resultLiveData);
        }
    }

    /**
     * Updates an existing recipe in Firestore. If a new image URI is provided,
     * uploads it first.
     *
     * @param recipe         The recipe to update
     * @param imageUri       The new local image URI (optional, null to keep
     *                       existing image)
     * @param resultLiveData LiveData to notify about the operation result
     */
    public void updateRecipe(Recipe recipe, Uri imageUri, MutableLiveData<AuthResource<Recipe>> resultLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            resultLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        if (recipe.getRecipeId() == null) {
            resultLiveData.setValue(AuthResource.error("Recipe ID is required for updates", null));
            return;
        }

        resultLiveData.setValue(AuthResource.loading(null));

        // Ensure user ID is set
        recipe.setUserId(currentUser.getUid());

        if (imageUri != null) {
            // Upload new image first, then update recipe
            uploadImage(imageUri, recipe, resultLiveData);
        } else {
            // Update recipe directly (keep existing image)
            saveRecipeToFirestore(recipe, resultLiveData);
        }
    }

    /**
     * Uploads an image to Firebase Storage and then saves the recipe.
     */
    private void uploadImage(Uri imageUri, Recipe recipe, MutableLiveData<AuthResource<Recipe>> resultLiveData) {
        StorageReference imageRef = storage.getReference()
                .child("recipe_images")
                .child(recipe.getUserId())
                .child(recipe.getRecipeId() + "_" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        recipe.setImageUrl(downloadUri.toString());
                        saveRecipeToFirestore(recipe, resultLiveData);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get download URL", e);
                        resultLiveData.setValue(AuthResource.error("Failed to upload image: " + e.getMessage(), null));
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload image", e);
                    resultLiveData.setValue(AuthResource.error("Failed to upload image: " + e.getMessage(), null));
                });
    }

    /**
     * Saves recipe data to Firestore.
     */
    private void saveRecipeToFirestore(Recipe recipe, MutableLiveData<AuthResource<Recipe>> resultLiveData) {
        firestore.collection("users")
                .document(recipe.getUserId())
                .collection("recipes")
                .document(recipe.getRecipeId())
                .set(recipe)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recipe saved successfully: " + recipe.getRecipeId());
                    resultLiveData.setValue(AuthResource.success(recipe));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save recipe", e);
                    resultLiveData.setValue(AuthResource.error("Failed to save recipe: " + e.getMessage(), null));
                });
    }

    /**
     * Fetches all recipes for the current user.
     *
     * @param recipesLiveData LiveData to notify about the fetched recipes
     */
    public void getUserRecipes(MutableLiveData<AuthResource<List<Recipe>>> recipesLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            recipesLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        recipesLiveData.setValue(AuthResource.loading(null));

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("recipes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipes.add(recipe);
                    }
                    Log.d(TAG, "Fetched " + recipes.size() + " recipes");
                    recipesLiveData.setValue(AuthResource.success(recipes));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch recipes", e);
                    recipesLiveData.setValue(AuthResource.error("Failed to fetch recipes: " + e.getMessage(), null));
                });
    }

    /**
     * Fetches a specific recipe by ID.
     *
     * @param recipeId       The ID of the recipe to fetch
     * @param recipeLiveData LiveData to notify about the fetched recipe
     */
    public void getRecipeById(String recipeId, MutableLiveData<AuthResource<Recipe>> recipeLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            recipeLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        recipeLiveData.setValue(AuthResource.loading(null));

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("recipes")
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Recipe recipe = documentSnapshot.toObject(Recipe.class);
                        recipeLiveData.setValue(AuthResource.success(recipe));
                    } else {
                        recipeLiveData.setValue(AuthResource.error("Recipe not found", null));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch recipe", e);
                    recipeLiveData.setValue(AuthResource.error("Failed to fetch recipe: " + e.getMessage(), null));
                });
    }

    /**
     * Deletes a recipe from Firestore and its associated image from Storage.
     *
     * @param recipe         The recipe to delete
     * @param resultLiveData LiveData to notify about the operation result
     */
    public void deleteRecipe(Recipe recipe, MutableLiveData<AuthResource<Void>> resultLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            resultLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        resultLiveData.setValue(AuthResource.loading(null));

        // Delete from Firestore first
        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("recipes")
                .document(recipe.getRecipeId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recipe deleted from Firestore: " + recipe.getRecipeId());

                    // Delete image from Storage if it exists
                    if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                        deleteImageFromStorage(recipe.getImageUrl(), resultLiveData);
                    } else {
                        resultLiveData.setValue(AuthResource.success(null));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete recipe", e);
                    resultLiveData.setValue(AuthResource.error("Failed to delete recipe: " + e.getMessage(), null));
                });
    }

    /**
     * Deletes an image from Firebase Storage.
     */
    private void deleteImageFromStorage(String imageUrl, MutableLiveData<AuthResource<Void>> resultLiveData) {
        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Recipe image deleted from Storage");
                        resultLiveData.setValue(AuthResource.success(null));
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Failed to delete image from Storage (recipe still deleted from Firestore)", e);
                        // Still consider it success since the recipe is deleted from Firestore
                        resultLiveData.setValue(AuthResource.success(null));
                    });
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid image URL, skipping image deletion", e);
            resultLiveData.setValue(AuthResource.success(null));
        }
    }
}