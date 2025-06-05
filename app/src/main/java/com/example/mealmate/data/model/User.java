package com.example.mealmate.data.model;

import com.google.firebase.Timestamp;

/**
 * User data model representing a registered user in the MealMate application.
 * This POJO is designed to work with Firebase Firestore for data persistence.
 */
public class User {
    private String userId;
    private String email;
    private String displayName;
    private String photoUrl;
    private Timestamp createdAt;

    /**
     * Empty constructor required for Firebase Firestore deserialization.
     */
    public User() {
    }

    /**
     * Constructor with all fields.
     *
     * @param userId      The unique identifier for the user (typically Firebase
     *                    Auth UID)
     * @param email       The user's email address
     * @param displayName The user's display name (optional)
     * @param photoUrl    URL to the user's profile photo (optional)
     * @param createdAt   Timestamp when the user account was created
     */
    public User(String userId, String email, String displayName, String photoUrl, Timestamp createdAt) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId The user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the user's email address.
     *
     * @return The email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email The email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's display name.
     *
     * @return The display name (may be null)
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the user's display name.
     *
     * @param displayName The display name to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the URL to the user's profile photo.
     *
     * @return The photo URL (may be null)
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Sets the URL to the user's profile photo.
     *
     * @param photoUrl The photo URL to set
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /**
     * Gets the timestamp when the user account was created.
     *
     * @return The creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the user account was created.
     *
     * @param createdAt The creation timestamp to set
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}