package com.example.mealmate.data.model;

/**
 * GroceryItem data model representing an item in a grocery shopping list.
 * This POJO is designed to work with Firebase Firestore for data persistence.
 */
public class GroceryItem {
    private String itemId;
    private String name;
    private double quantity;
    private String unit;
    private String category;
    private boolean purchased;
    private String notes;
    private String recipeId;

    /**
     * Empty constructor required for Firebase Firestore deserialization.
     */
    public GroceryItem() {
    }

    /**
     * Constructor with all fields.
     *
     * @param itemId    The unique identifier for the grocery item
     * @param name      The name of the grocery item
     * @param quantity  The quantity needed
     * @param unit      The unit of measurement (e.g., "pieces", "lbs", "cups")
     * @param category  The category of the item (optional, e.g., "dairy",
     *                  "produce", "meat")
     * @param purchased Whether the item has been purchased
     * @param notes     Additional notes about the item (optional)
     * @param recipeId  The recipe ID this item came from (optional, for
     *                  traceability)
     */
    public GroceryItem(String itemId, String name, double quantity, String unit,
            String category, boolean purchased, String notes, String recipeId) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.purchased = purchased;
        this.notes = notes;
        this.recipeId = recipeId;
    }

    /**
     * Constructor with required fields only.
     *
     * @param name     The name of the grocery item
     * @param quantity The quantity needed
     * @param unit     The unit of measurement
     */
    public GroceryItem(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.purchased = false; // Default to not purchased
    }

    /**
     * Gets the item ID.
     *
     * @return The item ID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Sets the item ID.
     *
     * @param itemId The item ID to set
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets the item name.
     *
     * @return The item name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the item name.
     *
     * @param name The item name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the quantity needed.
     *
     * @return The quantity
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity needed.
     *
     * @param quantity The quantity to set
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets the unit of measurement.
     *
     * @return The unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the unit of measurement.
     *
     * @param unit The unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Gets the item category.
     *
     * @return The category (may be null)
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the item category.
     *
     * @param category The category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the purchased status.
     *
     * @return True if the item has been purchased, false otherwise
     */
    public boolean isPurchased() {
        return purchased;
    }

    /**
     * Sets the purchased status.
     *
     * @param purchased The purchased status to set
     */
    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    /**
     * Gets the additional notes about the item.
     *
     * @return The notes (may be null)
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the additional notes about the item.
     *
     * @param notes The notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Gets the recipe ID this item came from.
     *
     * @return The recipe ID (may be null)
     */
    public String getRecipeId() {
        return recipeId;
    }

    /**
     * Sets the recipe ID this item came from.
     *
     * @param recipeId The recipe ID to set
     */
    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }
}