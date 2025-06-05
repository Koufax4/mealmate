package com.example.mealmate.data.model;

/**
 * StoreLocation data model representing a saved grocery store location in the
 * MealMate application.
 * This POJO is designed to work with Firebase Firestore for data persistence.
 * This class is used for the optional geotagging feature (Phase 7).
 */
public class StoreLocation {
    private String storeId;
    private String userId;
    private String name;
    private String address;
    private double latitude;
    private double longitude;

    /**
     * Empty constructor required for Firebase Firestore deserialization.
     */
    public StoreLocation() {
    }

    /**
     * Constructor with all fields.
     *
     * @param storeId   The unique identifier for the store (Firestore document ID)
     * @param userId    The ID of the user who saved this store
     * @param name      The name of the store
     * @param address   The address of the store (optional)
     * @param latitude  The latitude coordinate of the store location
     * @param longitude The longitude coordinate of the store location
     */
    public StoreLocation(String storeId, String userId, String name, String address,
            double latitude, double longitude) {
        this.storeId = storeId;
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructor with required fields only.
     *
     * @param userId    The ID of the user who saved this store
     * @param name      The name of the store
     * @param latitude  The latitude coordinate of the store location
     * @param longitude The longitude coordinate of the store location
     */
    public StoreLocation(String userId, String name, double latitude, double longitude) {
        this.userId = userId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Gets the store ID.
     *
     * @return The store ID
     */
    public String getStoreId() {
        return storeId;
    }

    /**
     * Sets the store ID.
     *
     * @param storeId The store ID to set
     */
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    /**
     * Gets the user ID who saved this store.
     *
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID who saved this store.
     *
     * @param userId The user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the store name.
     *
     * @return The store name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the store name.
     *
     * @param name The store name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the store address.
     *
     * @return The store address (may be null)
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the store address.
     *
     * @param address The store address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the latitude coordinate.
     *
     * @return The latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude coordinate.
     *
     * @param latitude The latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the longitude coordinate.
     *
     * @return The longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude coordinate.
     *
     * @param longitude The longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}