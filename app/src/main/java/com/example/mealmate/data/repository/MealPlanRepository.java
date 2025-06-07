package com.example.mealmate.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.MealPlan;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Repository class for handling meal plan-related operations with Firebase
 * Firestore.
 */
public class MealPlanRepository {

    private static final String TAG = "MealPlanRepository";
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    public MealPlanRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Saves a meal plan to Firestore.
     *
     * @param mealPlan       The meal plan to save
     * @param resultLiveData LiveData to notify about the operation result
     */
    public void saveMealPlan(MealPlan mealPlan, MutableLiveData<AuthResource<MealPlan>> resultLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            resultLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        resultLiveData.setValue(AuthResource.loading(null));

        // Set user ID and generate plan ID if not set
        mealPlan.setUserId(currentUser.getUid());
        if (mealPlan.getPlanId() == null) {
            mealPlan.setPlanId(UUID.randomUUID().toString());
        }

        firestore.collection("users")
                .document(mealPlan.getUserId())
                .collection("mealPlans")
                .document(mealPlan.getPlanId())
                .set(mealPlan)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Meal plan saved successfully: " + mealPlan.getPlanId());
                    resultLiveData.setValue(AuthResource.success(mealPlan));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save meal plan", e);
                    resultLiveData.setValue(AuthResource.error("Failed to save meal plan: " + e.getMessage(), null));
                });
    }

    /**
     * Fetches all meal plans for the current user.
     *
     * @param mealPlansLiveData LiveData to notify about the fetched meal plans
     */
    public void getUserMealPlans(MutableLiveData<AuthResource<List<MealPlan>>> mealPlansLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            mealPlansLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        mealPlansLiveData.setValue(AuthResource.loading(null));

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("mealPlans")
                .orderBy("weekStartDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MealPlan> mealPlans = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        MealPlan mealPlan = document.toObject(MealPlan.class);
                        mealPlans.add(mealPlan);
                    }
                    Log.d(TAG, "Fetched " + mealPlans.size() + " meal plans");
                    mealPlansLiveData.setValue(AuthResource.success(mealPlans));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch meal plans", e);
                    mealPlansLiveData
                            .setValue(AuthResource.error("Failed to fetch meal plans: " + e.getMessage(), null));
                });
    }

    /**
     * Fetches a specific meal plan by ID.
     *
     * @param planId           The ID of the meal plan to fetch
     * @param mealPlanLiveData LiveData to notify about the fetched meal plan
     */
    public void getMealPlanById(String planId, MutableLiveData<AuthResource<MealPlan>> mealPlanLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            mealPlanLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        mealPlanLiveData.setValue(AuthResource.loading(null));

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("mealPlans")
                .document(planId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        MealPlan mealPlan = documentSnapshot.toObject(MealPlan.class);
                        mealPlanLiveData.setValue(AuthResource.success(mealPlan));
                    } else {
                        mealPlanLiveData.setValue(AuthResource.error("Meal plan not found", null));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch meal plan", e);
                    mealPlanLiveData.setValue(AuthResource.error("Failed to fetch meal plan: " + e.getMessage(), null));
                });
    }

    /**
     * Fetches a specific meal plan for a given week's start date.
     *
     * @param weekStartDate    The timestamp of the Monday of the week to fetch.
     * @param mealPlanLiveData LiveData to notify about the fetched meal plan.
     */
    public void getMealPlanForWeek(Timestamp weekStartDate, MutableLiveData<AuthResource<MealPlan>> mealPlanLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            mealPlanLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        mealPlanLiveData.setValue(AuthResource.loading(null));

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("mealPlans")
                .whereEqualTo("weekStartDate", weekStartDate)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No plan found for this week
                        mealPlanLiveData.setValue(AuthResource.success(null));
                    } else {
                        // Plan found
                        MealPlan mealPlan = queryDocumentSnapshots.getDocuments().get(0).toObject(MealPlan.class);
                        mealPlanLiveData.setValue(AuthResource.success(mealPlan));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch meal plan for week", e);
                    mealPlanLiveData.setValue(AuthResource.error("Failed to fetch meal plan: " + e.getMessage(), null));
                });
    }

    /**
     * Updates an existing meal plan in Firestore.
     *
     * @param mealPlan       The meal plan to update
     * @param resultLiveData LiveData to notify about the operation result
     */
    public void updateMealPlan(MealPlan mealPlan, MutableLiveData<AuthResource<MealPlan>> resultLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            resultLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        if (mealPlan.getPlanId() == null) {
            resultLiveData.setValue(AuthResource.error("Meal plan ID is required for update", null));
            return;
        }

        resultLiveData.setValue(AuthResource.loading(null));

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("mealPlans")
                .document(mealPlan.getPlanId())
                .set(mealPlan)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Meal plan updated successfully: " + mealPlan.getPlanId());
                    resultLiveData.setValue(AuthResource.success(mealPlan));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update meal plan", e);
                    resultLiveData.setValue(AuthResource.error("Failed to update meal plan: " + e.getMessage(), null));
                });
    }

    /**
     * Deletes a meal plan from Firestore.
     *
     * @param mealPlan       The meal plan to delete
     * @param resultLiveData LiveData to notify about the operation result
     */
    public void deleteMealPlan(MealPlan mealPlan, MutableLiveData<AuthResource<Void>> resultLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            resultLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        if (mealPlan.getPlanId() == null) {
            resultLiveData.setValue(AuthResource.error("Meal plan ID is required for deletion", null));
            return;
        }

        resultLiveData.setValue(AuthResource.loading(null));

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("mealPlans")
                .document(mealPlan.getPlanId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Meal plan deleted successfully: " + mealPlan.getPlanId());
                    resultLiveData.setValue(AuthResource.success(null));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete meal plan", e);
                    resultLiveData.setValue(AuthResource.error("Failed to delete meal plan: " + e.getMessage(), null));
                });
    }
}