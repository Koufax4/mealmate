package com.example.mealmate.data.model;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Recipe data model representing a cooking recipe in the MealMate application.
 * This POJO is designed to work with Firebase Firestore for data persistence.
 */
public class Recipe {
    private String recipeId;
    private String userId;
    private String name;
    private List<Ingredient> ingredients;
    private String instructions;
    private String imageUrl;
    private String sourceUrl;
    private String prepTime;
    private String cookTime;
    private int servings;
    private String category;
    private Timestamp createdAt;

    /**
     * Empty constructor required for Firebase Firestore deserialization.
     */
    public Recipe() {
    }

    /**
     * Constructor with all fields.
     *
     * @param recipeId     The unique identifier for the recipe (Firestore document
     *                     ID)
     * @param userId       The ID of the user who created this recipe
     * @param name         The name of the recipe
     * @param ingredients  List of ingredients required for the recipe
     * @param instructions Step-by-step cooking instructions
     * @param imageUrl     URL to the recipe image (optional)
     * @param sourceUrl    URL to the original recipe source (optional)
     * @param prepTime     Preparation time (optional, e.g., "15 minutes")
     * @param cookTime     Cooking time (optional, e.g., "30 minutes")
     * @param servings     Number of servings the recipe makes (optional)
     * @param category     Recipe category (optional, e.g., "breakfast", "dinner",
     *                     "dessert")
     * @param createdAt    Timestamp when the recipe was created
     */
    public Recipe(String recipeId, String userId, String name, List<Ingredient> ingredients,
            String instructions, String imageUrl, String sourceUrl, String prepTime,
            String cookTime, int servings, String category, Timestamp createdAt) {
        this.recipeId = recipeId;
        this.userId = userId;
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageUrl = imageUrl;
        this.sourceUrl = sourceUrl;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.servings = servings;
        this.category = category;
        this.createdAt = createdAt;
    }

    /**
     * Constructor with required fields only.
     *
     * @param userId       The ID of the user who created this recipe
     * @param name         The name of the recipe
     * @param ingredients  List of ingredients required for the recipe
     * @param instructions Step-by-step cooking instructions
     */
    public Recipe(String userId, String name, List<Ingredient> ingredients, String instructions) {
        this.userId = userId;
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.servings = 0; // Default value for optional int field
    }

    /**
     * Gets the recipe ID.
     *
     * @return The recipe ID
     */
    public String getRecipeId() {
        return recipeId;
    }

    /**
     * Sets the recipe ID.
     *
     * @param recipeId The recipe ID to set
     */
    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    /**
     * Gets the user ID who created this recipe.
     *
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID who created this recipe.
     *
     * @param userId The user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the recipe name.
     *
     * @return The recipe name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the recipe name.
     *
     * @param name The recipe name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of ingredients.
     *
     * @return The ingredients list
     */
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     * Sets the list of ingredients.
     *
     * @param ingredients The ingredients list to set
     */
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Gets the cooking instructions.
     *
     * @return The instructions
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Sets the cooking instructions.
     *
     * @param instructions The instructions to set
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * Gets the image URL.
     *
     * @return The image URL (may be null)
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image URL.
     *
     * @param imageUrl The image URL to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Gets the source URL.
     *
     * @return The source URL (may be null)
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Sets the source URL.
     *
     * @param sourceUrl The source URL to set
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * Gets the preparation time.
     *
     * @return The prep time (may be null)
     */
    public String getPrepTime() {
        return prepTime;
    }

    /**
     * Sets the preparation time.
     *
     * @param prepTime The prep time to set
     */
    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    /**
     * Gets the cooking time.
     *
     * @return The cook time (may be null)
     */
    public String getCookTime() {
        return cookTime;
    }

    /**
     * Sets the cooking time.
     *
     * @param cookTime The cook time to set
     */
    public void setCookTime(String cookTime) {
        this.cookTime = cookTime;
    }

    /**
     * Gets the number of servings.
     *
     * @return The servings count
     */
    public int getServings() {
        return servings;
    }

    /**
     * Sets the number of servings.
     *
     * @param servings The servings count to set
     */
    public void setServings(int servings) {
        this.servings = servings;
    }

    /**
     * Gets the recipe category.
     *
     * @return The category (may be null)
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the recipe category.
     *
     * @param category The category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return The creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt The creation timestamp to set
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}