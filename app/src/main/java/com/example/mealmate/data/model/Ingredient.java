package com.example.mealmate.data.model;

/**
 * Ingredient data model representing a single ingredient used in recipes.
 * This class is designed to be embedded within Recipe objects.
 */
public class Ingredient {
    private String name;
    private double quantity;
    private String unit;
    private String category;

    /**
     * Empty constructor required for Firebase Firestore deserialization.
     */
    public Ingredient() {
    }

    /**
     * Constructor with all fields.
     *
     * @param name     The name of the ingredient
     * @param quantity The quantity needed
     * @param unit     The unit of measurement (e.g., "cups", "grams", "pieces")
     * @param category The category of the ingredient (optional, e.g., "dairy",
     *                 "meat", "vegetables")
     */
    public Ingredient(String name, double quantity, String unit, String category) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
    }

    /**
     * Constructor with required fields only.
     *
     * @param name     The name of the ingredient
     * @param quantity The quantity needed
     * @param unit     The unit of measurement
     */
    public Ingredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.category = null;
    }

    /**
     * Gets the ingredient name.
     *
     * @return The ingredient name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the ingredient name.
     *
     * @param name The ingredient name to set
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
     * Gets the ingredient category.
     *
     * @return The category (may be null)
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the ingredient category.
     *
     * @param category The category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
}