package com.example.mealmate.data.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Map;
import java.util.List;

/**
 * MealPlan data model representing a weekly meal plan in the MealMate
 * application.
 * This POJO is designed to work with Firebase Firestore for data persistence.
 */
public class MealPlan implements Serializable {
    private String planId;
    private String userId;
    private String name;
    private Timestamp weekStartDate;
    private Map<String, List<String>> days;

    /**
     * Empty constructor required for Firebase Firestore deserialization.
     */
    public MealPlan() {
    }

    /**
     * Constructor with all fields.
     *
     * @param planId        The unique identifier for the meal plan (Firestore
     *                      document ID)
     * @param userId        The ID of the user who created this meal plan
     * @param name          The name of the meal plan (e.g., "Week of June 5th")
     * @param weekStartDate The start date of the week for this meal plan
     * @param days          Map with day names as keys (e.g., "Monday", "Tuesday")
     *                      and
     *                      lists of recipe IDs as values for meals planned on each
     *                      day
     */
    public MealPlan(String planId, String userId, String name, Timestamp weekStartDate,
            Map<String, List<String>> days) {
        this.planId = planId;
        this.userId = userId;
        this.name = name;
        this.weekStartDate = weekStartDate;
        this.days = days;
    }

    /**
     * Constructor with required fields only.
     *
     * @param userId        The ID of the user who created this meal plan
     * @param name          The name of the meal plan
     * @param weekStartDate The start date of the week for this meal plan
     */
    public MealPlan(String userId, String name, Timestamp weekStartDate) {
        this.userId = userId;
        this.name = name;
        this.weekStartDate = weekStartDate;
    }

    /**
     * Gets the meal plan ID.
     *
     * @return The meal plan ID
     */
    public String getPlanId() {
        return planId;
    }

    /**
     * Sets the meal plan ID.
     *
     * @param planId The meal plan ID to set
     */
    public void setPlanId(String planId) {
        this.planId = planId;
    }

    /**
     * Gets the user ID who created this meal plan.
     *
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID who created this meal plan.
     *
     * @param userId The user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the meal plan name.
     *
     * @return The meal plan name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the meal plan name.
     *
     * @param name The meal plan name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the week start date.
     *
     * @return The week start date
     */
    public Timestamp getWeekStartDate() {
        return weekStartDate;
    }

    /**
     * Sets the week start date.
     *
     * @param weekStartDate The week start date to set
     */
    public void setWeekStartDate(Timestamp weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    /**
     * Gets the days map containing meal assignments.
     *
     * @return Map with day names as keys and lists of recipe IDs as values
     */
    public Map<String, List<String>> getDays() {
        return days;
    }

    /**
     * Sets the days map containing meal assignments.
     *
     * @param days Map with day names as keys and lists of recipe IDs as values
     */
    public void setDays(Map<String, List<String>> days) {
        this.days = days;
    }
}