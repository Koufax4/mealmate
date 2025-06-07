package com.example.mealmate.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.GroceryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class for handling grocery list operations with Firebase
 * Firestore.
 */
public class GroceryRepository {

    private static final String TAG = "GroceryRepository";
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    public GroceryRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Fetches a specific grocery list by ID.
     *
     * @param listId   The ID of the grocery list to fetch
     * @param liveData LiveData to notify about the fetched grocery list
     */
    public void getGroceryList(String listId, MutableLiveData<AuthResource<List<GroceryItem>>> liveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            liveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        liveData.setValue(AuthResource.loading(null));

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("groceryLists")
                .document(listId)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GroceryItem> items = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        GroceryItem item = document.toObject(GroceryItem.class);
                        items.add(item);
                    }
                    Log.d(TAG, "Fetched " + items.size() + " grocery items");
                    liveData.setValue(AuthResource.success(items));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch grocery list", e);
                    liveData.setValue(AuthResource.error("Failed to fetch grocery list: " + e.getMessage(), null));
                });
    }

    /**
     * Updates a single grocery item's state.
     *
     * @param listId         The ID of the grocery list
     * @param item           The item to update
     * @param resultLiveData LiveData to notify about the operation result
     */
    public void updateGroceryItem(String listId, GroceryItem item, MutableLiveData<AuthResource<Void>> resultLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            resultLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        resultLiveData.setValue(AuthResource.loading(null));

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("groceryLists")
                .document(listId)
                .collection("items")
                .document(item.getItemId())
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Grocery item updated successfully: " + item.getItemId());
                    resultLiveData.setValue(AuthResource.success(null));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update grocery item", e);
                    resultLiveData.setValue(AuthResource.error("Failed to update item: " + e.getMessage(), null));
                });
    }

    /**
     * Deletes an item from the grocery list.
     *
     * @param listId         The ID of the grocery list
     * @param item           The item to delete
     * @param resultLiveData LiveData to notify about the operation result
     */
    public void deleteGroceryItem(String listId, GroceryItem item, MutableLiveData<AuthResource<Void>> resultLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            resultLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        resultLiveData.setValue(AuthResource.loading(null));

        firestore.collection("users")
                .document(currentUser.getUid())
                .collection("groceryLists")
                .document(listId)
                .collection("items")
                .document(item.getItemId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Grocery item deleted successfully: " + item.getItemId());
                    resultLiveData.setValue(AuthResource.success(null));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete grocery item", e);
                    resultLiveData.setValue(AuthResource.error("Failed to delete item: " + e.getMessage(), null));
                });
    }

    /**
     * Saves a newly generated grocery list to Firestore.
     *
     * @param listId         The ID for the new grocery list
     * @param items          The list of grocery items to save
     * @param resultLiveData LiveData to notify about the operation result
     */
    public void saveGroceryList(String listId, List<GroceryItem> items,
            MutableLiveData<AuthResource<Void>> resultLiveData) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            resultLiveData.setValue(AuthResource.error("User not authenticated", null));
            return;
        }

        resultLiveData.setValue(AuthResource.loading(null));

        // Create a batch write to save all items at once
        firestore.runBatch(batch -> {
            // First create the grocery list document with metadata
            Map<String, Object> listData = new HashMap<>();
            listData.put("listId", listId);
            listData.put("userId", currentUser.getUid());
            listData.put("createdAt", com.google.firebase.Timestamp.now());
            listData.put("itemCount", items.size());

            batch.set(firestore.collection("users")
                    .document(currentUser.getUid())
                    .collection("groceryLists")
                    .document(listId), listData);

            // Then add all the items
            for (GroceryItem item : items) {
                batch.set(firestore.collection("users")
                        .document(currentUser.getUid())
                        .collection("groceryLists")
                        .document(listId)
                        .collection("items")
                        .document(item.getItemId()), item);
            }
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Grocery list saved successfully with " + items.size() + " items");
            resultLiveData.setValue(AuthResource.success(null));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to save grocery list", e);
            resultLiveData.setValue(AuthResource.error("Failed to save grocery list: " + e.getMessage(), null));
        });
    }
}